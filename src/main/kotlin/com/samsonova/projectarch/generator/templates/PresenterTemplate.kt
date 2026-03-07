package com.samsonova.projectarch.generator.templates

import com.samsonova.projectarch.models.AppArchitecture
import com.samsonova.projectarch.models.Feature
import com.samsonova.projectarch.models.Screen
import com.samsonova.projectarch.models.UseCase

object PresenterTemplate {

    fun generate(architecture: AppArchitecture, feature: Feature, screen: Screen): String {
        return """
            package ${architecture.packageName}.${feature.name.lowercase()}.presenter
            
            import javax.inject.Inject
            
            class ${screen.name}Presenter @Inject constructor(
                // Inject interactors
            ) : ${screen.name}Contract.Presenter {
            
                private var view: ${screen.name}Contract.View? = null
            
                override fun attach(view: ${screen.name}Contract.View) {
                    this.view = view
                }
            
                override fun detach() {
                    this.view = null
                }
            
                override fun load() {
                    // Interact with interactors
                }
            }
        """.trimIndent()
    }

    fun generateContract(feature: Feature, screen: Screen): String {
        return """
            interface ${screen.name}Contract {
            
                interface View {
                    fun showLoading()
                    fun showContent(data: Any)
                    fun showError(message: String)
                }
            
                interface Presenter {
                    fun attach(view: View)
                    fun detach()
                    fun load()
                }
            
                interface Interactor {
                    fun load(callback: Callback)
                }
            
                interface Callback {
                    fun onSuccess(data: Any)
                    fun onError(exception: Exception)
                }
            }
        """.trimIndent()
    }

    fun generateInteractor(architecture: AppArchitecture, feature: Feature, useCase: UseCase): String {
        return """
            package ${architecture.packageName}.${feature.name.lowercase()}.interactor
            
            import javax.inject.Inject
            
            class ${useCase.name}Interactor @Inject constructor() {
            
                suspend fun execute(): Result<Unit> {
                    return try {
                        // Execute business logic
                        Result.Success(Unit)
                    } catch (e: Exception) {
                        Result.Error(e)
                    }
                }
            }
        """.trimIndent()
    }
}