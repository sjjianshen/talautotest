package com.tal.autotest.core.generateor.method

import com.tal.autotest.core.generateor.testcase.SpringBeanMethodTestCaseGenerator
import com.tal.autotest.core.util.Case
import com.tal.autotest.core.util.ClassConfig
import com.tal.autotest.core.util.MethodConfig
import com.tal.autotest.core.util.MethodGeneratorContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.springframework.test.context.TestContextManager
import java.lang.reflect.Method

open class SpringMethodGenerator(
    val context: TestContextManager,
    mtc: MethodGeneratorContext
) : MethodGenerator(mtc) {

    override fun doGenerateMethodCode(
        method: Method,
        mv: MethodVisitor,
        cacf: Case
    ) {
        SpringBeanMethodTestCaseGenerator(
            mv,
            context,
            clz,
            method,
            cacf.params
        ).generate()
    }
}