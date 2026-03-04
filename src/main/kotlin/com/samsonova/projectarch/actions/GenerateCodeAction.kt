package com.samsonova.projectarch.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.samsonova.projectarch.generator.ArchitectureGenerator
import com.samsonova.projectarch.parcer.YamlDslParser

class GenerateCodeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        // Выбор файла с описанием
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withFileFilter { it.name.endsWith(".yaml") || it.name.endsWith(".yml") }
        
        val files = FileChooser.chooseFiles(descriptor, project, null)
        if (files.isNotEmpty()) {
            val yamlContent = files[0].contentsToByteArray().decodeToString()
            val parser = YamlDslParser()
            val architecture = parser.parse(yamlContent)
            
            val generator = ArchitectureGenerator(project)
            generator.generate(architecture)
        }
    }
}