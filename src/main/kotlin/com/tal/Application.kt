package com.tal

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val workspace = args[0]
    val configPath = "$workspace/talTester/config.json"
    val config = Json.parse(InputConfig.serializer(), File(configPath).readText())
    val javaFilePath = "$workspace/src/main/java"
    val javaFiles = Files.walk(Paths.get(javaFilePath)).filter { t -> t.toString().endsWith(".java") }
    val classFilePath = "$workspace/build/classes"
    val classFiles = Files.walk(Paths.get(classFilePath)).filter { t -> t.toString().endsWith(".class") }
    val outputPath = "$workspace/src/test/java"
    val outputClassPath = "$workspace/build/talTester/classes"
    val targetClassLoader = DirectoryClassLoader(classFilePath, Thread.currentThread().contextClassLoader)
    Thread.currentThread().contextClassLoader = targetClassLoader
    config.classConfigs.forEach {
        val ccf = it
        val className = ccf.name
        val targetClassName = "${className}Test"
        val slashedClassName = targetClassName.replace(".", "/")
        val targetName = "$outputPath/${slashedClassName}.java"
        val targetClassPath = "${outputClassPath}/${slashedClassName}.class"
        if (Files.exists(Paths.get(targetName))) {
            val clz = targetClassLoader.loadClass(className)
            val methods = clz?.methods
            val cw = ClassWriter(0)
            cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, slashedClassName, null, "java/lang/Object", null)
            ccf.methodConfigs.forEach {
                val mcf = it
                val methodName = mcf.name
                val suitableMethods = methods?.filter {
                    it.name == methodName
                }
                mcf.cases.forEach {
                    val cacf = it
                    val configParams = cacf.params
                    val paramCount = configParams.size
                    val matchMethods = suitableMethods?.filter { it.parameterCount == paramCount }
                    val match = matchMethods!![0]
                    val caname = cacf.name
                    if (!matchMethods.isNullOrEmpty()) {
                        val mv = cw.visitMethod(ACC_PUBLIC, caname, "()V", null, null)
                        val av = mv.visitAnnotation("Lorg/junit/Test;",true)
                        av.visitEnd()
                        mv.visitCode()
                        if ((match.modifiers and ACC_STATIC) != 0) {
                            createTestForStaticMethod(mv, className, match, configParams)
                        } else {
                            createTestForNonStaticMethod(mv, className, clz, match, configParams)
                        }
                    }
                }
            }
            cw.visitEnd()
            val byteArray = cw.toByteArray()
            writeClass(targetClassPath, byteArray)
        }
    }
    decompileClass(outputPath, outputClassPath)
}

fun createTestForStaticMethod(
    mv: MethodVisitor,
    className: String?,
    match: Method,
    configParams: List<JsonObject>
) {
    val list = processParams(match, configParams, mv)
    val ret = match.invoke(null, *list.toArray())
    val methodDesc = processMethodDesc(match)
    mv.visitMethodInsn(INVOKESTATIC, className?.replace('.', '/'), match.name, methodDesc,false)
    mv.visitIntInsn(ISTORE, 1)
    mv.visitVarInsn(ILOAD, 1)
    processReturn(match, mv, ret)
    mv.visitInsn(RETURN)
    mv.visitMaxs(3, 3)
    mv.visitEnd()
}

fun decompileClass(outputPath : String, outputClassPath : String) {
    makeSureDirectoryExist(outputPath)
    ConsoleDecompiler.main(arrayOf(outputClassPath, outputPath))
}

private fun createTestForNonStaticMethod(
    mv: MethodVisitor,
    className: String?,
    clz : Class<*>,
    match: Method,
    configParams: List<JsonObject>
) {
    mv.visitTypeInsn(NEW, className?.replace('.', '/'))
    mv.visitInsn(DUP)
    mv.visitMethodInsn(INVOKESPECIAL, className?.replace('.', '/'), "<init>", "()V", false)
    mv.visitVarInsn(ASTORE, 1)
    mv.visitVarInsn(ALOAD, 1)
    val list = processParams(match, configParams, mv)
    val methodDesc = processMethodDesc(match)
    mv.visitMethodInsn(INVOKEVIRTUAL, className?.replace('.', '/'), match.name, methodDesc,false)
    mv.visitIntInsn(ISTORE, 2)
    mv.visitVarInsn(ILOAD, 2)
    val obj = clz.newInstance()
    val ret = match.invoke(obj, *list.toArray())
    processReturn(match, mv, ret)
    mv.visitInsn(RETURN)
    mv.visitMaxs(4, 4)
    mv.visitEnd()
}

private fun processParams(
    match: Method,
    configParams: List<JsonObject>,
    mv: MethodVisitor
): ArrayList<Any> {
    val list = ArrayList<Any>()
    if (match.parameterCount > 0) {
        match.parameters.forEachIndexed { index, it ->
            val given = configParams?.get(index)?.get("value")
            if (it.type.isPrimitive) {
                val value: Any = processPrimitive(given, it, mv)
                list.add(value)
            } else {
                val value = processObject(it, given, mv)
                list.add(value)
            }
        }
    }
    return list
}

