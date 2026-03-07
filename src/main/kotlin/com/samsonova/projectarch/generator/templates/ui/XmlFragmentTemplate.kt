package com.samsonova.projectarch.generator.templates.ui

import com.samsonova.projectarch.models.AppArchitecture
import com.samsonova.projectarch.models.Feature
import com.samsonova.projectarch.models.Screen

object XmlFragmentTemplate {
    fun generate(architecture: AppArchitecture, feature: Feature, screen: Screen): String {
        val viewModelField = if (screen.hasViewModel) {
            "private val viewModel: ${screen.name}ViewModel by viewModels()\n"
        } else {
            ""
        }

        val viewCreatedBody = if (screen.hasViewModel) {
            """
                        lifecycleScope.launch {
                            viewModel.uiState.collect { state ->
                                when (state) {
                                    is ${screen.name}UiState.Loading -> { }
                                    is ${screen.name}UiState.Success -> { }
                                    is ${screen.name}UiState.Error -> { }
                                }
                            }
                        }
            """.trimIndent()
        } else {
            ""
        }

        return """
            package ${architecture.packageName}.${feature.name.lowercase()}.presentation
            
            import android.os.Bundle
            import androidx.fragment.app.Fragment
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.viewModels
            import androidx.lifecycle.lifecycleScope
            import dagger.hilt.android.AndroidEntryPoint
            import kotlinx.coroutines.launch
            
            @AndroidEntryPoint
            class ${screen.name}Fragment : Fragment() {
            
                $viewModelField
                override fun onCreateView(
                    inflater: LayoutInflater,
                    container: ViewGroup?,
                    savedInstanceState: Bundle?
                ): View = inflater.inflate(
                    R.layout.fragment_${screen.name.camelToSnakeCase()},
                    container,
                    false
                )
            
                override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                    super.onViewCreated(view, savedInstanceState)
                    $viewCreatedBody
                }
            }
        """.trimIndent()
    }
}