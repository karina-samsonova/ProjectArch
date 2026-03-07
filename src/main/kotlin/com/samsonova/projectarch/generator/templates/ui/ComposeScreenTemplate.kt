package com.samsonova.projectarch.generator.templates.ui

import com.samsonova.projectarch.models.AppArchitecture
import com.samsonova.projectarch.models.Feature
import com.samsonova.projectarch.models.ListType
import com.samsonova.projectarch.models.Screen

object ComposeScreenTemplate {
    fun generate(architecture: AppArchitecture, feature: Feature, screen: Screen): String {
        val viewModelParam = if (screen.hasViewModel) {
            "viewModel: ${screen.name}ViewModel = hiltViewModel()"
        } else {
            ""
        }

        val lazyColumnContent = if (screen.listType == ListType.LAZY_COLUMN) {
            """
                        val data = (uiState as ${screen.name}UiState.Success).data
                        LazyColumn(contentPadding = paddingValues) {
                            items(1) {
                                Text("Content")
                            }
                        }
            """.trimIndent()
        } else {
            """
                        Text("Success")
            """.trimIndent()
        }

        return if (screen.hasViewModel) {
            """
                package ${architecture.packageName}.${feature.name.lowercase()}.presentation
                
                import androidx.compose.material3.*
                import androidx.compose.runtime.*
                import androidx.compose.foundation.lazy.LazyColumn
                import androidx.compose.foundation.lazy.items
                import androidx.hilt.navigation.compose.hiltViewModel
                
                @Composable
                fun ${screen.name}Screen(
                    $viewModelParam
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    
                    Scaffold(
                        topBar = { TopAppBar(title = { Text("${screen.name}") }) }
                    ) { paddingValues ->
                        when (uiState) {
                            is ${screen.name}UiState.Loading -> {
                                CircularProgressIndicator()
                            }
                            is ${screen.name}UiState.Success -> {
                $lazyColumnContent
                            }
                            is ${screen.name}UiState.Error -> {
                                val error = (uiState as ${screen.name}UiState.Error).message
                                Text("Error: ${'$'}error")
                            }
                        }
                    }
                }
            """.trimIndent()
        } else {
            """
                package ${architecture.packageName}.${feature.name.lowercase()}.presentation
                
                import androidx.compose.material3.*
                import androidx.compose.runtime.*
                
                @Composable
                fun ${screen.name}Screen() {
                    Text("${screen.name}")
                }
            """.trimIndent()
        }
    }
}

// ==================== STRING EXTENSIONS ====================

fun String.camelToSnakeCase(): String {
    return this.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
}

fun String.decapitalize(): String {
    return this.replaceFirstChar { it.lowercase() }
}