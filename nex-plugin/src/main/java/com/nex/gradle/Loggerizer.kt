package com.nex.gradle

import javassist.CtClass
import javassist.CtMethod

class Loggerizer(
    private val clazz: CtClass,
    private val method: CtMethod
) {

    fun loggerize() {

        val lastIndex = method.parameterTypes.lastIndex
        val parameters = method.parameterTypes.toStringIndexed("\n") { index, ctClass ->
            appendParamToBuilder(index, ctClass, lastIndex)
        }
        val insertBefore = """
                    StringBuilder builder = new StringBuilder();
                    $parameters
                    android.util.Log.i("Nex:[${clazz.simpleName}]", "⇢ ${method.name}(" + builder.toString() + ")");  
                """.trimIndent().toJavassist()

        val hasReturnType = method.returnType != CtClass.voidType
        if (hasReturnType) {
            val insertAfter = """
                    android.util.Log.i("Nex:[${clazz.simpleName}]", "⇠ ${method.name}${if (!hasReturnType) "" else " [\"+@_+\"] "}");                    
                """.trimIndent().toJavassist()
            method.insertAfter(insertAfter)
        }

        println("\n")
        println("typeOfField: ${method.returnType.name}")
        println("INSERT BEFORE METHOD: ${method.name}\n")
        method.insertBefore(insertBefore)
    }


    private fun appendParamToBuilder(index: Int, parameter: CtClass, lastIndex: Int): String {
        val equals = "append(' ').append('=').append(' ')"
        var end = ";"
        if (index != lastIndex) end = ".append(\", \");"
        return "builder.append(\"${parameter.simpleName}\").$equals.append(@${index + 1})$end"
    }
}