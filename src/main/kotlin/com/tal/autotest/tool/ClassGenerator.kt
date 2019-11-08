package com.tal.autotest.tool

import kotlinx.serialization.json.Json
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.springframework.cglib.core.DebuggingClassWriter
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

class ClassGenerator(val workspace: String, val urls: Array<URL>) {
    public fun launch() {
        val configPath = "$workspace/talTester/config1.json"
        val config = Json.parse(InputConfig.serializer(), File(configPath).readText())
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "/Users/jianshen/workspace/innovation-backend/class")
        System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true")
        val outputPath = "$workspace/src/test/java"
        val outputClassPath = "$workspace/build/talTester/classes"
        val targetClassLoader =
            DirectoryClassLoader(workspace, urls, Thread.currentThread().contextClassLoader)
        Thread.currentThread().contextClassLoader = targetClassLoader
        config.classConfigs.forEach {
            val ccf = it
            val className = ccf.name
            val targetClassName = "${className}Test"
            val slashedClassName = targetClassName.replace(".", "/")
            val targetName = "$outputPath/${slashedClassName}.java"
            val targetClassPath = "${outputClassPath}/${slashedClassName}.class"
            if (true or Files.exists(Paths.get(targetName))) {
                val clz = targetClassLoader.loadClass(className)
                val methods = clz?.methods
                val cw = ClassWriter(0)
                cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, slashedClassName, null, "java/lang/Object", null)
                if (ccf.autowire) {
                    var cav : AnnotationVisitor = cw.visitAnnotation("Lorg/junit/runner/RunWith;", true)
                    cav.visit("value", Type.getType("Lorg/springframework/test/context/junit4/SpringRunner;"))
                    cav.visitEnd()
                    cav = cw.visitAnnotation("Lorg/springframework/test/context/ContextConfiguration;", true)
                    var av1 = cav.visitArray("classes")
                    av1.visit(null, Type.getType("L${ccf.appName.replace(".", "/")};"))
                    av1.visitEnd()
                    av1 = cav.visitArray("initializers")
                    av1.visit(
                        null,
                        Type.getType("Lorg/springframework/boot/test/context/ConfigFileApplicationContextInitializer;")
                    )
                    av1.visitEnd()
                    cav.visitEnd()
                    val fv : FieldVisitor = cw.visitField(ACC_PRIVATE, "instance", "L${className};", null, null)
                    val fav : AnnotationVisitor = fv.visitAnnotation("Lorg/springframework/beans/factory/annotation/Autowired;", true)
                    fav.visitEnd()
                    fv.visitEnd()
                }
                ccf.methodConfigs.forEach { methodConfig ->
                    val mcf = methodConfig
                    val methodName = mcf.name
                    val suitableMethods = methods?.filter { method ->
                        method.name == methodName
                    }
                    mcf.cases.forEach { case ->
                        try {
                            val cacf = case
                            val configParams = cacf.params
                            val paramCount = configParams.size
                            val matchMethods = suitableMethods?.filter { it -> it.parameterCount == paramCount }
                            val method = matchMethods!![0]
                            val caseName = cacf.name
                            if (!matchMethods.isNullOrEmpty()) {
                                val mv = cw.visitMethod(Opcodes.ACC_PUBLIC, caseName, "()V", null, null)
                                val av = mv.visitAnnotation("Lorg/junit/Test;", true)
                                av.visitEnd()
                                mv.visitCode()
                                if ((method.modifiers and Opcodes.ACC_STATIC) != 0) {
                                    StaticMethodGenerator(mv, clz, method, configParams).generate()
                                } else if (ccf.autowire) {
                                    SpringBeanMethodGenerator(mv, clz, method, slashedClassName, ccf.appName, configParams)
                                        .generate()
                                } else {
                                    NonStaticMethodGenerator(mv, clz, method, configParams).generate()
                                }
                                mv.visitEnd()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                cw.visitEnd()
                val byteArray = cw.toByteArray()
                writeClass(targetClassPath, byteArray)
            }
        }
        decompileClass(outputPath, outputClassPath)
    }

    fun decompileClass(outputPath : String, outputClassPath : String) {
        makeSureDirectoryExist(outputPath)
        ConsoleDecompiler.main(arrayOf(outputClassPath, outputPath))
    }

    fun writeClass(targetClassPath: String, byteArray: ByteArray) {
        val classDir = targetClassPath.substring(0, targetClassPath.lastIndexOf('/'))
        makeSureDirectoryExist(classDir)
        Files.write(Paths.get(targetClassPath), byteArray)
    }

    fun makeSureDirectoryExist(dir: String) {
        if (Files.exists(Paths.get(dir))) return
        Files.createDirectories(Paths.get(dir))
    }
}