private fun processPrimitive(
    given: JsonElement?,
    it: Parameter,
    mv: MethodVisitor
): Any {
    val givenContent = given?.content
    val value: Any = when (it.type) {
        Byte::class.javaPrimitiveType -> givenContent?.toByte()
        Char::class.javaPrimitiveType -> givenContent?.toCharArray()?.first()
        Double::class.javaPrimitiveType -> givenContent?.toDouble()
        Float::class.javaPrimitiveType -> givenContent?.toFloat()
        Int::class.javaPrimitiveType -> givenContent?.toInt()
        Long::class.javaPrimitiveType -> givenContent?.toLong()
        Short::class.javaPrimitiveType -> givenContent?.toShort()
        Boolean::class.javaPrimitiveType -> givenContent?.toBoolean()
        String::class.java -> givenContent
        else -> null
    } as Any
    when (it.type) {
        Byte::class.javaPrimitiveType -> mv.visitIntInsn(SIPUSH, (value as Byte).toInt())
        Char::class.javaPrimitiveType -> mv.visitIntInsn(SIPUSH, (value as Char).toInt())
        Double::class.javaPrimitiveType -> mv.visitLdcInsn(value)
        Float::class.javaPrimitiveType -> mv.visitLdcInsn(value)
        Int::class.javaPrimitiveType -> mv.visitIntInsn(SIPUSH, value as Int)
        Long::class.javaPrimitiveType -> mv.visitLdcInsn(value)
        Short::class.javaPrimitiveType -> mv.visitIntInsn(SIPUSH, (value as Short).toInt())
        Boolean::class.javaPrimitiveType -> mv.visitInsn(if (value as Boolean) ICONST_1 else ICONST_0)
        String::class.java -> mv.visitLdcInsn(value)
        else -> mv.visitInsn(ACONST_NULL)
    }
    return value
}

private fun processObject(
    it: Parameter,
    params: JsonElement?,
    mv: MethodVisitor
) : Any {
    val ins = it.type.newInstance()
    mv.visitTypeInsn(NEW, it.name?.replace('.', '/'))
    mv.visitInsn(DUP)
    mv.visitMethodInsn(INVOKESPECIAL, it.name?.replace('.', '/'), "<init>", "()V", false)
    if (params != null) {
        it.type.fields.forEach {
            val name = it.name
//            it.
        }
    }
    return ins;
}

private fun processReturn(match: Method, mv: MethodVisitor, ret: Any?) {
    when (match.returnType) {
        Byte::class.javaPrimitiveType -> {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(I)Ljava/lang/Byte;", false)
            mv.visitIntInsn(SIPUSH, ret as Int)
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(I)Ljava/lang/Byte;", false)
        }
        Char::class.javaPrimitiveType -> {
            mv.visitIntInsn(SIPUSH, ret as Int)
        }
        Double::class.javaPrimitiveType -> {
            mv.visitLdcInsn(ret)
        }
        Float::class.javaPrimitiveType -> {
            mv.visitLdcInsn(ret)
        }
        Int::class.javaPrimitiveType -> {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
            mv.visitIntInsn(SIPUSH, ret as Int)
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
        }
        Long::class.javaPrimitiveType -> {
            mv.visitLdcInsn(ret)
        }
        Short::class.javaPrimitiveType -> {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(I)Ljava/lang/Short;", false)
            mv.visitIntInsn(SIPUSH, ret as Int)
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(I)Ljava/lang/Short;", false)
        }
        Boolean::class.javaPrimitiveType -> {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(I)Ljava/lang/Boolean;", false)
            mv.visitIntInsn(SIPUSH, ret as Int)
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(I)Ljava/lang/Boolean;", false)
        }
        String::class.java -> {
            mv.visitLdcInsn(ret)
        }
        else -> null
    }
    mv.visitMethodInsn(INVOKESTATIC, "org/hamcrest/core/Is", "is", "(Ljava/lang/Object;)Lorg/hamcrest/Matcher;", false)
    mv.visitMethodInsn(INVOKESTATIC, "org/hamcrest/MatcherAssert", "assertThat", "(Ljava/lang/Object;Lorg/hamcrest/Matcher;)V", false)
}

fun processMethodDesc(match: Method): String {
    var res = StringBuilder("(")
    match.parameters?.forEach {
        val type = it.type
        res.append(mapTypeToDesc(type))
    }
    res.append(")")
    res.append(mapTypeToDesc(match.returnType))
    return res.toString()
}

private fun mapTypeToDesc(type: Class<*>): String {
    if (type.isPrimitive()) {
        return when(type) {
            Byte::class.javaPrimitiveType -> "B"
            Char::class.javaPrimitiveType -> "C"
            Double::class.javaPrimitiveType -> "D"
            Float::class.javaPrimitiveType -> "F"
            Int::class.javaPrimitiveType -> "I"
            Long::class.javaPrimitiveType -> "J"
            Short::class.javaPrimitiveType -> "S"
            Boolean::class.javaPrimitiveType -> "Z"
            Void.TYPE -> "V"
            else -> throw RuntimeException("Unrecognized primitive $type")
        }
    }
    return if (type.isArray()) type.getName().replace('.', '/') else ('L' + type.getName() + ';').replace('.', '/')
}

fun writeClass(targetClassPath: String, byteArray: ByteArray) {
    val classDir = targetClassPath.substring(0, targetClassPath.lastIndexOf('/'))
    makeSureDirectoryExist(classDir)
    Files.write(Paths.get(targetClassPath), byteArray)
}

fun makeSureDirectoryExist(dir: String) {
    if (Files.exists(Paths.get(dir))) return
    Files.createDirectories(Paths.get(dir))
}