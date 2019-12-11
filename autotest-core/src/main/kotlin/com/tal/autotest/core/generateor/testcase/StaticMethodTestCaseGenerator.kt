package com.tal.autotest.core.generateor.testcase

import kotlinx.serialization.json.JsonObject
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.RETURN
import java.lang.reflect.Method

class StaticMethodTestCaseGenerator(
    private val mv: MethodVisitor,
    private val clz: Class<*>,
    private val method: Method,
    private val config: List<JsonObject>
) : TestCaseGenerator() {
    override fun generate() {
        val list = processParams(method, config)
        addParamsByteCode(method, config, list, mv)
        val ret = method.invoke(null, *list.toArray())
        val methodDesc = processMethodDesc(method)
        mv.visitMethodInsn(INVOKESTATIC, clz.name.replace('.', '/'), method.name, methodDesc, false)
        if (method.returnType.isPrimitive) {
            postProcess(method.returnType, mv)
        }
        processVerifyByteCode(method, mv, ret)
        mv.visitInsn(RETURN)
        mv.visitMaxs(3, 3)
    }
}