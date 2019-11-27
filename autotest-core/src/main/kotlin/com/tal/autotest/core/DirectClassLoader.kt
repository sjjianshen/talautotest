package com.tal.autotest.core

import sun.misc.CompoundEnumeration
import java.io.File
import java.io.InputStream
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import java.security.AccessController
import java.security.PrivilegedAction
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DirectoryClassLoader(
    workspace: String,
    urls: Array<URL>,
    projectType: String,
    val oldCl: ClassLoader
) : URLClassLoader(urls, oldCl) {
    private var classpath = if (projectType.equals("mvn")) "${workspace}/target/classes" else "${workspace}/build/classes/java/main"
    private var resourcePath = if (projectType.equals("mvn")) "${workspace}/target/classes" else "${workspace}/build/resources/main"
    private var cache: HashMap<String, Class<*>?> = HashMap()
    private val auxPaths = mutableListOf<File>()

    @Synchronized
    override fun loadClass(name: String): Class<*>? {
        if (name.contains("XPathConstants")) {
            System.out.println("333")
        }
        if (name.contains("QName")) {
            System.out.println("222")
        }
        if (cache.containsKey(name)) {
            return cache[name]
        }

        var packageName: String? = null
        val pos = name.lastIndexOf('.')
        if (pos != -1)
            packageName = name.substring(0, pos)
        if (packageName != null) {
            // Define the package (if null)
            if (getPackage(packageName) == null) {
                try {
                    definePackage(packageName, null, null, null, null, null, null, null)
                } catch (e: IllegalArgumentException) {
                }
                getPackage(packageName)
            }
        }
        var res: Class<*>? = null
        if (!name.startsWith("javax.xml")) {
            try {
                res = super.findClass(name)
            } catch (e: Throwable) {
            }
            if (res != null) {
                cache[name] = res
                return res
            }
        }

        try {
            val data: ByteArray = loadClassData(name)
            res = defineClass(name, data, 0, data.size)
            resolveClass(res)
        } catch (e: Exception) {
        }
        if (res != null) {
            cache[name] = res
            return res
        }

        try {
            res = oldCl.loadClass(name)
        } catch (e: Throwable) {
        }
        if (res != null) {
            cache[name] = res
            return res
        }

        throw ClassNotFoundException(name)
    }

    private fun loadClassData(name: String): ByteArray {
        val slashedClassName = name.replace('.', '/')
//        if (slashedClassName.contains("org/springframework/validation/beanvalidation/LocalValidatorFactoryBean")) {
//            System.out.println("")
//        }
        return File("$classpath/$slashedClassName.class").readBytes()
    }


    public fun loadClassDirect(name: String, byteArray: ByteArray): Class<*> {
        return defineClass(name, byteArray, 0, byteArray.size)
    }

    fun addAuxClassPath(path: String) {
        auxPaths.add(File(path))
    }

    //    @Throws(IOException::class)
    override fun findResources(name: String): Enumeration<URL> {
        if (name.isEmpty()) {
            throw ClassNotFoundException(name)
        }
        var pUrls: Enumeration<URL> = this.parent.getResources(name)
        val sUrls = super.findResources(name)
        val urlList: ArrayList<URL> = ArrayList()
        addResourceToList(File("${resourcePath}/${name}"), urlList)
        addResourceToList(File("${classpath}/${name}"), urlList)

        var oUrls = ListEnumeration(urlList)
        return CompoundEnumeration<URL>(arrayOf(pUrls, sUrls, oUrls))
    }

    private fun addResourceToList(rfile: File, urlList: ArrayList<URL>) {
        try {
            if (rfile.exists()) {
                urlList.add(rfile.toURI().toURL())
            }
        } catch (e: Exception) {
        }
    }

    internal class ListEnumeration(var list: List<URL>) : Enumeration<URL> {
        var i: Int = 0

        init {
            this.i = 0
        }

        override fun hasMoreElements(): Boolean {
            synchronized(this.list) {
                return this.i < this.list.size
            }
        }

        override fun nextElement(): URL {
            synchronized(this.list) {
                return if (this.i < this.list.size) {
                    this.list.get(this.i++)
                } else {
                    throw NoSuchElementException("VectorEnumerator")
                }
            }
        }
    }

    override fun getResourceAsStream(name: String): InputStream? {
        var ins: InputStream? = this.parent.getResourceAsStream(name)
        if (ins != null) {
            return ins
        }
        ins = super.getResourceAsStream(name)
        if (ins != null) {
            return ins
        }
        ins = AccessController.doPrivileged(
            PrivilegedAction {
                if (Files.exists(Paths.get("${resourcePath}/${name}"))) {
                    Files.newInputStream(Paths.get("${resourcePath}/${name}"))
                } else if (Files.exists(Paths.get("${classpath}/${name}"))) {
                    Files.newInputStream(Paths.get("${classpath}/${name}"))
                } else {
                    null
                }
            }, AccessController.getContext()
        )
        return ins
    }

    override fun getResource(name: String): URL? {
        var url: URL? = parent.getResource(name)
        if (url != null) {
            return url
        }
        url = super.getResource(name)
        if (url != null) {
            return url
        }
        if (url == null) {
            url = findResource(name)
        }
        return url
    }

    override fun findResource(name: String): URL? {
        if (name.isEmpty()) {
            throw ClassNotFoundException(name)
        }
        var url = super.findResource(name)
        if (url != null) {
            return url
        }
        var file = File("${resourcePath}/${name}")
        if (file.exists() && file.isFile) {
            return file.toURI().toURL()
        }
        if (file.exists() && file.isFile) {
            return file.toURI().toURL()
        }
        return null
    }
}