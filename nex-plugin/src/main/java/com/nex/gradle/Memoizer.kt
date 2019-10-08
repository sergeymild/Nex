package com.nex.gradle

import javassist.CtClass
import javassist.CtMethod

class Memoizer(
    private val clazz: CtClass,
    private val method: CtMethod
) {

    fun memoize() {

        if (method.returnType == CtClass.voidType)
            error("@Memoize may be placed only on method which return something. But in this case: ${clazz.simpleName}.${method.name} return void.")

        val cacheResultFieldName = buildCacheMethodResult(clazz, method)

        // set main check of return type
        var resultPreCondition =
            "if (\$0.${cacheResultFieldName} != null) return \$0.${cacheResultFieldName};"
        var cacheParameters = ""

        // go through all method parameters, cache each and create check that if parameter is the same
        val withResultCheck = mutableListOf<String>()
        withResultCheck.add("\$0.${cacheResultFieldName} != null")
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

        if (method.parameterTypes.isNotEmpty()) {
            resultPreCondition = withResultCheck.joinToString(
                separator = " && ",
                prefix = "if (",
                postfix = ") return \$0.${cacheResultFieldName};"
            )
        }

        val insertBefore = """
                    $resultPreCondition
                    System.out.println("@Memoize: ${clazz.simpleName}.${method.name}");
                """.trimIndent()

        val insertAfter = """
                    $cacheParameters
                    @0.${cacheResultFieldName} = @_;
                    """.toJavassist()

        println("\n")
        println("INSERT BEFORE METHOD: ${method.name}\n")
        println(insertBefore.trimIndent().trimStart().trimEnd())
        println("INSERT AFTER METHOD: ${method.name}")
        println(insertAfter.trimIndent().trimMargin())
        println("\n")
        method.insertBefore(insertBefore)
        method.insertAfter(insertAfter)
    }

    private fun buildCacheMethodResult(clazz: CtClass, method: CtMethod): String {
        val cachedName = "_\$_cached${method.name.capitalize()}"
        clazz.replaceField(method.returnType, cachedName)
        return cachedName
    }
}