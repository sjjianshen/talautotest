package com.tal.autotest.tool

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.ConfigurableEnvironment

class TalApplicationContextInitializer() : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val env : ConfigurableEnvironment = applicationContext.environment as ConfigurableEnvironment
        TalConfigListener().postProcessEnv(env)
    }
}