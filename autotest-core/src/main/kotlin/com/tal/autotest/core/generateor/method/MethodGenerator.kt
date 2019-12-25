package com.tal.autotest.core.generateor.method

import com.tal.autotest.core.generateor.testcase.DefaultMethodTestCaseGenerator
import com.tal.autotest.core.util.Case
import com.tal.autotest.core.util.ClassConfig
import com.tal.autotest.core.util.MethodConfig
import com.tal.autotest.core.util.MethodGeneratorContext
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.lang.reflect.Method

open class MethodGenerator(
    mtc: MethodGeneratorContext
) {
    val mcf: MethodConfig = mtc.mcf
    val clz: Class<*> = mtc.clz
    val cw: ClassVisitor = mtc.cw
    val ccf: ClassConfig = mtc.ccf

    fun generateTestCase() {
        val methods = clz.methods
        val methodName = mcf.name
        val suitableMethods = methods.filter { method ->
            method.name == methodName
        }
        if (suitableMethods.isEmpty()) {
            System.out.println("没有找到匹配的目标方法: $methodName")
            return
        }
        mcf.cases.forEach { cacf ->
            try {
                val caseName = cacf.name
                val configParams = cacf.params
                val paramCount = configParams.size
                val matchMethods = suitableMethods.filter { it.parameterCount == paramCount }
                if (matchMethods.isEmpty()) {
                    System.out.println("没有找到case: ${cacf.name} 匹配的目标方法: $methodName, 跳过case生成")
                    return@forEach
                }
                val method = matchMethods[0]
                val mv = cw.visitMethod(Opcodes.ACC_PUBLIC, caseName, "()V", null, null)
                val av = mv.visitAnnotation("Lorg/junit/Test;", true)
                av.visitEnd()
                mv.visitCode()
                doGenerateMethodCode(method, mv, cacf)
                mv.visitEnd()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    open fun doGenerateMethodCode(
        method: Method,
        mv: MethodVisitor,
        cacf: Case
    ) {
        DefaultMethodTestCaseGenerator(
            mv,
            clz,
            method,
            cacf
        ).generate()
    }
}