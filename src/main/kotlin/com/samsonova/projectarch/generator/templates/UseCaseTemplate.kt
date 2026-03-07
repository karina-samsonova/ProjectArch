package com.samsonova.projectarch.generator.templates

import com.samsonova.projectarch.generator.templates.ui.decapitalize
import com.samsonova.projectarch.models.AppArchitecture
import com.samsonova.projectarch.models.Feature
import com.samsonova.projectarch.models.UseCase

object UseCaseTemplate {
    
    fun generate(architecture: AppArchitecture, feature: Feature, useCase: UseCase): String {
        val inputParam = if (useCase.inputModel != null) {
            "${useCase.inputModel.decapitalize()}: ${useCase.inputModel}"
        } else {
            ""
        }

        return buildString {
            appendLine("import javax.inject.Inject")
            appendLine()
            appendLine("class ${useCase.name}UseCase @Inject constructor(")
            if (useCase.inputModel != null) {
                appendLine("    private val repository: ${useCase.inputModel}Repository")
            }
            appendLine(") {")
            appendLine()
            appendLine("    suspend operator fun invoke($inputParam): Result<${useCase.outputModel ?: "Unit"}> {")
            appendLine("        return try {")
            appendLine("            // Use business logic")
            appendLine("            Result.Success(Unit)")
            appendLine("        } catch (e: Exception) {")
            appendLine("            Result.Error(e)")
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
        }
    }
}