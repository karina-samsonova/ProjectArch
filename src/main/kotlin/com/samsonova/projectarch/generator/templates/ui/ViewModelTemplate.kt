package com.samsonova.projectarch.generator.templates.ui

import com.samsonova.projectarch.models.AppArchitecture
import com.samsonova.projectarch.models.Feature
import com.samsonova.projectarch.models.Screen

object ViewModelTemplate {
    fun generate(architecture: AppArchitecture, feature: Feature, screen: Screen): String {
        return """
            package ${architecture.packageName}.${feature.name.lowercase()}.presentation
            
            import androidx.lifecycle.ViewModel
            import androidx.lifecycle.viewModelScope
            import dagger.hilt.android.lifecycle.HiltViewModel
            import kotlinx.coroutines.flow.*
            import kotlinx.coroutines.launch
            import javax.inject.Inject
            
            @HiltViewModel
            class ${screen.name}ViewModel @Inject constructor(
                // Inject use cases here
            ) : ViewModel() {
            
                private val _uiState = MutableStateFlow<${screen.name}UiState>(${screen.name}UiState.Loading)
                val uiState: StateFlow<${screen.name}UiState> = _uiState.asStateFlow()
            
                init {
                    loadData()
                }
            
                private fun loadData() {
                    viewModelScope.launch {
                        try {
                            _uiState.value = ${screen.name}UiState.Loading
                            // Call use cases
                            _uiState.value = ${screen.name}UiState.Success(Unit)
                        } catch (e: Exception) {
                            _uiState.value = ${screen.name}UiState.Error(e.message ?: "Unknown error")
                        }
                    }
                }
            }
        """.trimIndent()
    }
}