package com.tal.autotest.core.generateor.testcase

import com.google.gson.Gson
import kotlinx.serialization.json.*
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
import java.lang.reflect.Method
import java.lang.reflect.Type

abstract class AbstractTestCaseGenerator {
    internal var varCounter = 0
    val componentBoxedTypes = arrayListOf<String>()
    val componentPrimitiveTypes = arrayListOf<String>()

    init {
        componentBoxedTypes.add("java.lang.Boolean")
        componentBoxedTypes.add("java.lang.Byte")
        componentBoxedTypes.add("java.lang.Short")
        componentBoxedTypes.add("java.lang.Integer")
        componentBoxedTypes.add("java.lang.Float")
        componentBoxedTypes.add("java.lang.Double")
        componentBoxedTypes.add("java.lang.Long")
        componentPrimitiveTypes.add("int")
        componentPrimitiveTypes.add("char")
        componentPrimitiveTypes.add("boolean")
        componentPrimitiveTypes.add("long")
        componentPrimitiveTypes.add("short")
        componentPrimitiveTypes.add("float")
        componentPrimitiveTypes.add("double")
    }

    fun generate() {
        generateGiven()
        val ret = probeRealResult()
        generateWhen()
        generateThen(ret)
        afterGenerate()
    }

    fun processParams(
        match: Method,
        configParams: List<JsonObject>
    ): ArrayList<Any?> {
        val list = ArrayList<Any?>()
        match.parameters.forEachIndexed { index, it ->
            val given = configParams[index]["value"]!!
            val type = it.type
            val componentType = it.parameterizedType
            list.add(processParam(type, given, componentType))
        }
        return list
    }

