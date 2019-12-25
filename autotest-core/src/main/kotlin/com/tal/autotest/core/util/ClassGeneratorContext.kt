package com.tal.autotest.core.util

import org.objectweb.asm.ClassWriter

data class ClassGeneratorContext(
    val ccf: ClassConfig,
    val cw: ClassWriter,
    val clz: Class<*>,
    val ctx : AutotestContext
)