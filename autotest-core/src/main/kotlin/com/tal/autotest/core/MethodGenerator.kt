package com.tal.autotest.core

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import java.lang.reflect.Method

abstract class MethodGenerator {
    internal var varCounter = 0
    abstract fun generate()

    fun processParams(
        match: Method,
        configParams: List<JsonObject>
    ): ArrayList<Any?> {
        val list = ArrayList<Any?>()
        match.parameters.forEachIndexed { index, it ->
            val given = configParams.get(index).get("value")
            if (isBasicType(it.type)) {
                list.add(processPrimitive(given, it.type))
            } else if (it.type.isArray) {
                list.add(if (given?.jsonArray != null) processArray(given.jsonArray, it.type) else null)
            } else if (match.returnType.isAssignableFrom(List::class.java)) {
                list.add(if (given?.jsonArray != null) processList(given.jsonArray, it.type) else null)
            } else {
                list.add(processObject(given?.jsonObject, it.type))
            }
        }
        return list
    }

    private fun processArray(jsonArray: JsonArray, type: Class<*>): Array<Any?> {
        return processList(jsonArray, type).toTypedArray()
    }

    private fun processList(
        jsonArray: JsonArray,
        type: Class<*>
    ): MutableList<Any?> {
        var res = mutableListOf<Any?>()
        jsonArray.forEach {
            res.add(processObject(it.jsonObject, type.componentType))
        }
        return res
    }


    private fun processObject(
        params: JsonObject?,
        type: Class<*>
    ): Any? {
        if (params == null) {
            return null
        }
        val ins = type.newInstance()
        val methods = type.methods
        type.declaredFields.forEach {
            val name = it.name
            val given = params[name] ?: return@forEach
            val setMethodName = "set${name}"
            val setMethod: Method? = methods.find { it.name.equals(setMethodName, true) }
            if (setMethod != null) {
                setMethod.invoke(
                    ins,
                    if (isBasicType(it.type)) processPrimitive(given, it.type) else processObject(
                        given.jsonObject,
                        it.type
                    )
                )
            } else if (it.modifiers and ACC_PUBLIC != 0) {
                it.set(
                    ins, if (isBasicType(it.type)) processPrimitive(given, it.type) else processObject(
                        given.jsonObject,
                        it.type
                    )
                )
            }
        }
        return ins
    }

    fun addParamsByteCode(
        match: Method,
        configParams: List<JsonObject>,
        list: java.util.ArrayList<Any?>,
        mv: MethodVisitor
    ) {
        match.parameters.forEachIndexed { index, parameter ->
            if (isBasicType(parameter.type)) {
                addBasicByteCode(parameter.type, list.get(index), mv)
            } else {
                addObjectParamByteCode(parameter.type, configParams.get(index), list.get(index), mv)
            }
        }
    }