    public fun processParam(
        clz: Class<*>,
        given: JsonElement,
        type: Type
    ): Any? {
        if (isBasicType(clz)) {
            return processPrimitive(given, clz)
        } else if (clz == String::class.java) {
            return (given as JsonLiteral).content
        } else if (clz.isArray) {
            return if (given.jsonArray != null) processArray(given.jsonArray, (type as Class<*>).componentType) else null
        } else if (clz.isAssignableFrom(List::class.java)) {
            return if (given.jsonArray != null) processList(given.jsonArray,(type as ParameterizedTypeImpl).actualTypeArguments[0] as Class<*>) else null
        } else {
            return processObject(given.toString(), clz)
        }
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

    private fun processArray(jsonArray: JsonArray, type: Class<*>): Any {
        if (isPrimitiveBasicType(type)) {
            val size = jsonArray.size
            when(type.name) {
                "int" -> {
                    val init: (Int) -> Int = {0}
                    val res = IntArray(size,init)
                    jsonArray.forEachIndexed { index, jsonElement ->
                        res[index] = (jsonElement as JsonLiteral).content.toInt()
                    }
                    return res
                }
                "short" -> {
                    val init: (Int) -> Short = {0}
                    val res = ShortArray(size,init)
                    jsonArray.forEachIndexed { index, jsonElement ->
                        res[index] = (jsonElement as JsonLiteral).content.toShort()
                    }
                    return res
                }
                "boolean" -> {
                    val init: (Int) -> Boolean = {true}
                    val res = BooleanArray(size,init)
                    jsonArray.forEachIndexed { index, jsonElement ->
                        res[index] = (jsonElement as JsonLiteral).content.toBoolean()
                    }
                    return res
                }
                "byte" -> {
                    val init: (Int) -> Byte = {0}
                    val res = ByteArray(size,init)
                    jsonArray.forEachIndexed { index, jsonElement ->
                        res[index] = (jsonElement as JsonLiteral).content.toByte()
                    }
                    return res
                }
                "char" -> {
                    val init: (Int) -> Char = {'0'}
                    val res = CharArray(size,init)
                    jsonArray.forEachIndexed { index, jsonElement ->
                        res[index] = (jsonElement as JsonLiteral).content.toCharArray()[0]
                    }
                    return res
                }
                "float" -> {
                    val init: (Int) -> Float = {0.0f}
                    val res = FloatArray(size,init)
                    jsonArray.forEachIndexed { index, jsonElement ->
                        res[index] = (jsonElement as JsonLiteral).content.toFloat()
                    }
                    return res
                }
                "double" -> {
                    val init: (Int) -> Double = {0.0}
                    val res = DoubleArray(size,init)
                    jsonArray.forEachIndexed { index, jsonElement ->
                        res[index] = (jsonElement as JsonLiteral).content.toDouble()
                    }
                    return res
                }
                "long" -> {
                    val init: (Int) -> Long = {0}
                    val res = LongArray(size,init)
                    jsonArray.forEachIndexed { index, jsonElement ->
                        res[index] = (jsonElement as JsonLiteral).content.toLong()
                    }
                    return res
                }
                else -> {
                    return emptyArray<Any>()
                }
            }
        }
        return processList(jsonArray, type).toTypedArray()
    }

    private fun processList(
        jsonArray: JsonArray,
        type: Class<*>
    ): MutableList<Any?> {
        var res = mutableListOf<Any?>()
        jsonArray.forEach {
            res.add(processObject(it.toString(), type))
        }
        return res
    }

    private fun processObject(
        params: String?,
        type: Class<*>
    ): Any? {
        if (params == null) {
            return null
        }
        val gson = Gson()
        val ins = gson.fromJson(params, type)
        return ins
    }

    fun addParamsByteCode(
        match: Method,
        configParams: List<JsonObject>,
        mv: MethodVisitor
    ) {
        match.parameters.forEachIndexed { index, parameter ->
            val paramValue = configParams[index]["value"]!!
            val type = parameter.type
            val componentType = parameter.parameterizedType
            addParamByteCode(type, paramValue, mv, componentType)
        }
    }

    fun addParamByteCode(
        clz: Class<*>,
        paramValue: JsonElement,
        mv: MethodVisitor,
        type: Type
    ) {
        if (isBasicType(clz)) {
            addBasicByteCode(clz, (paramValue as JsonLiteral).content, mv)
        } else if (clz == String::class.java) {
            addStringByteCode((paramValue as JsonLiteral).content, mv)
        } else if (clz.isArray) {
            addArrayParamByteCode(clz.componentType, paramValue.jsonArray, mv)
        } else if (clz.isAssignableFrom(List::class.java)) {
            addListParamByteCode(
                (type as ParameterizedTypeImpl).actualTypeArguments[0] as Class<*>,
                paramValue.jsonArray, mv
            )
        } else {
            addObjectParamByteCode(clz, paramValue.jsonObject, mv)
        }
    }

    private fun isBasicType(type: Class<*>) = type.isPrimitive

    private fun addBasicByteCode(type: Class<*>, value: Any, mv: MethodVisitor) {
        when (type) {
            Byte::class.javaPrimitiveType -> {
                mv.visitLdcInsn(value.toString().toByte())
            }
            Char::class.javaPrimitiveType -> {
                mv.visitLdcInsn(value.toString().toCharArray()[0])
            }
            Double::class.javaPrimitiveType -> {
                mv.visitLdcInsn(value.toString().toDouble())
            }
            Float::class.javaPrimitiveType -> {
                mv.visitLdcInsn(value.toString().toFloat())
            }
            Int::class.javaPrimitiveType -> {
                mv.visitLdcInsn(value.toString().toInt())
            }
            Long::class.javaPrimitiveType -> {
                mv.visitLdcInsn(value.toString().toLong())
            }
            Short::class.javaPrimitiveType -> {
                mv.visitLdcInsn(value.toString().toShort())
            }
            Boolean::class.javaPrimitiveType -> {
                mv.visitLdcInsn(value.toString())
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Ljava/lang/String;)Ljava/lang/Boolean;", false)
            }
            else -> mv.visitInsn(ACONST_NULL)
        }
    }

    private fun addStringByteCode(value: String, mv: MethodVisitor) {
        mv.visitLdcInsn(value)
    }

