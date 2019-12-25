package com.tal.autotest.core.generateor.clz

import com.tal.autotest.core.context.AllInOneContextBootStraper
import com.tal.autotest.core.generateor.method.SpringMethodGenerator
import com.tal.autotest.core.util.AutotestContext
import com.tal.autotest.core.util.ClassConfig
import com.tal.autotest.core.util.MethodGeneratorContext
import org.objectweb.asm.*
import org.springframework.test.context.TestContextManager

class SpringTestClassGenerator(
    ccf : ClassConfig, ctx : AutotestContext
) : TestClassGenerator(ccf, ctx) {
    var context : TestContextManager? = null

    init {
        val clz = Thread.currentThread().contextClassLoader.loadClass(ccf.name)
        context = TestContextManager(
            AllInOneContextBootStraper(
                clz,
                Thread.currentThread().contextClassLoader.loadClass(ccf.appName)
            )
        )
    }

    override fun afterClassInitialized(cw: ClassWriter, clz: Class<*>) {
        configAutowareClassIfNeeded(ccf, cw, clz.name)
    }

    override fun doGenerateTestClass(cw: ClassWriter, clazz: Class<*>) {
        if (context == null) {
            System.out.println("Spring context initialized failed, skip test generate")
            return
        }
        context!!.beforeTestClass()
        ccf.methodConfigs.forEach {
            val mtc = MethodGeneratorContext(ccf, it, cw, clazz)
            SpringMethodGenerator(context!!, mtc).generateTestCase()
        }
        context!!.afterTestClass()
    }

    private fun configAutowareClassIfNeeded(
        ccf: ClassConfig,
        cw: ClassWriter,
        className: String
    ) {
        if (ccf.autowire) {
            var cav: AnnotationVisitor = cw.visitAnnotation("Lorg/junit/runner/RunWith;", true)
            cav.visit("value", Type.getType("Lorg/springframework/test/context/junit4/SpringRunner;"))
            cav.visitEnd()
            cav = cw.visitAnnotation("Lorg/springframework/test/context/ContextConfiguration;", true)
            var av1 = cav.visitArray("classes")
            av1.visit(null, Type.getType("L${ccf.appName.replace(".", "/")};"))
            av1.visitEnd()
            av1 = cav.visitArray("initializers")
            av1.visit(
                null,
                Type.getType("Lorg/springframework/boot/test/context/ConfigFileApplicationContextInitializer;")
            )
            av1.visitEnd()
            cav.visitEnd()
            val fv: FieldVisitor = cw.visitField(Opcodes.ACC_PRIVATE, "instance", "L${className};", null, null)
            val fav: AnnotationVisitor =
                fv.visitAnnotation("Lorg/springframework/beans/factory/annotation/Autowired;", true)
            fav.visitEnd()
            fv.visitEnd()
        }
    }
}