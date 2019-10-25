package com.tal.autotest.tool

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import org.springframework.test.context.TestContextManager
import java.lang.reflect.Field
import java.lang.reflect.Method

abstract class MethodGenerator {
    internal var varCounter = 0
    abstract fun generate()

    fun postProcessPrimitive(returnType: Class<*>, mv: MethodVisitor) {
        when (returnType) {
            Byte::class.javaPrimitiveType -> mv.visitMethodInsn(
                INVOKESTATIC,
                "java/lang/Byte",
                "valueOf",
                "(I)Ljava/lang/Byte;",
                false
            )
            Int::class.javaPrimitiveType -> mv.visitMethodInsn(
                INVOKESTATIC, "java/lang/Integer", "valueOf",
                "(I)Ljava/lang/Integer;", false
            )
            Short::class.javaPrimitiveType -> {
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "java/lang/Short",
                    "valueOf",
                    "(I)Ljava/lang/Short;",
                    false
                )
            }
            Boolean::class.javaPrimitiveType -> {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean")
            }
            else -> null
        }
    }

    fun processParams(
        match: Method,
        configParams: List<JsonObject>
    ): ArrayList<Any> {
        val list = ArrayList<Any>()
        if (match.parameterCount > 0) {
            match.parameters.forEachIndexed { index, it ->
                val given = configParams.get(index).get("value")
                if (it.type.isPrimitive) {
                    val value: Any = processPrimitive(given, it.type)
                    list.add(value)
                } else if (it.type == String::class.java) {
                    val value: Any = given?.content as Any
                    list.add(value)
                } else {
                    val value = processObject(given?.jsonObject, it.type)
                    list.add(value)
                }
            }
        }
        return list
    }

    private fun processPrimitive(
        given: JsonElement?,
        type: Class<*>
    ): Any {
        val givenContent = given?.content
        return when (type) {
            Byte::class.javaPrimitiveType -> givenContent?.toByte()
            Char::class.javaPrimitiveType -> givenContent?.toCharArray()?.first()
            Double::class.javaPrimitiveType -> givenContent?.toDouble()
            Float::class.javaPrimitiveType -> givenContent?.toFloat()
            Int::class.javaPrimitiveType -> givenContent?.toInt()
            Long::class.javaPrimitiveType -> givenContent?.toLong()
            Short::class.javaPrimitiveType -> givenContent?.toShort()
            Boolean::class.javaPrimitiveType -> givenContent?.toBoolean()
            else -> null
        } as Any
    }

    private fun processObject(
        params: JsonObject?,
        type: Class<*>
    ): Any {
        val ins = type.newInstance()
        val methods = type.methods
        if (params != null) {
            type.declaredFields.forEach {
                val name = it.name
                val setMethodName = "set${name}"
                val setMethod: Method? = methods.find { it.name.equals(setMethodName, true) }
                val given = params[name]
                if (given != null) {
                    if (setMethod != null) {
                        if (it.type.isPrimitive) {
                            setMethod.invoke(ins, processPrimitive(given, it.type))
                        } else if (it.type == String::class.java) {
                            setMethod.invoke(ins, given.content)
                        } else {
                            setMethod.invoke(ins, processObject(given.jsonObject, it.type))
                        }
                    } else if (it.modifiers and ACC_PUBLIC != 0) {
                        if (it.type.isPrimitive) {
                            it.set(ins, processPrimitive(given, it.type))
                        } else {
                            it.set(ins, processObject(given.jsonObject, it.type))
                        }
                    }
                }
            }
        }
        return ins
    }

    fun addParamsByteCode(
        match: Method,
        configParams: List<JsonObject>,
        list: java.util.ArrayList<Any>,
        mv: MethodVisitor
    ) {
        if (match.parameterCount > 0) {
            match.parameters.forEachIndexed { index, parameter ->
                if (parameter.type.isPrimitive) {
                    processPrimitiveByteCode(parameter.type, list.get(index), mv)
                } else if (parameter.type == String::class.java) {
                    processStringByteCode(list.get(index), mv)
                } else {
                    processObjectByteCode(parameter.type, configParams.get(index), list.get(index), mv)
                }
            }
        }
    }

    fun processPrimitiveByteCode(type: Class<*>, value: Any, mv: MethodVisitor) {
        when (type) {
            Byte::class.javaPrimitiveType -> mv.visitIntInsn(SIPUSH, (value as Byte).toInt())
            Char::class.javaPrimitiveType -> mv.visitIntInsn(SIPUSH, (value as Char).toInt())
            Double::class.javaPrimitiveType -> mv.visitLdcInsn(value)
            Float::class.javaPrimitiveType -> mv.visitLdcInsn(value)
            Int::class.javaPrimitiveType -> mv.visitIntInsn(SIPUSH, value as Int)
            Long::class.javaPrimitiveType -> mv.visitLdcInsn(value)
            Short::class.javaPrimitiveType -> mv.visitIntInsn(SIPUSH, (value as Short).toInt())
            Boolean::class.javaPrimitiveType -> mv.visitInsn(if (value as Boolean) ICONST_1 else ICONST_0)
            else -> mv.visitInsn(ACONST_NULL)
        }
    }

    fun processObjectByteCode(type: Class<*>, config: JsonObject, ins: Any, mv: MethodVisitor) {
        mv.visitTypeInsn(NEW, type.name.replace('.', '/'))
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKESPECIAL, type.name.replace('.', '/'), "<init>", "()V", false)
        varCounter++
        val oldCounter = varCounter
        mv.visitVarInsn(ASTORE, varCounter)
        val methods = type.methods
        type.declaredFields.forEach {
            it.setAccessible(true)
            if (!isDefault(it.type, it.get(ins)) and (config[it.name] != null)) {
                val name = it.name
                val setMethodName = "set${name}"
                val setMethod: Method? = methods.find { it.name.equals(setMethodName, true) }
                if (setMethod != null) {
                    mv.visitVarInsn(ALOAD, varCounter)
                    if (it.type.isPrimitive) {
                        processPrimitiveByteCode(it.type, it.get(ins), mv)
                    } else if (it.type == String::class.java) {
                        processStringByteCode(it.get(ins), mv)
                    } else {
                        val fieldConfig = config[name]
                        if (fieldConfig != null) {
                            processObjectByteCode(it.type, fieldConfig.jsonObject, it.get(ins), mv)
                        }
                    }
                    mv.visitMethodInsn(
                        INVOKESPECIAL, type.name.replace('.', '/'), setMethod.name,
                        processMethodDesc(setMethod), false
                    )
                } else if (it.modifiers and ACC_PUBLIC != 0) {
                    mv.visitVarInsn(ALOAD, varCounter)
                    if (it.type.isPrimitive) {
                        processPrimitiveByteCode(it.type, it.get(ins), mv)
                    } else if (it.type == String::class.java) {
                        processStringByteCode(it.get(ins), mv)
                    } else {
                        val fieldConfig = config[name]
                        if (fieldConfig != null) {
                            processObjectByteCode(it.type, fieldConfig.jsonObject, it.get(ins), mv)
                        }
//                        processObjectByteCode(it.type, it.get(ins), mv)
                    }
                    mv.visitFieldInsn(PUTFIELD, type.name.replace('.', '/'), it.name, mapTypeToDesc(it.type))
                }
            }
        }
        mv.visitVarInsn(ALOAD, oldCounter)
    }

    fun processStringByteCode(value: Any?, mv: MethodVisitor) {
        mv.visitLdcInsn(value)
    }

    fun isDefault(type: Class<*>?, value: Any?): Boolean {
        return when (type) {
            Byte::class.javaPrimitiveType -> value as Byte == 0.toByte()
            Char::class.javaPrimitiveType -> value as Char == 0.toChar()
            Double::class.javaPrimitiveType -> value as Double == 0.toDouble()
            Float::class.javaPrimitiveType -> value as Float == 0.toFloat()
            Int::class.javaPrimitiveType -> value as Int == 0
            Long::class.javaPrimitiveType -> value as Long == 0.toLong()
            Short::class.javaPrimitiveType -> value as Short == 0.toShort()
            Boolean::class.javaPrimitiveType -> !(value as Boolean)
            else -> value == null
        }
    }

    fun processVerifyByteCode(match: Method, mv: MethodVisitor, ret: Any?) {
        varCounter++
        mv.visitIntInsn(ISTORE, varCounter)
        mv.visitVarInsn(ILOAD, varCounter)
        if (match.returnType.isPrimitive) {
            when (match.returnType) {
                Byte::class.javaPrimitiveType -> {
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
                    mv.visitIntInsn(SIPUSH, ret as Int)
                    mv.visitMethodInsn(
                        INVOKESTATIC,
                        "java/lang/Integer",
                        "valueOf",
                        "(I)Ljava/lang/Integer;",
                        false
                    )
                }
                Long::class.javaPrimitiveType -> {
                    mv.visitLdcInsn(ret)
                }
                Short::class.javaPrimitiveType -> {
                    mv.visitIntInsn(SIPUSH, ret as Int)
                    mv.visitMethodInsn(
                        INVOKESTATIC,
                        "java/lang/Short",
                        "valueOf",
                        "(I)Ljava/lang/Short;",
                        false
                    )
                }
                Boolean::class.javaPrimitiveType -> {
                    if (ret as Boolean) {
                        mv.visitInsn(ICONST_1)
                    } else {
                        mv.visitInsn(ICONST_0)
                    }
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean")
                }
                else -> null
            }
            mv.visitMethodInsn(
                INVOKESTATIC,
                "org/hamcrest/core/Is",
                "is",
                "(Ljava/lang/Object;)Lorg/hamcrest/Matcher;",
                false
            )
            mv.visitMethodInsn(
                INVOKESTATIC,
                "org/hamcrest/MatcherAssert",
                "assertThat",
                "(Ljava/lang/Object;Lorg/hamcrest/Matcher;)V",
                false
            )
        } else if (match.returnType == String::class.java) {
            mv.visitLdcInsn(ret)
            mv.visitMethodInsn(
                INVOKESTATIC,
                "org/hamcrest/core/Is",
                "is",
                "(Ljava/lang/Object;)Lorg/hamcrest/Matcher;",
                false
            )
            mv.visitMethodInsn(
                INVOKESTATIC,
                "org/hamcrest/MatcherAssert",
                "assertThat",
                "(Ljava/lang/Object;Lorg/hamcrest/Matcher;)V",
                false
            )
        } else {
            veryfyObject(match.returnType, mv, ret)
        }
    }

    private fun veryfyObject(type: Class<*>, mv: MethodVisitor, ret: Any?) {
        val methods = type.methods
        if (ret == null) {
            return
        }
        type.declaredFields.forEach {
            it.setAccessible(true)
            val name = it.name
            val setMethodName = "get${name}"
            val getMethod: Method? = methods.find { it.name.equals(setMethodName, true) }
            val value = it.get(ret)
            if (value != null) {
                if (getMethod != null) {
                    if (it.type.isPrimitive or (it.type == String::class.java)) {
                        mv.visitVarInsn(ALOAD, varCounter)
                        mv.visitMethodInsn(
                            INVOKESPECIAL, type.name.replace('.', '/'), getMethod.name,
                            processMethodDesc(getMethod), false
                        )
                        verifyField(it, value, mv)
                    }
                } else if (it.modifiers and ACC_PUBLIC != 0) {
                    if (it.type.isPrimitive or (it.type == String::class.java)) {
                        mv.visitVarInsn(ALOAD, varCounter)
                        mv.visitFieldInsn(
                            GETFIELD,
                            type.name.replace('.', '/'),
                            it.name,
                            mapTypeToDesc(it.type)
                        )
                        verifyField(it, value, mv)
                    }
                }
            }
        }
    }

    private fun verifyField(it: Field, value: Any, mv: MethodVisitor) {
        if (it.type.isPrimitive) {
            processPrimitiveByteCode(it.type, value, mv)
        } else {
            processStringByteCode(value, mv)
        }
        mv.visitMethodInsn(
            INVOKESTATIC,
            "org/hamcrest/core/Is",
            "is",
            "(Ljava/lang/Object;)Lorg/hamcrest/Matcher;",
            false
        )
        mv.visitMethodInsn(
            INVOKESTATIC,
            "org/hamcrest/MatcherAssert",
            "assertThat",
            "(Ljava/lang/Object;Lorg/hamcrest/Matcher;)V",
            false
        )
    }

    fun processMethodDesc(match: Method): String {
        var res = StringBuilder("(")
        match.parameters.forEach {
            val type = it.type
            res.append(mapTypeToDesc(type))
        }
        res.append(")")
        res.append(mapTypeToDesc(match.returnType))
        return res.toString()
    }

    private fun mapTypeToDesc(type: Class<*>): String {
        if (type.isPrimitive()) {
            return when (type) {
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
        return if (type.isArray()) type.getName().replace('.', '/') else ('L' + type.getName() + ';').replace(
            '.',
            '/'
        )
    }
}