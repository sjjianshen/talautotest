package com.tal.autotest.core

import com.tal.autotest.core.generateor.clz.TestClassGenerator
import com.tal.autotest.core.util.AutotestContext
import com.tal.autotest.core.util.FileSystemUtil
import com.tal.autotest.core.util.InputConfig
import kotlinx.serialization.json.Json
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class AutotestGeneratorEngine(val autoTestContext: AutotestContext) {
    val outputPath = autoTestContext.outputPath
    val outputClassPath = autoTestContext.outputClassPath
    val configPath = autoTestContext.ConfigFile
    public fun launch() {
        if (!Files.exists(Paths.get(configPath))) {
            System.out.println("没有发现配置文件")
            return
        }
        val config = Json.parse(InputConfig.serializer(), File(configPath).readText())
        config.classConfigs.forEach {
            if (it.autowire) {

            } else {
                TestClassGenerator(it, autoTestContext).generateTestClass()
            }
        }
        decompileClass(outputPath, outputClassPath)
    }

    fun decompileClass(outputPath : String, outputClassPath : String) {
        FileSystemUtil.makeSureDirectoryExist(outputPath)
        ConsoleDecompiler.main(arrayOf(outputClassPath, outputPath))
    }
}