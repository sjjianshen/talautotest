package com.tal.autotest.tool

import org.springframework.boot.context.config.ConfigFileApplicationListener
import org.springframework.core.env.ConfigurableEnvironment

class TalConfigListener : ConfigFileApplicationListener() {
    fun postProcessEnv(env : ConfigurableEnvironment) {
        addPropertySources(env, null)
    }
}