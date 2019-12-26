package com.tal.autotest.core

import com.tal.autotest.core.generateor.clz.SpringTestClassGenerator
import com.tal.autotest.core.generateor.clz.TestClassGenerator
import com.tal.autotest.core.util.AutotestContext
import com.tal.autotest.core.util.FileSystemUtil
import com.tal.autotest.core.util.InputConfig
import com.tal.autotest.runtime.instrument.InstrumentAgent
import com.tal.autotest.runtime.instrument.InstrumentAgentLoader
import kotlinx.serialization.json.Json
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class AutotestGeneratorEngine(private val autoTestContext: AutotestContext) {
    private val outputPath = autoTestContext.outputPath
    private val outputClassPath = autoTestContext.outputClassPath
    private val configPath = autoTestContext.ConfigFile
    fun launch() {
        if (!Files.exists(Paths.get(configPath))) {
            println("没有发现配置文件")
            return
        }
        val config = Json.parse(InputConfig.serializer(), File(configPath).readText())
        InstrumentAgentLoader.initialize()
        InstrumentAgent.inActive()
        config.classConfigs.forEach {
            try {
                if (it.autowire) {
                    SpringTestClassGenerator(it, autoTestContext).generateTestClass()
                } else {
                    TestClassGenerator(it, autoTestContext).generateTestClass()
                }
            } catch (e : Exception) {
                print(e.message)
            }
        }
        decompileClass(outputPath, outputClassPath)
    }

    private fun decompileClass(outputPath : String, outputClassPath : String) {
        FileSystemUtil.makeSureDirectoryExist(outputPath)
        ConsoleDecompiler.main(arrayOf(outputClassPath, outputPath))
    }
}