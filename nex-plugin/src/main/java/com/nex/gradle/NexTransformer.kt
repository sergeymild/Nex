package com.nex.gradle

import com.android.build.api.transform.*
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.nex.Debounce
import com.nex.Memoize
import com.nex.Throttle
import javassist.ClassPool
import javassist.CtClass
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * The transformer.
 */

class NexTransformer(private val project: Project) : Transform() {

    override fun getName(): String = NexTransformer::class.java.simpleName
    override fun getInputTypes(): Set<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS
    override fun isIncremental(): Boolean = false
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
        TransformManager.PROJECT_ONLY

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> =
        mutableSetOf(QualifiedContent.Scope.EXTERNAL_LIBRARIES, QualifiedContent.Scope.SUB_PROJECTS)

    override fun transform(transformInvocation: TransformInvocation) {
        val result = measureTimeMillis {
            val pool = ClassPool()

            fillPoolAndroidInputs(pool)
            fillPoolReferencedInputs(transformInvocation, pool)
            fillPoolInputs(transformInvocation, pool)
            transformInvocation.outputProvider.deleteAll()


            transformInvocation.inputs.forEach { transformInput ->
                transformInput.directoryInputs.forEach { directoryInput ->
                    transformDirectoryInputs(pool, directoryInput, transformInvocation)
                }


                transformInput.jarInputs.forEach { jarInput ->
                    copyJarInputs(jarInput, transformInvocation)
                }
            }
        }
        println("-> Processing time: $result")
    }

    private fun fillPoolAndroidInputs(classPool: ClassPool) {
        classPool.appendClassPath(project.extensions.findByType(BaseExtension::class.java)!!.bootClasspath[0].toString())
    }

    private fun fillPoolReferencedInputs(
        transformInvocation: TransformInvocation,
        classPool: ClassPool
    ) {
        transformInvocation.referencedInputs.forEach { transformInput ->
            transformInput.directoryInputs.forEach { directoryInput ->
                classPool.appendClassPath(directoryInput.file.absolutePath)
            }
            transformInput.jarInputs.forEach { jarInput ->
                classPool.appendClassPath(jarInput.file.absolutePath)
            }
        }
    }

    private fun fillPoolInputs(transformInvocation: TransformInvocation, classPool: ClassPool) {
        transformInvocation.inputs.forEach { transformInput ->
            transformInput.directoryInputs.forEach { directoryInput ->
                classPool.appendClassPath(directoryInput.file.absolutePath)
            }
            transformInput.jarInputs.forEach { jarInput ->
                classPool.appendClassPath(jarInput.file.absolutePath)
            }
        }
    }

    private fun copyJarInputs(jarInput: JarInput, transformInvocation: TransformInvocation) {
        val destFolder = transformInvocation.outputProvider.getContentLocation(
            jarInput.name,
            jarInput.contentTypes,
            jarInput.scopes,
            Format.JAR
        )
        FileUtils.copyFile(jarInput.file, destFolder)
    }

    private fun transformDirectoryInputs(
        pool: ClassPool,
        inputDirectory: DirectoryInput,
        transformInvocation: TransformInvocation
    ) {
        val destFolder = transformInvocation.outputProvider.getContentLocation(
            inputDirectory.name,
            inputDirectory.contentTypes,
            inputDirectory.scopes,
            Format.DIRECTORY
        )

        inputDirectory.file.walkTopDown().forEach { originalClassFile ->
            if (originalClassFile.isClassfile()) {
                val classname = originalClassFile.relativeTo(inputDirectory.file).toClassname()
                val clazz = pool.get(classname)

                if (!shouldSkipProcess(originalClassFile)) {
                    transformClass(clazz, pool, destFolder.absolutePath)
                }

                clazz.writeFile(destFolder.absolutePath)
            }
        }

    }

    private fun transformClass(
        clazz: CtClass,
        pool: ClassPool,
        destFolder: String
    ) {


        for (method in clazz.declaredMethods) {
            if (method.isEmpty) continue

            if (method.hasAnnotation(Memoize::class.java.canonicalName)) {
                Memoizer(clazz, method).memoize()
            }

            if (method.hasAnnotation(Throttle::class.java.canonicalName)) {
                Throttler(clazz, method).throttle()
            }

            if (method.hasAnnotation(Debounce::class.java.canonicalName)) {
                Debouncer(
                    destFolder,
                    pool,
                    clazz,
                    method
                ).debounce()
            }
        }
    }

    private fun shouldSkipProcess(file: File): Boolean {
        if (file.name.endsWith("R.class")) return true
        if (file.name.endsWith("BuildConfig.class")) return true
        if (file.name.contains("R\$")) return true
        return false
    }
}