    private fun addArrayParamByteCode(componentType: Class<*>, arrayParams: JsonArray, mv: MethodVisitor) {
        if (isPrimitiveBasicType(componentType)) {
            val size = arrayParams.size
            when(componentType.name) {
                "int" -> {
                    mv.visitIntInsn(BIPUSH, size)
                    mv.visitIntInsn(NEWARRAY, T_INT)
                    varCounter++
                    mv.visitVarInsn(ASTORE, varCounter)
                    mv.visitVarInsn(ALOAD, varCounter)
                    for (index in 0 until size) {
                        mv.visitVarInsn(ALOAD, varCounter)
                        mv.visitIntInsn(BIPUSH, index)
                        mv.visitIntInsn(BIPUSH, (arrayParams[index] as JsonLiteral).content.toInt())
                        mv.visitInsn(IASTORE)
                    }
                }
                "short" -> {
                    mv.visitIntInsn(BIPUSH, size)
                    mv.visitIntInsn(NEWARRAY, T_SHORT)
                    varCounter++
                    mv.visitVarInsn(ASTORE, varCounter)
                    mv.visitVarInsn(ALOAD, varCounter)
                    for (index in 0 until size) {
                        mv.visitVarInsn(ALOAD, varCounter)
                        mv.visitIntInsn(BIPUSH, index)
                        mv.visitIntInsn(BIPUSH, (arrayParams[index] as JsonLiteral).content.toInt())
                        mv.visitInsn(SASTORE)
                    }
                }
                "boolean" -> {
                    mv.visitIntInsn(BIPUSH, size)
                    mv.visitIntInsn(NEWARRAY, T_BOOLEAN)
                    varCounter++
                    mv.visitVarInsn(ASTORE, varCounter)
                    mv.visitVarInsn(ALOAD, varCounter)
                    for (index in 0 until size) {
                        mv.visitVarInsn(ALOAD, varCounter)
                        mv.visitIntInsn(BIPUSH, index)
                        mv.visitIntInsn(BIPUSH, (arrayParams[index] as JsonLiteral).content.toInt())
                        mv.visitInsn(BASTORE)
                    }
                }
                "byte" -> {
                    mv.visitIntInsn(BIPUSH, size)
                    mv.visitIntInsn(NEWARRAY, T_BYTE)
                    varCounter++
                    mv.visitVarInsn(ASTORE, varCounter)
                    mv.visitVarInsn(ALOAD, varCounter)
                    for (index in 0 until size) {
                        mv.visitVarInsn(ALOAD, varCounter)
                        mv.visitIntInsn(BIPUSH, index)
                        mv.visitIntInsn(BIPUSH, (arrayParams[index] as JsonLiteral).content.toInt())
                        mv.visitInsn(BASTORE)
                    }
                }
                "char" -> {
                    mv.visitIntInsn(BIPUSH, size)
                    mv.visitIntInsn(NEWARRAY, T_CHAR)
                    varCounter++
                    mv.visitVarInsn(ASTORE, varCounter)
                    mv.visitVarInsn(ALOAD, varCounter)
                    for (index in 0 until size) {
                        mv.visitVarInsn(ALOAD, varCounter)
                        mv.visitIntInsn(BIPUSH, index)
                        mv.visitIntInsn(BIPUSH, (arrayParams[index] as JsonLiteral).content.toInt())
                        mv.visitInsn(CASTORE)
                    }
                }
                "float" -> {
                    mv.visitIntInsn(BIPUSH, size)
                    mv.visitIntInsn(NEWARRAY, T_FLOAT)
                    varCounter++
                    mv.visitVarInsn(ASTORE, varCounter)
                    mv.visitVarInsn(ALOAD, varCounter)
                    for (index in 0 until size) {
                        mv.visitVarInsn(ALOAD, varCounter)
                        mv.visitIntInsn(BIPUSH, index)
                        mv.visitIntInsn(BIPUSH, (arrayParams[index] as JsonLiteral).content.toInt())
                        mv.visitInsn(FASTORE)
                    }
                }
                "double" -> {
                    mv.visitIntInsn(BIPUSH, size)
                    mv.visitIntInsn(NEWARRAY, T_DOUBLE)
                    varCounter++
                    mv.visitVarInsn(ASTORE, varCounter)
                    mv.visitVarInsn(ALOAD, varCounter)
                    for (index in 0 until size) {
                        mv.visitVarInsn(ALOAD, varCounter)
                        mv.visitIntInsn(BIPUSH, index)
                        mv.visitIntInsn(BIPUSH, (arrayParams[index] as JsonLiteral).content.toInt())
                        mv.visitInsn(DASTORE)
                    }
                }
                "long" -> {
                    mv.visitIntInsn(BIPUSH, size)
                    mv.visitIntInsn(NEWARRAY, T_LONG)
                    varCounter++
                    mv.visitVarInsn(ASTORE, varCounter)
                    mv.visitVarInsn(ALOAD, varCounter)
                    for (index in 0 until size) {
                        mv.visitVarInsn(ALOAD, varCounter)
                        mv.visitIntInsn(BIPUSH, index)
                        mv.visitIntInsn(BIPUSH, (arrayParams[index] as JsonLiteral).content.toInt())
                        mv.visitInsn(LASTORE)
                    }
                }
                else -> {
                }
            }
        } else {
            addListParamByteCode(componentType, arrayParams, mv)
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/List", "toArray", "()V", false)
            varCounter++
            val insVarSlot = varCounter
            mv.visitVarInsn(ASTORE, insVarSlot)
            mv.visitVarInsn(ALOAD, insVarSlot)
        }
    }