    fun addObjectParamByteCode(type: Class<*>, config: JsonObject, ins: Any?, mv: MethodVisitor) {
        if(ins == null) {
            return
        }
        mv.visitTypeInsn(NEW, type.name.replace('.', '/'))
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKESPECIAL, type.name.replace('.', '/'), "<init>", "()V", false)
        varCounter++
        val oldCounter = varCounter
        mv.visitVarInsn(ASTORE, varCounter)
        val methods = type.methods
        type.declaredFields.forEach {
            it.setAccessible(true)
            val paramName = it.name
            val ele = config[paramName]
            if (isDefault(it.type, it.get(ins))) {
                return@forEach
            }
            val setMethodName = "set${paramName}"
            val setMethod: Method? = methods.find { it.name.equals(setMethodName, true) }
            if (ele != null && setMethod != null) {
                mv.visitVarInsn(ALOAD, varCounter)
                if (isBasicType(it.type)) {
                    addBasicByteCode(it.type, it.get(ins), mv)
                } else {
                    addObjectParamByteCode(it.type, ele.jsonObject, it.get(ins), mv)
                }
                mv.visitMethodInsn(
                    INVOKEVIRTUAL, type.name.replace('.', '/'), setMethod.name,
                    processMethodDesc(setMethod), false
                )
            } else if (ele != null && (it.modifiers and ACC_PUBLIC != 0)) {
                mv.visitVarInsn(ALOAD, varCounter)
                if (isBasicType(it.type)) {
                    addBasicByteCode(it.type, it.get(ins), mv)
                } else {
                    addObjectParamByteCode(it.type, ele.jsonObject, it.get(ins), mv)
                }
                mv.visitFieldInsn(PUTFIELD, type.name.replace('.', '/'), paramName, mapTypeToDesc(it.type))
            }
        }
        mv.visitVarInsn(ALOAD, oldCounter)
    }

    fun processVerifyByteCode(match: Method, mv: MethodVisitor, ret: Any?) {
        varCounter++
        mv.visitIntInsn(ISTORE, varCounter)
        mv.visitVarInsn(ILOAD, varCounter)
        if (isBasicType(match.returnType)) {
            verifyBasic(match.returnType, ret, mv)
        } else if(match.returnType.isArray) {
            veryfyArraySize(ret, mv)
        } else if (match.returnType.isAssignableFrom(List::class.java)) {
            verifyListSize(ret, mv)

        } else {
            veryfyObject(match.returnType, mv, ret)
        }
    }

    fun veryfyArraySize(ret: Any?, mv: MethodVisitor) {
        if (ret != null) {
            val count = (ret as Array<*>).size
            mv.visitInsn(ARRAYLENGTH)
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
            mv.visitIntInsn(BIPUSH, count)
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
            addVerifyByteCode(mv)
        }
    }

    fun verifyListSize(ret: Any?, mv: MethodVisitor) {
        if (ret != null) {
            val count = (ret as List<*>).size
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true)
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
            mv.visitIntInsn(BIPUSH, count)
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
            addVerifyByteCode(mv)
        }
    }

    private fun veryfyObject(type: Class<*>, mv: MethodVisitor, ret: Any?) {
        val methods = type.methods
        if (ret == null) {
            verifyBasic(type, ret, mv)
        }
        type.declaredFields.forEach {
            if (!isBasicType(it.type)) {
                return@forEach
            }
            it.setAccessible(true)
            val name = it.name
            val setMethodName = "get${name}"
            val getMethod: Method? = methods.find { it.name.equals(setMethodName, true) }
            val value = it.get(ret)
            if (value != null && getMethod != null) {
                mv.visitVarInsn(ALOAD, varCounter)
                mv.visitMethodInsn(
                    INVOKESPECIAL, type.name.replace('.', '/'), getMethod.name,
                    processMethodDesc(getMethod), false
                )
                verifyBasic(it.type, value, mv)
            } else if (it.modifiers and ACC_PUBLIC != 0) {
                mv.visitVarInsn(ALOAD, varCounter)
                mv.visitFieldInsn(
                    GETFIELD,
                    type.name.replace('.', '/'),
                    it.name,
                    mapTypeToDesc(it.type)
                )
                verifyBasic(it.type, value, mv)
            }

        }
    }

    private fun isBasicType(type: Class<*>) = type.isPrimitive or (type == String::class.java)

    private fun isDefault(type: Class<*>?, value: Any?): Boolean {
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

    private fun verifyBasic(type: Class<*>, value: Any?, mv: MethodVisitor) {
        addBasicByteCode(type, value, mv)
        addVerifyByteCode(mv)
    }

    private fun addBasicByteCode(type: Class<*>, value: Any?, mv: MethodVisitor) {
        if (value == null) {
            mv.visitInsn(ACONST_NULL)
            return
        }

        when (type) {
            Byte::class.javaPrimitiveType -> {
                mv.visitIntInsn(SIPUSH, (value as Byte).toInt())
                mv.visitTypeInsn(CHECKCAST, "java/lang/Char")
            }
            Char::class.javaPrimitiveType -> mv.visitIntInsn(SIPUSH, (value as Char).toInt())
            Double::class.javaPrimitiveType -> {
                mv.visitLdcInsn(value)
                mv.visitTypeInsn(CHECKCAST, "java/lang/Double")
            }
            Float::class.javaPrimitiveType -> {
                mv.visitLdcInsn(value)
                mv.visitTypeInsn(CHECKCAST, "java/lang/Float")
            }
            Int::class.javaPrimitiveType -> {
                mv.visitLdcInsn(value)
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
            }
            Long::class.javaPrimitiveType -> {
                mv.visitLdcInsn(value)
                mv.visitTypeInsn(CHECKCAST, "java/lang/Long")
            }
            Short::class.javaPrimitiveType -> {
                mv.visitIntInsn(SIPUSH, (value as Short).toInt())
                mv.visitTypeInsn(CHECKCAST, "java/lang/Short")
            }
            Boolean::class.javaPrimitiveType -> {
                mv.visitInsn(if (value as Boolean) ICONST_1 else ICONST_0)
                mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean")
            }
            String::class.java -> {
                mv.visitLdcInsn(value)
            }
            else -> mv.visitInsn(ACONST_NULL)
        }
    }

    private fun addVerifyByteCode(mv: MethodVisitor) {
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
            String::class.java -> givenContent
            else -> null
        } as Any
    }

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
}