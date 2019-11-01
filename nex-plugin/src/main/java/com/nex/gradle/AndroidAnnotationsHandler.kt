package com.nex.gradle

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
        val originalMethod = CtNewMethod.copy(method, clazz, null)
        originalMethod.name = "${method.name}MainThreadCall$$"
        val originalMethodParameters = method.parameterTypes.indicesToString { "\$0._${it + 2}" }

        val proxyMethod = CtNewMethod.copy(method, clazz, null)


        val repeatRunnable = clazz.makeNestedRunnableClass(
            className = "MainThreadCall${method.name.capitalize()}Runnable",
            constructorParameters = originalMethod.parameterTypes)

        val proxyBody = """{
            if (android.os.Looper.getMainLooper() == android.os.Looper.myLooper()) {
                ${originalMethod.name}(@@);
            } else {
                com.nex.Nex.nexUIHandler.post(new ${repeatRunnable.name}(@0, @@));
            }
        }""".toJavassist()

        clazz.removeMethod(method)
        clazz.addMethod(originalMethod)
        clazz.addMethod(proxyMethod)

        val runMethod = repeatRunnable.makeMethod("run") {
            add("\$0._1.${originalMethod.name}($originalMethodParameters);")
            add("\$0._1 = null;")
            add(originalMethod.clearFieldParameters())
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
}