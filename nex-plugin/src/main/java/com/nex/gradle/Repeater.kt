package com.nex.gradle

import com.nex.Lazy
import com.nex.Repeat
import com.nex.Throttle
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.CtNewMethod

class Repeater(
    private val destFolder: String,
    private val pool: ClassPool,
    private val clazz: CtClass,
    private val method: CtMethod
) {

    fun repeat() {

        if (method.hasAnnotation(Throttle::class.java))
            error("@Repeat may placed on method which contains @Throttle")
        if (method.hasAnnotation(Lazy::class.java))
            error("@Repeat may placed on method which contains @Lazy")

        val methodParameters = method.parameterTypes
        if (methodParameters.isEmpty()) {
            error("@Repeat may placed on method with minimum one parameter, and it must be Boolean.")
        }

        if (methodParameters[0] != CtClass.booleanType) {
            error("@Repeat may placed on method where first parameter must be Boolean")
        }

        val repeatRunnable = clazz.makeNestedClass("Repeat${method.name.capitalize()}Runnable", true)
        repeatRunnable.addInterface(pool.get("java.lang.Runnable"))

        val repeatRunnableField = "_\$_repeatMethodCall${method.name.capitalize()}"
        clazz.addPublicField(repeatRunnable, repeatRunnableField)

        val repeatValue = method.annotation(Repeat::class.java).every

        val originalMethod = CtNewMethod.copy(method, clazz, null)
        originalMethod.name = "${method.name}\$\$"

        val proxyMethod = CtNewMethod.copy(method, clazz, null)

        repeatRunnable.addPublicField(clazz, "_super")
        repeatRunnable.addPublicField(CtClass.booleanType, "isActive")
        originalMethod.parameterTypes.forEachIndexed { index, c ->
            repeatRunnable.addPublicField(c, "_${index + 1}")
        }

        repeatRunnable.addDefaultConstructor()


        val proxyBody = """{
            if (@0.${repeatRunnableField} != null) {
                @0.$repeatRunnableField.isActive = false;
                com.nex.Nex.nexUIHandler.removeCallbacks(@0.$repeatRunnableField);
                @0.$repeatRunnableField.clear();
            }
            if (!@1) {
                return;
            }
            if (@0.${repeatRunnableField} == null) {
                @0.$repeatRunnableField = new ${repeatRunnable.name}(); 
            }
            @0.$repeatRunnableField.isActive = @1;
            @0.${repeatRunnableField}._super = @0;
            ${originalMethod.parameterTypes.indicesToString("\n") {
            "@0.${repeatRunnableField}._${it + 1} = @${it + 1};"
        }}            
            com.nex.Nex.nexUIHandler.postDelayed(@0.$repeatRunnableField, (long)$repeatValue);
        }""".toJavassist()

        clazz.removeMethod(method)
        clazz.addMethod(originalMethod)
        clazz.addMethod(proxyMethod)

        val runMethod = repeatRunnable.newMethod("run") {
            """{
                if (!isActive) return;
                $0._super.${originalMethod.name}(${originalMethod.parameterTypes.indicesToString { "\$0._${it + 1}" }});
                com.nex.Nex.nexUIHandler.postDelayed($0, (long)$repeatValue);
            }""".trimIndent()
        }

        val clearMethod = repeatRunnable.newMethod("clear") {
            """{
                $0._super.$repeatRunnableField = null;
                $0._super = null;
                ${originalMethod.parameterTypes.filter { !it.isPrimitive }.indicesToString("\n") { "\$0._${it + 1} = null;" }}
            }""".trimIndent()
        }

        repeatRunnable.addMethod(clearMethod)
        repeatRunnable.addMethod(runMethod)
        proxyMethod.setBody(proxyBody)
        repeatRunnable.writeFile(destFolder)
    }
}