package com.tal.autotest.core

data class AutotestContext(
    var projectType: String,
    var workSpace: String,
    var ConfigFile: String,
    var outputPath: String,
    var outputClassPath: String
)