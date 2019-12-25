package com.tal.autotest.core.generateor.testcase

import com.tal.autotest.core.util.Case
import com.tal.autotest.core.util.DirectoryClassLoader
import com.tal.autotest.runtime.instrument.InstrumentAgentLoader
import com.tal.autotest.runtime.mock.MockFrameWork
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import java.lang.reflect.Method

class DefaultMethodTestCaseGenerator(
    private val mv: MethodVisitor,
    private var clz: Class<*>,
    private val method: Method,
    private val cacf: Case
) : AbstractTestCaseGenerator() {
    val paramsConfig = cacf.params
    override fun generateGiven() {
        if (!cacf.mock.isEmpty()) {
            cacf.mock.forEach {
                val className = it.className.replace(".", "/")
                val methodName = it.methodName
                val ret = it.ret["value"]!!
                val params = it.params
                val clz: Class<*> = Thread.currentThread().contextClassLoader.loadClass(it.className)
                val method: Method? = clz.methods.find {
                    it.name == methodName && it.parameterCount == params.size }
                if (method == null) {
                    System.out.println("mock method with specified params not found")
                    return
                }
                mv.visitLdcInsn(Type.getType("L${className};"))
                mv.visitMethodInsn(INVOKESTATIC, "com/tal/autotest/runtime/mock/MockFrameWork", "mock", "(Ljava/lang/Class;)Ljava/lang/Object;", false)
                mv.visitTypeInsn(CHECKCAST, className)
                varCounter++
                mv.visitVarInsn(ASTORE, varCounter)
                mv.visitVarInsn(ALOAD, varCounter)
                addParamsByteCode(method, params, mv)
                mv.visitMethodInsn(INVOKEVIRTUAL, className, methodName, Type.getMethodDescriptor(method), false)
                mv.visitMethodInsn(INVOKESTATIC, "com/tal/autotest/runtime/mock/MockFrameWork", "when", "(Ljava/lang/Object;)Lcom/tal/autotest/runtime/mock/MockFrameWork\$CaseBuilder;", false)
                addParamByteCode(method.returnType, ret, mv, method.genericReturnType)
                mv.visitMethodInsn(INVOKEVIRTUAL, "com/tal/autotest/runtime/mock/MockFrameWork\$CaseBuilder", "thenReturn", "(Ljava/lang/Object;)V", false)
                val list = processParams(method, params)
                val retObj = processParam(method.returnType, ret, method.genericReturnType)
                val mockInstance = MockFrameWork.mock(clz)
                MockFrameWork.`when`(method.invoke(mockInstance, *list.toArray()))
                    .thenReturn(retObj)
            }
        }
    }

    override fun generateWhen() {
        var opcode = INVOKESTATIC
        if (!isStaticMethod(method)) {
            mv.visitTypeInsn(NEW, clz.name.replace('.', '/'))
            mv.visitInsn(DUP)
            mv.visitMethodInsn(INVOKESPECIAL, clz.name.replace('.', '/'), "<init>", "()V", false)
            varCounter++
            mv.visitVarInsn(ASTORE, varCounter)
            mv.visitVarInsn(ALOAD, varCounter)
            opcode = INVOKEVIRTUAL
        }
        addParamsByteCode(method, paramsConfig, mv)
        val methodDesc = processMethodDesc(method)
        mv.visitMethodInsn(opcode, clz.name.replace('.', '/'), method.name, methodDesc, false)
        postProcess(method.returnType, mv)
    }

    override fun probeRealResult(): Any? {
        val list = processParams(method, paramsConfig)
        var obj: Any? = null
        if (!cacf.mock.isEmpty()) {
            InstrumentAgentLoader.initialize()
            val loader = Thread.currentThread().contextClassLoader
            if (loader is DirectoryClassLoader) {
                (loader as DirectoryClassLoader).redefineClass(clz)
            }
            MockFrameWork.inActive()
        }
        if (!isStaticMethod(method)) {
            obj = clz.newInstance()
        }
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

    private fun isStaticMethod(method: Method) = (method.modifiers and ACC_STATIC) != 0
}