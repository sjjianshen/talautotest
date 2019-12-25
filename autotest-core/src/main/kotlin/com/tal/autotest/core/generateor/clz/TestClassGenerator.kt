package com.tal.autotest.core.generateor.clz

import com.tal.autotest.core.generateor.method.MethodGenerator
import com.tal.autotest.core.util.AutotestContext
import com.tal.autotest.core.util.ClassConfig
import com.tal.autotest.core.util.FileSystemUtil
import com.tal.autotest.core.util.MethodGeneratorContext
import org.objectweb.asm.*
import java.nio.file.Files
import java.nio.file.Paths

open class TestClassGenerator(val ccf : ClassConfig, val ctx : AutotestContext) {
    fun generateTestClass() {
        val outputPath = ctx.outputPath
        val outputClassPath = ctx.outputClassPath
        val className = ccf.name
        val targetClassName = "${className}Test"
        val slashedClassName = targetClassName.replace(".", "/")
        val targetName = "$outputPath/${slashedClassName}.java"
        val targetClassPath = "${outputClassPath}/${slashedClassName}.class"
        if (true or Files.exists(Paths.get(targetName))) {
            val clz = Thread.currentThread().contextClassLoader.loadClass(className)
            if (clz == null) {
                System.out.println("Failed to load target class: $className, class generate skipped")
            }
            val cw = ClassWriter(0)
            cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, slashedClassName, null, "java/lang/Object", null)
            afterClassInitialized(cw, clz)
            doGenerateTestClass(cw, clz)
            cw.visitEnd()
            val byteArray = cw.toByteArray()
            writeClass(targetClassPath, byteArray)
        }
    }

    open fun afterClassInitialized(cw: ClassWriter, clz: Class<*>) {
        if (ccf.useMock) {
            var cav: AnnotationVisitor = cw.visitAnnotation("Lorg/junit/runner/RunWith;", true)
            cav.visit("value", Type.getType("Lcom/tal/autotest/runtime/AutotestRunner;"))
            cav.visitEnd()
        }
    }

    open fun doGenerateTestClass(cw: ClassWriter, clazz: Class<*>) {
        ccf.methodConfigs.forEach {
            val mtc = MethodGeneratorContext(ccf, it, cw, clazz)
            MethodGenerator(mtc).generateTestCase()
        }
    }

    fun writeClass(targetClassPath: String, byteArray: ByteArray) {
        val classDir = targetClassPath.substring(0, targetClassPath.lastIndexOf('/'))
        FileSystemUtil.makeSureDirectoryExist(classDir)
        Files.write(Paths.get(targetClassPath), byteArray)
    }
}