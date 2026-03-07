package com.samsonova.projectarch.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import com.samsonova.projectarch.generator.CodeGenerator
import com.samsonova.projectarch.models.AppArchitecture
import com.samsonova.projectarch.models.Result
import com.samsonova.projectarch.ui.ArchConfigDialog
import java.io.File

class GenerateCodeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val basePath = project.basePath ?: return

        val dialog = ArchConfigDialog(project) { architecture ->
            generateCode(architecture, File(basePath), project)
        }

        dialog.show()
    }

    private fun generateCode(architecture: AppArchitecture, baseDir: File, project: Any?) {
        try {
            val generator = CodeGenerator(project)
            when (val result = generator.generate(architecture, baseDir)) {
                is Result.Success<*> -> {
                    VirtualFileManager.getInstance().syncRefresh()

                    Messages.showInfoMessage(
                        "Architecture code generated successfully!",
                        "Success"
                    )
                }
                is Result.Error -> {
                    Messages.showErrorDialog(
                        "Error generating code: ${result.exception.message}",
                        "Generation Error"
                    )
                }

                else -> {}
            }
        } catch (ex: Exception) {
            Messages.showErrorDialog(
                "Failed to generate architecture: ${ex.message}",
                "Error"
            )
        }
    }
}