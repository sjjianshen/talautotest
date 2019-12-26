package com.tal.autotest.core

import com.tal.autotest.core.util.AutotestContext
import com.tal.autotest.core.util.DirectoryClassLoader

fun main(args: Array<String>) {
    val workspace = args[0]
    val classpath = args[1] // mvn: ${workspace}/target/classes, gradle: ${workspace}/build/classes/java/main
    val resourcePath = args[2] // mvn: ${workspace}/target/classes, gradle: ${workspace}/build/resources/main
    val outputPath = "${workspace}/src/test/java"
    val configPath = "$workspace/autotest"
    val outputClassPath = "$workspace/build/autotest/classes"
    val atc = AutotestContext(workspace, configPath, outputPath, outputClassPath)
    val current = Thread.currentThread().contextClassLoader
    val targetClassLoader = DirectoryClassLoader(classpath, resourcePath, current)
    Thread.currentThread().contextClassLoader = targetClassLoader
    try {
        AutotestGeneratorEngine(atc).launch()
    } catch (e : Exception) {
        e.printStackTrace()
        System.exit(1)
    } finally {
        Thread.currentThread().contextClassLoader = current
    }
    System.exit(0)
}