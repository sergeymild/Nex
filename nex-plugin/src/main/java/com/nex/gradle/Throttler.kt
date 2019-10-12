package com.nex.gradle

import com.nex.Throttle
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.NotFoundException
import javassist.bytecode.AccessFlag

/**
 *  Throttling enforces a maximum number of times a function can be called over time.
 * */
class Throttler(
    private val clazz: CtClass,
    private val method: CtMethod) {

    fun throttle() {
        if (method.returnType != CtClass.voidType)
            error("@${Throttle::class.java.simpleName} may be placed only on method which return void. But in this case: ${clazz.simpleName}.${method.name} return ${method.returnType.simpleName}.")
        if (method.hasAnnotation(Memoizer::class.java))
            error("@Throttle may placed on method which contains @Memoize")

        val cacheResultFieldName = buildCacheLastTimeMethodCalled(clazz, method)

        val throttleValue = getThrottleTimeFromAnnotation()

        method.insertBefore("""{
            if (System.currentTimeMillis() - $cacheResultFieldName < $throttleValue) return;
            $cacheResultFieldName = System.currentTimeMillis();
        }""".trimIndent())
    }

    private fun getThrottleTimeFromAnnotation(): Long {
        val annotation = method.getAnnotation(Throttle::class.java) as Throttle
        return annotation.value
    }

    private fun buildCacheLastTimeMethodCalled(clazz: CtClass, method: CtMethod): String {
        val cachedName = "_\$_lastTimeMethodCalled${method.name.capitalize()}"
        var isStatic = false

        try {
            clazz.getDeclaredField(cachedName)
        } catch (e: NotFoundException) {
            var initializer = "public ${CtClass.longType.name} $cachedName;"
            if ((method.methodInfo2.accessFlags and AccessFlag.STATIC) != 0) {
                isStatic = true
                initializer = "public static ${CtClass.longType.name} $cachedName;"
            }
            val newField = CtField.make(initializer, clazz)
            clazz.addField(newField)
        }

        return "${if (isStatic) "" else "$0."}$cachedName"
    }
}