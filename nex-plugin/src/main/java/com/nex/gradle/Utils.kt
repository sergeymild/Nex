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

inline fun <T> Array<out T>.toStringIndexed(separator: String = ",", crossinline transform: (index: Int, value: T) -> String): String {
    return mapIndexed { index, value ->
        transform(index, value)
    }.joinToString(separator)
}

inline fun <T> List<T>.indicesToString(separator: String = ",", crossinline transform: (index: Int) -> String): String {
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

inline fun CtClass.makeMethod(
    name: String,
    returnType: CtClass = CtClass.voidType,
    body: MutableList<String>.() -> Unit): CtMethod {

    val bodyList = mutableListOf<String>()
    body.invoke(bodyList)
    val bodyString = bodyList.joinToString("\n", prefix = "{", postfix = "}")

    return CtNewMethod.make(
        returnType,
        name,
        emptyArray(),
        emptyArray(),
        bodyString,
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

fun defaultValue(method: CtMethod): String {
    return when(method.returnType) {
        CtClass.booleanType -> "false"
        CtClass.charType -> "0"
        CtClass.byteType -> "0"
        CtClass.shortType -> "0"
        CtClass.intType -> "0"
        CtClass.longType -> "0L"
        CtClass.floatType -> "0F"
        CtClass.doubleType -> "0.0"
        else -> "null"
    }
}

fun <T> CtMethod.annotation(type: Class<T>) : T {
    return getAnnotation(type) as T
}

fun CtMethod.getParametersForMethodInvoke(): String {
    return parameterTypes.indicesToString { "\$0._${it + 1}" }
}

fun CtMethod.clearFieldParameters(): String {
    return parameterTypes.filter { !it.isPrimitive }.indicesToString("\n") { "\$0._${it + 1} = null;" }
}

fun CtClass.makeNestedRunnableClass(
    className: String,
    shouldAddSuper: Boolean = true,
    constructorParameters: Array<CtClass>
): CtClass {
    val runnable = makeNestedClass(className, true)
    runnable.addInterface(ClassPool.getDefault().get("java.lang.Runnable"))

    val params = mutableListOf<CtClass>()
    if (shouldAddSuper) params.add(this)
    params.addAll(constructorParameters)

    params.forEachIndexed { index, ctClass ->
        runnable.addPublicField(ctClass, "_${index + 1}")
    }

    runnable.addConstructor(CtNewConstructor.make(
        params.toTypedArray(),
        emptyArray(),
        """
            {${params.indicesToString("\n") { index -> "_${index + 1} = @${index + 1};" }}}
        """.toJavassist(),
        runnable
    ))

    return runnable
}

val CtMethod.isStatic: Boolean get() = (methodInfo2.accessFlags and AccessFlag.STATIC) != 0

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


//        if (jarInput.file.absolutePath.contains("sqlite-framework-2.0.0")) {
//            val ctc = pool.get("androidx.sqlite.db.framework.FrameworkSQLiteStatement")
//            ctc.getDeclaredMethod("executeInsert").insertBefore("""
//                com.nex.Checker.check(mDelegate);
//            """.trimIndent())
//            val toBytecode = ctc.toBytecode()
//            val input = JarFile(jarInput.file)
//            val output = JarOutputStream(FileOutputStream(destFolder))
//            for (entry in input.entries()) {
//                if (entry.name != "androidx/sqlite/db/framework/FrameworkSQLiteStatement.class") {
//                    val s = input.getInputStream(entry)
//                    output.putNextEntry(JarEntry(entry.name))
//                    IOUtils.copy(s, output)
//                    s.close()
//                }
//            }
//
//            output.putNextEntry(JarEntry("androidx/sqlite/db/framework/FrameworkSQLiteStatement.class"))
//            output.write(toBytecode)
//            output.close()
//        } else {
//        }