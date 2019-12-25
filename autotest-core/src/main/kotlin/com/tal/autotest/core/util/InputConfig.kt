package com.tal.autotest.core.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class InputConfig(val classConfigs: List<ClassConfig>)

@Serializable
data class ClassConfig(
    val name: String,
    val autowire: Boolean = false,
    val appName: String = "",
    val useMock: Boolean = false,
    val methodConfigs: List<MethodConfig>
)

@Serializable
data class MethodConfig(val name: String, val cases: List<Case>)

@Serializable
data class Case(val name: String, val params: List<JsonObject> = listOf(),
                val mock: List<MockConfig> = listOf())

@Serializable
data class MockConfig(val className: String, val methodName: String,
                      val params: List<JsonObject> = listOf(), val ret : JsonObject)
