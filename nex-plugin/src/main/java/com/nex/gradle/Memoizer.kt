package com.nex.gradle

import com.nex.Lazy
import com.nex.Throttle
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.NotFoundException
import javassist.bytecode.AccessFlag

class Memoizer(
    private val clazz: CtClass,
    private val method: CtMethod
) {

    fun memoize() {
        if (method.parameterTypes.isEmpty())
            error("@Memoize ${clazz.simpleName}.${method.name} method must contains parameters")
        if (method.hasAnnotation(Throttle::class.java))
            error("@Memoize ${clazz.simpleName}.${method.name} has placed on method which contains @Throttle")
        if (method.hasAnnotation(Lazy::class.java))
            error("@Memoize ${clazz.simpleName}.${method.name} has placed on method which contains @Lazy")

        val cacheResultFieldName = buildCacheMethodResult(clazz, method)

        // set main check of return type
        //var resultPreCondition = checkFieldOnEmpty(cacheResultFieldName, method)
        var cacheParameters = ""

        // go through all method parameters, cache each and create check that if parameter is the same
        val withResultCheck = mutableListOf<String>()
        cacheResultFieldName?.let { withResultCheck.add("$it != null") }
        for ((index, type) in method.parameterTypes.withIndex()) {
            // _$_methodName_index_parameterName
            val cacheName = "_\$_${method.name}_${index}_${type.name.replace(".", "_")}"
            clazz.replaceField(type, cacheName)

            // parameterName != _$_methodName_index_parameterName
            val checks = if (type.isPrimitive) "\$${index + 1} == \$0.$cacheName"
            // (parameterName != null && parameterName.equals(_$_methodName_index_parameterName))
            else "(\$${index + 1} != null && \$${index + 1}.equals(\$0.$cacheName))"

            cacheParameters += "\$0.$cacheName = \$${index + 1};\n"

            withResultCheck.add(checks)
        }

        var resultPreCondition = withResultCheck.joinToString(
            separator = " && ",
            prefix = "if (",
            postfix = ") return;"
        )
        if (method.returnType != CtClass.voidType) {
            resultPreCondition = resultPreCondition.replace("return;", " return $cacheResultFieldName;")
        }

        val insertBefore = """
                    $resultPreCondition
                    System.out.println("@Memoize: ${clazz.simpleName}.${method.name}");
                """.trimIndent()

        val insertAfter = """
                    $cacheParameters
                    ${if (cacheResultFieldName != null) "$cacheResultFieldName = \$_;" else ""}
                    """.trimIndent()

        println("\n")
        println("typeOfField: ${method.returnType.name}")
        println("INSERT BEFORE METHOD: ${method.name}\n")
        println(insertBefore.trimIndent().trimStart().trimEnd())
        println("INSERT AFTER METHOD: ${method.name}\n")
        println(insertAfter.trimIndent().trimMargin())
        println("\n")
        method.insertBefore(insertBefore)
        method.insertAfter(insertAfter)
    }

    private fun checkFieldOnEmpty(fieldName: String?, method: CtMethod): String? {
        if (method.returnType == CtClass.voidType) return null
        if (!method.returnType.isPrimitive) return "if ($fieldName != null) return $fieldName;"
        return "if ($fieldName != ${defaultValue(method)}) return $fieldName;"
    }

    private fun buildCacheMethodResult(clazz: CtClass, method: CtMethod): String? {
        if (method.returnType == CtClass.voidType) return null
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