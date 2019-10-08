package com.nex.gradle

import com.nex.Throttle
import javassist.CtClass
import javassist.CtMethod

/**
 *  Throttling enforces a maximum number of times a function can be called over time.
 * */
class Throttler(
    private val clazz: CtClass,
    private val method: CtMethod) {

    fun throttle() {
        if (method.returnType != CtClass.voidType)
            error("@${Throttle::class.java.simpleName} may be placed only on method which return void. But in this case: ${clazz.simpleName}.${method.name} return ${method.returnType.simpleName}.")

        val cacheResultFieldName = buildCacheLastTimeMethodCalled(clazz, method)

        val throttleValue = getThrottleTimeFromAnnotation()

        method.insertBefore("""{
            if (System.currentTimeMillis() - @0.$cacheResultFieldName < $throttleValue) return;
            @0.$cacheResultFieldName = System.currentTimeMillis();
        }""".toJavassist())
    }

    private fun getThrottleTimeFromAnnotation(): Long {
        val annotation = method.getAnnotation(Throttle::class.java) as Throttle
        return annotation.value
    }

    private fun buildCacheLastTimeMethodCalled(clazz: CtClass, method: CtMethod): String {
        val cachedName = "_\$_lastTimeMethodCalled${method.name.capitalize()}"
        clazz.replaceField(CtClass.longType, cachedName)
        return cachedName
    }
}