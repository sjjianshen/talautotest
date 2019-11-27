package com.tal.autotest.core

import java.net.URL

fun main(args: Array<String>) {
    val workspace = args[0]
    val projectType = "gradle"
    val outputPath = "${workspace}/src/test/java"
    val outputClassPath = "$workspace/build/autotest/classes"
    val configFile = "$workspace/autotest/config.json"
    val urls = mutableListOf<URL>()
    for (i in 1 until args.size) {
        if (args[i].endsWith(".jar")) {
            urls.add(URL("file://${args[i]}"))
        }
    }
    val atc = AutotestContext(projectType, workspace, configFile, outputPath, outputClassPath)
    val oldCl = Thread.currentThread().contextClassLoader
    val targetClassLoader = DirectoryClassLoader(
        workspace, urls.toTypedArray(), "gradle",
        oldCl
    )
    Thread.currentThread().contextClassLoader = targetClassLoader
    ClassGenerator(atc).launch()
    Thread.currentThread().contextClassLoader = oldCl
}