package com.nex.gradle

import android.annotation.TargetApi
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.CtNewMethod

/**
 *  Throttling enforces a maximum number of times a function can be called over time.
 * */
class AndroidAnnotationsHandler(
    private val pool: ClassPool,
    private val destFolder: String,
    private val clazz: CtClass,
    private val method: CtMethod
) {

    fun wrapInMainThreadCall() {
        if (method.returnType != CtClass.voidType)
            error("@UiThread ${clazz.simpleName}.${method.name} cannot be placed on method witch has a return type")
        val originalMethod = CtNewMethod.copy(method, clazz, null)
        originalMethod.name = "${method.name}MainThreadCall$$"
        // if method is static, don't need pass this as first parameter
        val incrementIndex = if (originalMethod.isStatic) 1 else 2
        val originalMethodParameters = method.parameterTypes.indicesToString { "\$0._${it + incrementIndex}" }

        val proxyMethod = CtNewMethod.copy(method, clazz, null)

        val repeatRunnable = clazz.makeNestedRunnableClass(
            className = "MainThreadCall${method.uniqueName()}Runnable",
            shouldAddSuper = !originalMethod.isStatic,
            constructorParameters = originalMethod.parameterTypes)

        val params = if (originalMethod.isStatic) "@@" else "@0, @@"
        val proxyBody = """{
            if (android.os.Looper.getMainLooper() == android.os.Looper.myLooper()) {
                ${originalMethod.name}(@@);
            } else {
                com.nex.Nex.nexUIHandler.post(new ${repeatRunnable.name}($params));
            }
        }""".toJavassist()

        clazz.removeMethod(method)
        clazz.addMethod(originalMethod)
        clazz.addMethod(proxyMethod)

        val runMethod = repeatRunnable.makeMethod("run") {
            if (originalMethod.isStatic) {
                add("${clazz.name}.${originalMethod.name}($originalMethodParameters);")
            } else {
                add("\$0._1.${originalMethod.name}($originalMethodParameters);")
                add("\$0._1 = null;")
            }

            val stringBuilder = StringBuilder()
            originalMethod.parameterTypes.forEachIndexed { index, ctClass ->
                if (!ctClass.isPrimitive) {
                    stringBuilder.append("\$0._${index + incrementIndex} = null;\n")
                }
            }
            add(stringBuilder.toString())
        }

        repeatRunnable.addMethod(runMethod)
        proxyMethod.setBody(proxyBody)
        repeatRunnable.writeFile(destFolder)
    }


    fun checkWorkerThread() {
        method.insertBefore("""
            if (android.os.Looper.getMainLooper() == android.os.Looper.myLooper()) {
                throw new IllegalStateException("${clazz.simpleName}.${method.name} must be called only from Main Thread.");
            }
        """.toJavassist())
    }

    fun wrapInTargetApiCall() {
        val targetApi = method.annotation(TargetApi::class.java).value
        method.insertBefore("""
            if (android.os.Build.VERSION.SDK_INT < $targetApi) return;
        """.toJavassist())
    }
}