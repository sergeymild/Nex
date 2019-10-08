package com.nex.gradle

import javassist.*
import javassist.bytecode.AccessFlag
import java.io.File


internal fun CtClass.replaceField(fieldType: CtClass, name: String) {
    val newField = CtField(fieldType, name, this)
    try { removeField(newField) } catch (e: Throwable) {}
    addField(newField)
}

internal fun CtClass.addFieldIfNotPresent(fieldType: CtClass, name: String) {
    try {
        getField(name)
    } catch (e: NotFoundException) {
        val newField = CtField(fieldType, name, this)
        addField(newField)
    }
}

internal fun String.toJavassist(): String = replace("@", "${'$'}").trimIndent().trim()

internal fun File.toClassname(): String =
    path.replace("/", ".")
        .replace("\\", ".")
        .replace(".class", "")

internal fun File.isClassfile(): Boolean = isFile && path.endsWith(".class")

inline fun <T> Array<out T>.indicesToString(separator: String = ",", crossinline transform: (index: Int) -> String): String {
    return mapIndexed { index, _ ->
        transform(index)
    }.joinToString(separator)
}

inline fun CtClass.newMethod(
    name: String,
    returnType: CtClass = CtClass.voidType,
    body: () -> String): CtMethod {
    return CtNewMethod.make(
        returnType,
        name,
        emptyArray(),
        emptyArray(),
        body(),
        this
    )
}

fun CtClass.addPublicField(type: CtClass, name: String): CtField {
    return CtField(type, name, this).also {
        it.modifiers = AccessFlag.PUBLIC
        addField(it)
    }
}

fun CtClass.addPrivateField(type: CtClass, name: String): CtField {
    return CtField(type, name, this).also {
        it.modifiers = AccessFlag.PRIVATE
        addField(it)
    }
}

fun CtClass.addDefaultConstructor() {
    val constructorBody = """{
            System.out.println("init runnable");
        }""".trimIndent()
    addConstructor(
        CtNewConstructor.make(
            emptyArray(),
            emptyArray(),
            constructorBody,
            this
        )
    )
}