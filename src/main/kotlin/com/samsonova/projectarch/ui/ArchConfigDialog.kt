package com.samsonova.projectarch.ui

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.readText
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTabbedPane
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

    private lateinit var tabbedPane: JBTabbedPane
    private lateinit var yamlTextArea: JBTextArea
    private lateinit var dslTextArea: JBTextArea
    private lateinit var statusLabel: JBLabel

    init {
        title = "Architecture Configuration"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JBPanel<JBPanel<*>>(BorderLayout())
        mainPanel.preferredSize = Dimension(800, 600)

        // Tabbed pane for YAML and DSL
        tabbedPane = JBTabbedPane()

        // YAML Tab
        val yamlPanel = createYamlPanel()
        tabbedPane.addTab("YAML Configuration", yamlPanel)

        // DSL Tab
        //val dslPanel = createDslPanel()
        //tabbedPane.addTab("DSL Configuration", dslPanel)

        mainPanel.add(tabbedPane, BorderLayout.CENTER)

        // Status label
        statusLabel = JBLabel("Ready")
        val bottomPanel = JPanel(BorderLayout())
        bottomPanel.add(statusLabel, BorderLayout.CENTER)
        mainPanel.add(bottomPanel, BorderLayout.SOUTH)

        return mainPanel
    }

    private fun createYamlPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10))
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Buttons panel
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

        // Text area
        yamlTextArea = JBTextArea(30, 60)
        yamlTextArea.lineWrap = false
        yamlTextArea.font = UIManager.getFont("TextArea.font") ?: yamlTextArea.font

        val scrollPane = JScrollPane(yamlTextArea)
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }

    private fun createDslPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10))
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Buttons panel
        val buttonsPanel = JPanel()

        val loadFileButton = JButton("Load from File").apply {
            addActionListener {
                loadDslFromFile()
            }
        }
        buttonsPanel.add(loadFileButton)

        val clearButton = JButton("Clear").apply {
            addActionListener {
                dslTextArea.text = ""
            }
        }
        buttonsPanel.add(clearButton)

        val exampleButton = JButton("Load Example").apply {
            addActionListener {
                loadDslExample()
            }
        }
        buttonsPanel.add(exampleButton)

        // Note label
        val noteLabel = JBLabel("Note: DSL support requires manual conversion to YAML for now")
        buttonsPanel.add(noteLabel)

        panel.add(buttonsPanel, BorderLayout.NORTH)

        // Text area
        dslTextArea = JBTextArea(30, 60)
        dslTextArea.lineWrap = false
        dslTextArea.font = UIManager.getFont("TextArea.font") ?: dslTextArea.font

        val scrollPane = JScrollPane(dslTextArea)
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }

    override fun doOKAction() {
        val selectedIndex = tabbedPane.selectedIndex
        val configText = when (selectedIndex) {
            0 -> yamlTextArea.text
            1 -> {
                statusLabel.text = "DSL support requires conversion to YAML"
                return
            }
            else -> return
        }

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

    private fun loadDslFromFile() {
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withFileFilter { it.name.endsWith(".kts") }

        val file = FileChooser.chooseFile(descriptor, project, null) ?: return

        try {
            dslTextArea.text = file.readText()
            statusLabel.text = "Loaded: ${file.name}"
        } catch (e: Exception) {
            statusLabel.text = "Error loading file: ${e.message}"
        }
    }

    private fun loadYamlExample() {
        yamlTextArea.text = getYamlExample()
        statusLabel.text = "Loaded example configuration"
    }

    private fun loadDslExample() {
        dslTextArea.text = getDslExample()
        statusLabel.text = "Loaded example configuration"
    }

    private fun getYamlExample(): String {
        return """
            app_name: "MyApp"
            package_name: "com.example.app"
            pattern: "CLEAN_ARCHITECTURE"
            ui_framework: "COMPOSE"
            storage: "ROOM"
            use_hilt: true
            min_sdk: 24
            target_sdk: 34
            
            features:
              - name: "users"
                screens:
                  - name: "UserList"
                    list_type: "LAZY_COLUMN"
                models:
                  - name: "User"
                    properties:
                      id:
                        type: "String"
                      name:
                        type: "String"
                      email:
                        type: "String"
                use_cases:
                  - name: "GetUsers"
                    output_model: "User"
                  - name: "CreateUser"
                    input_model: "User"
        """.trimIndent()
    }

    private fun getDslExample(): String {
        return """
            appName = "MyApp"
            packageName = "com.example.app"
            pattern = ArchitecturePattern.CLEAN_ARCHITECTURE
            uiFramework = UIFramework.COMPOSE
            
            feature("users") {
                description = "User management"
                
                screen("UserList") {
                    useCompose = true
                    listType = ListType.LAZY_COLUMN
                }
                
                model("User") {
                    property("id", "String")
                    property("name", "String")
                    property("email", "String")
                }
                
                useCase("GetUsers") {
                    outputModel = "User"
                }
            }
        """.trimIndent()
    }
}