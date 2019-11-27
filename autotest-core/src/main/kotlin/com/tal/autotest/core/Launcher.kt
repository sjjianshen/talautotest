package com.tal.autotest.core

class Launcher {
    public fun launch(args : Array<String>) {
        val workspace = args[0]
        val projectType = args[1]
        val outputPath = args[2]
        val outputClassPath = args[3]
        val configFile = args[4]
        val atc = AutotestContext(projectType, workspace, configFile, outputPath, outputClassPath)
        ClassGenerator(atc).launch()
    }
}