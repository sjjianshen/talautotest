package com.tal

import java.io.File

class DirectoryClassLoader (private val classpath: String, private val oldCl: ClassLoader) : ClassLoader(oldCl) {
    private var cache: HashMap<String, Class<*>> = HashMap()
    @Synchronized override fun loadClass(name: String?): Class<*>? {
        if (name.isNullOrBlank()) {
            return null
        }
        if (cache.containsKey(name)) {
            return cache[name]
        }
        var res: Class<*>
        try {
            res = oldCl.loadClass(name)
        } catch (e : ClassNotFoundException) {
            val data: ByteArray = loadClassData(name)
            res = defineClass(name, data, 0, data.size)
            resolveClass(res)
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
}