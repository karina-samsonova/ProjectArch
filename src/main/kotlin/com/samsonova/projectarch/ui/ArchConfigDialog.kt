package com.samsonova.projectarch.ui

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.readText
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextArea
import com.samsonova.projectarch.models.AppArchitecture
import com.samsonova.projectarch.models.Result
import com.samsonova.projectarch.parser.YamlParser
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.UIManager

class ArchConfigDialog(
    private val project: Project,
    private val onConfigLoaded: (AppArchitecture) -> Unit
) : DialogWrapper(project) {

    private lateinit var yamlTextArea: JBTextArea
    private lateinit var statusLabel: JBLabel

    init {
        title = "Architecture Configuration"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JBPanel<JBPanel<*>>(BorderLayout())
        mainPanel.preferredSize = Dimension(900, 650)

        val yamlPanel = createYamlPanel()
        mainPanel.add(yamlPanel, BorderLayout.CENTER)

        statusLabel = JBLabel("Ready")
        val bottomPanel = JPanel(BorderLayout())
        bottomPanel.add(statusLabel, BorderLayout.CENTER)
        mainPanel.add(bottomPanel, BorderLayout.SOUTH)

        return mainPanel
    }

    private fun createYamlPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10))
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        val buttonsPanel = JPanel()

        val loadFileButton = JButton("Load from File").apply {
            addActionListener {
                loadYamlFromFile()
            }
        }
        buttonsPanel.add(loadFileButton)

        val clearButton = JButton("Clear").apply {
            addActionListener {
                yamlTextArea.text = ""
            }
        }
        buttonsPanel.add(clearButton)

        val exampleButton = JButton("Load Example").apply {
            addActionListener {
                loadYamlExample()
            }
        }
        buttonsPanel.add(exampleButton)

        panel.add(buttonsPanel, BorderLayout.NORTH)

        yamlTextArea = JBTextArea(30, 60)
        yamlTextArea.lineWrap = false
        yamlTextArea.font = UIManager.getFont("TextArea.font") ?: yamlTextArea.font

        val scrollPane = JScrollPane(yamlTextArea)
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }

    override fun doOKAction() {
        val configText = yamlTextArea.text

        if (configText.isBlank()) {
            statusLabel.text = "Error: Configuration is empty"
            return
        }

        val parser = YamlParser()
        when (val result = parser.parse(configText)) {
            is Result.Success -> {
                onConfigLoaded(result.data)
                super.doOKAction()
            }
            is Result.Error -> {
                statusLabel.text = "Error: ${result.exception.message}"
            }
        }
    }

    private fun loadYamlFromFile() {
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withFileFilter { it.name.endsWith(".yaml") || it.name.endsWith(".yml") }

        val file = FileChooser.chooseFile(descriptor, project, null) ?: return

        try {
            yamlTextArea.text = file.readText()
            statusLabel.text = "Loaded: ${file.name}"
        } catch (e: Exception) {
            statusLabel.text = "Error loading file: ${e.message}"
        }
    }

    private fun loadYamlExample() {
        yamlTextArea.text = getYamlExample()
        statusLabel.text = "Loaded example configuration"
    }

    private fun getYamlExample(): String {
        return """
            app_name: "MyApp"
            package_name: "com.example.app"
            
            modules:
              # Feature: Authentication
              - name: "auth"
                type: "feature"
                description: "User authentication and login"
                architecture:
                  pattern: "CLEAN_ARCHITECTURE"
                  use_layers: true
                
                local_storage: "DATASTORE"
                remote_storage: "RETROFIT"
                use_hilt: true
                
                screens:
                  - name: "LoginScreen"
                    ui: "COMPOSE"
                    view_model: true
                  - name: "SignUpScreen"
                    ui: "COMPOSE"
                    view_model: true
                
                models:
                  - name: "User"
                    properties:
                      id: "String"
                      email: "String"
                      name: "String"
                    entity: true
                    dto: true
                
                use_cases:
                  - name: "LoginUser"
                    description: "Authenticate user"
                    input: "LoginCredentials"
                    output: "User"
                  - name: "SignUpUser"
                    description: "Create new account"
                    input: "SignUpData"
                    output: "User"
              
              # Feature: Posts
              - name: "posts"
                type: "feature"
                description: "Create and view posts"
                architecture:
                  pattern: "MVVM"
                  use_layers: false
                
                local_storage: "ROOM"
                remote_storage: "RETROFIT"
                use_hilt: true
                
                screens:
                  - name: "PostListScreen"
                    ui: "COMPOSE"
                    view_model: true
                  - name: "PostDetailScreen"
                    ui: "COMPOSE"
                    view_model: true
                
                models:
                  - name: "Post"
                    properties:
                      id: "String"
                      title: "String"
                      content: "String"
                      likes: "Int"
                      createdAt: "Long"
                    entity: true
                    dto: true
                
                use_cases:
                  - name: "GetPosts"
                    output: "Post"
                  - name: "CreatePost"
                    input: "Post"
              
              # Core: Network
              - name: "network"
                type: "core"
                description: "HTTP client and API integration"
              
              # Core: Database
              - name: "database"
                type: "core"
                description: "Room database and migrations"
              
              # Design System
              - name: "design-system"
                type: "design_system"
                description: "Design tokens and common components"
        """.trimIndent()
    }
}