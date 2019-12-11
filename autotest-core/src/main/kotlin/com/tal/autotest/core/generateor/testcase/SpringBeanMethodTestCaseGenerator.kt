package com.tal.autotest.core.generateor.testcase

import kotlinx.serialization.json.JsonObject
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.springframework.test.context.TestContextManager
import java.lang.reflect.Method

class SpringBeanMethodTestCaseGenerator(
    private val mv: MethodVisitor,
    private val context: TestContextManager,
    private val clz: Class<*>,
    private val method: Method,
    private val config: List<JsonObject>
) : TestCaseGenerator() {
    override fun generate() {
        val className = clz.name
        val slashedClzName = className.replace('.', '/')
        val testClassName = "${slashedClzName}Test"
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, testClassName, "instance", "L${slashedClzName};")
        val list = processParams(method, config)
        addParamsByteCode(method, config, list, mv)
        val methodDesc = processMethodDesc(method)
        mv.visitMethodInsn(INVOKEVIRTUAL, slashedClzName, method.name, methodDesc, false)
        val obj = clz.newInstance()
        System.out.println("autowire instance")
        context.prepareTestInstance(obj)
        try {
            val ret = method.invoke(obj, *list.toArray())
            if (method.returnType.isPrimitive) {
                postProcess(method.returnType, mv)
            }
            processVerifyByteCode(method, mv, ret)
        } catch (e : Exception) {
        }
        mv.visitInsn(RETURN)
        mv.visitMaxs(varCounter + 1, varCounter + 1)
        context.afterTestClass()
    }
}