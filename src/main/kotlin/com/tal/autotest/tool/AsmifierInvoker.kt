package com.tal.autotest.tool

import org.objectweb.asm.util.ASMifier

fun main(args: Array<String>) {
    ASMifier.main(arrayOf("com.tal.autotest.example.ListTestExample"))
}