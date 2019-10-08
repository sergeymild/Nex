package com.nex.gradle

import javassist.*
import javassist.bytecode.AccessFlag
import javassist.bytecode.ConstPool
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

fun ConstPool.getMethodName(ref: Int): String? {
    return try {
        getMethodrefName(ref)
    } catch (e: Throwable) {
        null
    }
}

//val codeAttribute = method.methodInfo2.codeAttribute
//val iterator = codeAttribute.iterator()
//println("----: ${method.name}")
//while (iterator.hasNext()) {
//    val currentPosition = iterator.next()
//
//    val cp = codeAttribute.constPool
//    if (iterator.codeLength != currentPosition + 1) {
//        val mref = ByteArray.readU16bit(iterator.get().code, currentPosition + 1)
//
//        if (cp.getMethodName(mref) == "setContentView") {
//            val m = method.methodInfo.getLineNumber(currentPosition + 1)
//            method.insertAt(m + 1, true, findFields())
//
//            break
//        }
//    }
//}