package com.tal.autotest.core

import java.net.URL

fun main(args: Array<String>) {
    val workspace = args[0]
    val urls = mutableListOf<URL>()
    for (i in 1 until args.size) {
        if (args[i].endsWith(".jar")) {
            urls.add(URL("file://${args[i]}"))
        }
    }
    ClassGenerator(workspace, urls.toTypedArray()).launch()
}