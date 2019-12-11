package com.tal.autotest.core.generateor.testcase

import kotlinx.serialization.json.JsonObject
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import java.lang.reflect.Method

class NonStaticMethodTestCaseGenerator(
    private val mv: MethodVisitor,
    private val clz: Class<*>,
    private val method: Method,
    private val config: List<JsonObject>
) : TestCaseGenerator() {
    override fun generate() {
        varCounter++
        mv.visitTypeInsn(NEW, clz.name.replace('.', '/'))
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKESPECIAL, clz.name.replace('.', '/'), "<init>", "()V", false)
        varCounter++
        mv.visitVarInsn(ASTORE, varCounter)
        mv.visitVarInsn(ALOAD, varCounter)
        val list = processParams(method, config)
        addParamsByteCode(method, config, list, mv)
        val methodDesc = processMethodDesc(method)
        mv.visitMethodInsn(INVOKEVIRTUAL, clz.name.replace('.', '/'), method.name, methodDesc, false)
        val obj = clz.newInstance()
        val ret = method.invoke(obj, *list.toArray())
//        if (method.returnType.isPrimitive) {
        postProcess(method.returnType, mv)
//        }
        processVerifyByteCode(method, mv, ret)
        mv.visitInsn(RETURN)
        mv.visitMaxs(varCounter + 100, varCounter + 1)
    }
}