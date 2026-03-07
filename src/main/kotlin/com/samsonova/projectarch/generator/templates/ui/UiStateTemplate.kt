package com.samsonova.projectarch.generator.templates.ui

import com.samsonova.projectarch.models.Feature
import com.samsonova.projectarch.models.Screen

object UiStateTemplate {
    fun generate(feature: Feature, screen: Screen): String {
        return """
            sealed class ${screen.name}UiState {
                object Loading : ${screen.name}UiState()
                data class Success(val data: Any) : ${screen.name}UiState()
                data class Error(val message: String) : ${screen.name}UiState()
            }
        """.trimIndent()
    }
}