package com.tal.autotest.core

import org.springframework.beans.BeanUtils
import org.springframework.test.context.*
import org.springframework.test.context.support.AbstractTestContextBootstrapper
import org.springframework.test.context.support.DefaultBootstrapContext
import org.springframework.test.context.support.DefaultTestContext
import org.springframework.test.context.support.DelegatingSmartContextLoader
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils
import java.util.*

class AllInOneContextBootStraper(private val declaredClz: Class<*>, private val bootClz: Class<*>) :
    AbstractTestContextBootstrapper() {
    private val DEFAULT_CACHE_AWARE_CONTEXT_LOADER_DELEGATE_CLASS_NAME =
        "org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate"
    override fun getDefaultContextLoaderClass(testClass: Class<*>): Class<out ContextLoader> {
        return DelegatingSmartContextLoader::class.java
    }

    override fun buildTestContext(): TestContext {
        var clazz = ClassUtils.forName(DEFAULT_CACHE_AWARE_CONTEXT_LOADER_DELEGATE_CLASS_NAME, Thread.currentThread()
                .contextClassLoader)
        var delegate: CacheAwareContextLoaderDelegate = BeanUtils.instantiateClass(clazz, CacheAwareContextLoaderDelegate::class.java)
        val bootStrapContext = DefaultBootstrapContext(declaredClz, delegate)
        this.bootstrapContext = bootStrapContext
        val defaultTestContext = DefaultTestContext(
            declaredClz, buildAllInOneContextConfiguration(),
            cacheAwareContextLoaderDelegate
        )

        return defaultTestContext
    }

    private fun buildAllInOneContextConfiguration(): MergedContextConfiguration {
        val testClass = declaredClz
        val cacheAwareContextLoaderDelegate = cacheAwareContextLoaderDelegate
        val attribute =
            ContextConfigurationAttributes(
                declaredClz,
                arrayOf<String>(),
                arrayOf(bootClz),
                false,
                arrayOf(),
                false,
                ContextLoader::class.java
            )
        return buildAllInOneContextConfiguration(
            testClass,
            listOf(attribute),
            null, cacheAwareContextLoaderDelegate
        )
    }

    private fun buildAllInOneContextConfiguration(
        testClass: Class<*>,
        configAttributesList: List<ContextConfigurationAttributes>,
        parentConfig: MergedContextConfiguration?,
        cacheAwareContextLoaderDelegate: CacheAwareContextLoaderDelegate
    ): MergedContextConfiguration {
        Assert.notEmpty(configAttributesList, "ContextConfigurationAttributes list must not be null or empty")
        val contextLoader = resolveContextLoader(testClass, configAttributesList)
        val locations = ArrayList<String>()
        val classes = ArrayList<Class<*>>()
        val initializers = ArrayList<Class<*>>()
        for (configAttributes in configAttributesList) {
            if (contextLoader is SmartContextLoader) {
                contextLoader.processContextConfiguration(configAttributes)
                locations.addAll(0, Arrays.asList(*configAttributes.getLocations()))
                classes.addAll(0, Arrays.asList(*configAttributes.getClasses()))
            } else {
                val processedLocations = contextLoader.processLocations(
                    configAttributes.getDeclaringClass(), *configAttributes.getLocations()
                )
                locations.addAll(0, Arrays.asList(*processedLocations))
                // Legacy ContextLoaders don't know how to process classes
            }
            initializers.addAll(
                0,
                Arrays.asList(*configAttributes.getInitializers())
            )
            if (!configAttributes.isInheritLocations()) {
                break
            }
        }
        val mergedConfig = MergedContextConfiguration(
            testClass,
            StringUtils.toStringArray(locations), ClassUtils.toClassArray(classes),
            setOf(TalApplicationContextInitializer::class.java),
            arrayOf(),
            arrayOf(),
            arrayOf(),
            setOf(), contextLoader, cacheAwareContextLoaderDelegate, parentConfig
        )
        return mergedConfig
    }
}