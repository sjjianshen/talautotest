package com.tal.autotest.core.generateor.testcase

import kotlinx.serialization.json.JsonElement
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
) : AbstractTestCaseGenerator() {
    override fun generateGiven() {
        // nothing to do now
    }

    override fun generateWhen() {
        val className = clz.name
        val slashedClzName = className.replace('.', '/')
        val testClassName = "${slashedClzName}Test"
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, testClassName, "instance", "L${slashedClzName};")
        addParamsByteCode(method, config, mv)
        val methodDesc = processMethodDesc(method)
        mv.visitMethodInsn(INVOKEVIRTUAL, slashedClzName, method.name, methodDesc, false)
        postProcess(method.returnType, mv)
    }

    override fun probeRealResult(): Any? {
        val obj = clz.newInstance()
        System.out.println("autowire instance")
        context.prepareTestInstance(obj)
        val list = processParams(method, config)
        val ret = method.invoke(obj, *list.toArray())
        return ret
    }

    override fun generateThen(ret: Any?) {
        processVerifyByteCode(method, mv, ret)
        mv.visitInsn(RETURN)
        mv.visitMaxs(varCounter + 100, varCounter + 1)
    }

    override fun afterGenerate() {
    }
}