    private fun addListParamByteCode(
        componentType: Class<*>,
        jsonArray: JsonArray,
        mv: MethodVisitor
    ) {
        mv.visitTypeInsn(NEW, "java/util/ArrayList")
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false)
        varCounter++
        val insVarSlot = varCounter
        mv.visitVarInsn(ASTORE, insVarSlot)
        val isBoxedType = isBoxedBasicType(componentType)
        jsonArray.forEach {
            mv.visitVarInsn(ALOAD, insVarSlot)
            if (isBoxedType) {
                addBoxParamByteCode(componentType, it as JsonLiteral, mv)
            } else {
                addObjectParamByteCode(componentType, it.jsonObject, mv)
            }
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/List", "add", "(Ljava/lang/Object;)V", false)
        }
        mv.visitVarInsn(ALOAD, insVarSlot)
    }

    private fun addBoxParamByteCode(type: Class<*>, value: JsonLiteral, mv: MethodVisitor) {
        when (type.name) {
            "java.lang.Byte" -> {
                mv.visitLdcInsn(value.toString())
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(Ljava/lang/String;)Ljava/lang/Byte;", false)
            }
            "java.lang.Double" -> {
                mv.visitLdcInsn(value.toString())
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(Ljava/lang/String;)Ljava/lang/Double;", false)
            }
            "java.lang.Float" -> {
                mv.visitLdcInsn(value.toString())
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(Ljava/lang/String;)Ljava/lang/Float;", false)
            }
            "java.lang.Integer" -> {
                mv.visitLdcInsn(value.toString())
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;", false)
            }
            "java.lang.Long" -> {
                mv.visitLdcInsn(value.toString())
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(Ljava/lang/String;)Ljava/lang/Long;", false)
            }
            "java.lang.Short" -> {
                mv.visitLdcInsn(value.toString())
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(Ljava/lang/String;)Ljava/lang/Short;", false)
            }
            "java.lang.Boolean" -> {
                mv.visitLdcInsn(value.toString())
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Ljava/lang/String;)Ljava/lang/Boolean;", false)
            }
            else -> mv.visitInsn(ACONST_NULL)
        }
    }

    private fun isBoxedBasicType(componentType: Class<*>): Boolean {
        return componentBoxedTypes.contains(componentType.name)
    }

    private fun isPrimitiveBasicType(componentType: Class<*>): Boolean {
        return componentPrimitiveTypes.contains(componentType.name)
    }


    fun addObjectParamByteCode(type: Class<*>, config: JsonObject, mv: MethodVisitor) {
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
            if (ele == null) {
                return@forEach
            }
            val setMethodName = "set${paramName}"
            val setMethod: Method? = methods.find { it.name.equals(setMethodName, true) }
            mv.visitVarInsn(ALOAD, oldCounter)
            if (isBasicType(it.type)) {
                addBasicByteCode(it.type, (ele as JsonLiteral).content, mv)
            } else if (it.type == String::class.java) {
                addStringByteCode((ele as JsonLiteral).content, mv)
            } else {
                addObjectParamByteCode(it.type, ele.jsonObject, mv)
            }
            if (setMethod != null) {
                mv.visitMethodInsn(
                    INVOKEVIRTUAL, type.name.replace('.', '/'), setMethod.name,
                    processMethodDesc(setMethod), false
                )
            } else {
                mv.visitFieldInsn(PUTFIELD, type.name.replace('.', '/'), paramName, mapTypeToDesc(it.type))
            }
        }
        mv.visitVarInsn(ALOAD, oldCounter)
    }

    fun processVerifyByteCode(match: Method, mv: MethodVisitor, ret: Any?) {
        if (isBasicType(match.returnType)) {
            verifyBasic(match.returnType, ret!!, mv)
        } else if (ret == null) {
            verifyNull(mv)
        } else if (match.returnType == String::class.java) {
            verifyString(ret as String, mv)
        } else if(match.returnType.isArray) {
            veryfyArraySize((match.genericReturnType as Class<*>).componentType, ret, mv)
        } else if (match.returnType.isAssignableFrom(List::class.java)) {
            verifyListSize(ret, mv)
        } else {
            veryfyObject(match.returnType, mv, ret)
        }
    }

    private fun verifyNull(mv: MethodVisitor) {
        mv.visitInsn(ACONST_NULL)
        addVerifyByteCode(mv)
    }

    private fun verifyString(s: String, mv: MethodVisitor) {
        mv.visitLdcInsn(s)
        addVerifyByteCode(mv)
    }

    private fun verifyBasic(type: Class<*>, value: Any, mv: MethodVisitor) {
        addBasicByteCode(type, value, mv)
        addVerifyByteCode(mv)
    }

    fun veryfyArraySize(type: Class<*>, ret: Any, mv: MethodVisitor) {
        val count = when(type.name) {
            "int" -> {
                (ret as IntArray).size
            }
            "short" -> {
                (ret as ShortArray).size
            }
            "boolean" -> {
                (ret as BooleanArray).size
            }
            "byte" -> {
                (ret as ByteArray).size
            }
            "char" -> {
                (ret as CharArray).size
            }
            "float" -> {
                (ret as FloatArray).size
            }
            "double" -> {
                (ret as DoubleArray).size
            }
            "long" -> {
                (ret as LongArray).size
            }
            else -> {
                (ret as Array<*>).size
            }
        }
        mv.visitInsn(ARRAYLENGTH)
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
        mv.visitIntInsn(BIPUSH, count)
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
        addVerifyByteCode(mv)
    }

    fun verifyListSize(ret: Any?, mv: MethodVisitor) {
        val count = (ret as List<*>).size
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true)
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
        mv.visitIntInsn(BIPUSH, count)
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
        addVerifyByteCode(mv)
    }

    private fun veryfyObject(type: Class<*>, mv: MethodVisitor, ret: Any) {
        val oldVarCounter = varCounter
        val methods = type.methods
        type.declaredFields.forEach {
            it.setAccessible(true)
            if (!isBasicType(it.type) && it.type != String::class.java) {
                return@forEach
            }
            val name = it.name
            val setMethodName = "get${name}"
            val getMethod: Method? = methods.find { it.name.equals(setMethodName, true) }
            val value = it.get(ret)
            if (getMethod != null) {
                mv.visitVarInsn(ALOAD, oldVarCounter)
                mv.visitMethodInsn(
                    INVOKESPECIAL, type.name.replace('.', '/'), getMethod.name,
                    processMethodDesc(getMethod), false
                )
                if (isBasicType(it.type)) {
                    verifyBasic(it.type, value, mv)
                } else {
                    verifyString(value as String, mv)
                }
            }
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

    fun postProcess(returnType: Class<*>, mv: MethodVisitor) {
        varCounter++
        when (returnType) {
            Boolean::class.javaPrimitiveType -> {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean")
                mv.visitVarInsn(ISTORE, varCounter)
                mv.visitVarInsn(ILOAD, varCounter)
            }
            Char::class.javaPrimitiveType -> {
                mv.visitVarInsn(ISTORE, varCounter)
                mv.visitVarInsn(ILOAD, varCounter)
            }
            Byte::class.javaPrimitiveType -> {
                mv.visitVarInsn(ISTORE, varCounter)
                mv.visitVarInsn(ILOAD, varCounter)
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "java/lang/Byte",
                    "valueOf",
                    "(I)Ljava/lang/Byte;",
                    false
                )
            }
            Short::class.javaPrimitiveType -> {
                mv.visitVarInsn(ISTORE, varCounter)
                mv.visitVarInsn(ILOAD, varCounter)
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "java/lang/Short",
                    "valueOf",
                    "(I)Ljava/lang/Short;",
                    false
                )
            }
            Int::class.javaPrimitiveType -> {
                mv.visitVarInsn(ISTORE, varCounter)
                mv.visitVarInsn(ILOAD, varCounter)
                mv.visitMethodInsn(
                    INVOKESTATIC, "java/lang/Integer", "valueOf",
                    "(I)Ljava/lang/Integer;", false
                )
            }
            Float::class.javaPrimitiveType -> {
                mv.visitVarInsn(FSTORE, varCounter)
                mv.visitVarInsn(FLOAD, varCounter)
            }
            Double::class.javaPrimitiveType -> {
                mv.visitVarInsn(DSTORE, varCounter)
                mv.visitVarInsn(DLOAD, varCounter)
            }
            Long::class.javaPrimitiveType -> {
                mv.visitVarInsn(LSTORE, varCounter)
                mv.visitVarInsn(LLOAD, varCounter)
            }
            else -> {
                mv.visitIntInsn(ASTORE, varCounter)
                mv.visitVarInsn(ALOAD, varCounter)
            }
        }
    }

    open fun afterGenerate() {
    }

    open fun generateGiven() {
    }

    abstract fun probeRealResult(): Any?

    abstract fun generateWhen()

    abstract fun generateThen(ret: Any?)
}