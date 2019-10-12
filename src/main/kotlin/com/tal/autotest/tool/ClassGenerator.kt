package com.tal.autotest.tool

import kotlinx.serialization.json.Json
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

class ClassGenerator(val workspace: String, val urls: Array<URL>) {
    public fun launch() {
        val configPath = "$workspace/talTester/config.json"
        val config = Json.parse(InputConfig.serializer(), File(configPath).readText())
        val javaFilePath = "$workspace/src/main/java"
        val javaFiles = Files.walk(Paths.get(javaFilePath)).filter { t -> t.toString().endsWith(".java") }
        val classFilePath = "$workspace/build/classes/java/main"
        val classFiles = Files.walk(Paths.get(classFilePath)).filter { t -> t.toString().endsWith(".class") }
        val outputPath = "$workspace/src/test/java"
        val outputClassPath = "$workspace/build/talTester/classes"
        val targetClassLoader =
            DirectoryClassLoader(classFilePath, urls, Thread.currentThread().contextClassLoader)
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
                ccf.methodConfigs.forEach {
                    val mcf = it
                    val methodName = mcf.name
                    val suitableMethods = methods?.filter {
                        it.name == methodName
                    }
                    mcf.cases.forEach {
                        try {
                            val cacf = it
                            val configParams = cacf.params
                            val paramCount = configParams.size
                            val matchMethods = suitableMethods?.filter { it.parameterCount == paramCount }
                            val match = matchMethods!![0]
                            val caseName = cacf.name
                            if (!matchMethods.isNullOrEmpty()) {
                                MethodGenerator(cw, caseName, clz, match, configParams).generate()
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