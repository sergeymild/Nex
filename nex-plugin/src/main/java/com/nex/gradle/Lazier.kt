package com.nex.gradle

import com.nex.Lazy
import com.nex.Memoize
import com.nex.Throttle
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.NotFoundException
import javassist.bytecode.AccessFlag

class Lazier(
    private val clazz: CtClass,
    private val method: CtMethod
) {

    fun lazy() {

        if (method.returnType == CtClass.voidType)
            error("@Lazy may be placed only on method which return something. But in this case: ${clazz.simpleName}.${method.name} return void.")

        if (method.hasAnnotation(Throttle::class.java))
            error("@Lazy may placed on method which contains @Throttle")
        if (method.hasAnnotation(Memoize::class.java))
            error("@Lazy may placed on method which contains @Memoize")

        val cacheResultFieldName = buildCacheMethodResult(clazz, method)

        // set main check of return type
        val resultPreCondition = checkFieldOnEmpty(cacheResultFieldName, method)

        val insertBefore = """
                    $resultPreCondition
                    System.out.println("@Lazy: ${clazz.simpleName}.${method.name}");
                """.trimIndent()

        val insertAfter = """
                    $cacheResultFieldName = ${'$'}_;
                    """.trimIndent()

//        println("\n")
//        println("-----> Lazy")
//        println("INSERT BEFORE METHOD: ${method.name}\n")
//        println(insertBefore.trimIndent().trimStart().trimEnd())
//        println("INSERT AFTER METHOD: ${method.name}\n")
//        println(insertAfter.trimIndent().trimMargin())
//        println("\n")
        method.insertBefore(insertBefore)
        method.insertAfter(insertAfter)
    }

    private fun checkFieldOnEmpty(fieldName: String, method: CtMethod): String {
        if (!method.returnType.isPrimitive) return "if ($fieldName != null) return $fieldName;"
        return "if ($fieldName != ${defaultValue(method)}) return $fieldName;"
    }

    private fun buildCacheMethodResult(clazz: CtClass, method: CtMethod): String {
        val cachedName = "_\$_cached${method.name.capitalize()}"
        var isStatic = false

        val defaultValue = defaultValue(method)
        try {
            clazz.getDeclaredField(cachedName)
        } catch (e: NotFoundException) {
            isStatic = (method.methodInfo2.accessFlags and AccessFlag.STATIC) != 0
            val maybeStatic = if (isStatic) "static" else ""
            val initializer = """
                public $maybeStatic ${method.returnType.name} $cachedName = $defaultValue;
            """.trimIndent()

            val newField = CtField.make(initializer, clazz)
            clazz.addField(newField)
        }

        return "${if (isStatic) "" else "$0."}$cachedName"
    }
}