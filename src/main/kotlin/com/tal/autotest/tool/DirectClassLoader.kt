package com.tal.autotest.tool

import java.io.File
import java.net.URL
import java.net.URLClassLoader

class DirectoryClassLoader (private val classpath: String, urls : Array<URL>, private val oldCl: ClassLoader) : URLClassLoader(urls, oldCl) {
    private var cache: HashMap<String, Class<*>?> = HashMap()
    private val auxPaths = mutableListOf<File>()
    @Synchronized override fun loadClass(name: String): Class<*>? {
        if (name.contains("TypeConstrainedMappingJackson2HttpMessageConverter") || name.isNullOrBlank()) {
            return null
        }
        if (cache.containsKey(name)) {
            return cache[name]
        }
        var res: Class<*>? = null
        try {
            res = oldCl.loadClass(name)
        } catch (e : ClassNotFoundException) {
        }
        if (res == null) {
            try {
                res = super.loadClass(name)
            } catch (e : ClassNotFoundException) {
            }
        }
        if (res == null) {
            try {
                val data: ByteArray = loadClassData(name)
                res = defineClass(name, data, 0, data.size)
                resolveClass(res)
            } catch (e : Exception) {
                throw ClassNotFoundException()
            }

        }
        cache[name] = res
        return res
    }

    private fun loadClassData(name: String): ByteArray {
        val slashedClassName = name.replace('.', '/')
        return File("$classpath/$slashedClassName.class").readBytes()
    }

    public fun loadClassDirect(name: String, byteArray: ByteArray): Class<*> {
        return defineClass(name, byteArray, 0, byteArray.size)
    }

    fun addAuxClassPath(path: String) {
        auxPaths.add(File(path))
    }
}