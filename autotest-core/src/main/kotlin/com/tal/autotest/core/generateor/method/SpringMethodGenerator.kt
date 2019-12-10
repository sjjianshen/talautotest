package com.tal.autotest.core.generateor.method

import com.tal.autotest.core.generateor.testcase.SpringBeanMethodTestCaseGenerator
import com.tal.autotest.core.util.ClassConfig
import com.tal.autotest.core.util.MethodConfig
import kotlinx.serialization.json.JsonElement
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.springframework.test.context.TestContextManager
import java.lang.reflect.Method

open class SpringMethodGenerator(
    val context: TestContextManager,
    mcf: MethodConfig,
    clz: Class<*>,
    cw: ClassVisitor
) : MethodGenerator(mcf, clz, cw) {

    override fun doGenerateMethodCode(
        method: Method,
        mv: MethodVisitor,
        configParams: List<JsonElement>
    ) {
        SpringBeanMethodTestCaseGenerator(
            mv,
            context,
            clz,
            method,
            configParams
        ).generate()
    }
}