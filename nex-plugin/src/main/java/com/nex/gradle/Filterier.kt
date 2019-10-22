package com.nex.gradle

import com.nex.Filter
import com.nex.Lazy
import com.nex.Throttle
import javassist.CtClass
import javassist.CtMethod

class Filterier(
    private val clazz: CtClass,
    private val method: CtMethod
) {

    private fun validates() {
        println(method.parameterTypes.joinToString { it.name })
        if (method.returnType != CtClass.voidType)
            error("@Filter may be placed only on method which return void. But in this case: ${clazz.simpleName}.${method.name} return ${method.returnType.simpleName}.")
        if (method.parameterTypes.isEmpty())
            error("@Filter may be placed only on method which contains parameters. ${clazz.simpleName}.${method.name}")
        listOf(Throttle::class.java, Lazy::class.java).forEach {
            if (method.hasAnnotation(it))
                error("@Filter may placed on method which contains @${it.simpleName}")
        }
    }

    fun filter() {

        validates()

        var cacheParameters = ""

        // go through all method parameters, cache each and create check that if parameter is the same
        val withResultCheck = mutableListOf<String>()

        val filterParameterIndex = (method.getAnnotation(Filter::class.java) as Filter).parameterIndex
        for ((index, type) in method.parameterTypes.withIndex()) {
            if (filterParameterIndex != -1 && filterParameterIndex != index) continue
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

        val resultPreCondition = withResultCheck.joinToString(
            separator = " && ",
            prefix = "if (",
            postfix = ") return;"
        )

        println("""
            @Filter(before)
            $resultPreCondition
            
            @Filter(after)
            $cacheParameters
        """.trimIndent())

        method.insertBefore(resultPreCondition)
        method.insertAfter(cacheParameters)
    }
}