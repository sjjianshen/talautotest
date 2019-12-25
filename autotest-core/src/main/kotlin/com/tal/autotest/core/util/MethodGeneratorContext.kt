package com.tal.autotest.core.util

import org.objectweb.asm.ClassWriter

data class MethodGeneratorContext(
    val ccf: ClassConfig,
    val mcf: MethodConfig,
    val cw: ClassWriter,
    val clz: Class<*>
)