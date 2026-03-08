package com.samsonova.projectarch.parser

import com.samsonova.projectarch.models.*
import org.yaml.snakeyaml.Yaml

class YamlParser {

    fun parse(content: String): Result<AppArchitecture> = try {
        val yaml = Yaml()
        val data = yaml.load<Map<String, Any>>(content) ?: emptyMap()

        val architecture = AppArchitecture(
            appName = data["app_name"] as? String ?: "MyApp",
            packageName = data["package_name"] as? String ?: "com.example.app",
            modules = parseModules(data["modules"] as? List<Map<String, Any>> ?: emptyList())
        )

        Result.Success(architecture)
    } catch (e: Exception) {
        Result.Error(Exception("Failed to parse YAML: ${e.message}", e))
    }

    private fun parseModules(modules: List<Map<String, Any>>): List<Module> {
        return modules.map { moduleMap ->
            val type = parseModuleType(moduleMap["type"] as? String)

            Module(
                name = moduleMap["name"] as? String ?: "module",
                type = type,
                description = moduleMap["description"] as? String ?: "",

                architecture = if (type == ModuleType.FEATURE) {
                    parseArchitecture(moduleMap["architecture"] as? Map<String, Any>)
                } else {
                    null
                },

                screens = if (type == ModuleType.FEATURE) {
                    parseScreens(moduleMap["screens"] as? List<Map<String, Any>> ?: emptyList())
                } else {
                    emptyList()
                },

                models = if (type == ModuleType.FEATURE) {
                    parseModels(moduleMap["models"] as? List<Map<String, Any>> ?: emptyList())
                } else {
                    emptyList()
                },

                useCases = if (type == ModuleType.FEATURE) {
                    parseUseCases(moduleMap["use_cases"] as? List<Map<String, Any>> ?: emptyList())
                } else {
                    emptyList()
                },

                localStorage = if (type == ModuleType.FEATURE) {
                    parseLocalStorageType(moduleMap["local_storage"] as? String)
                } else {
                    LocalStorageType.NONE
                },

                remoteStorage = if (type == ModuleType.FEATURE) {
                    parseRemoteStorageType(moduleMap["remote_storage"] as? String)
                } else {
                    RemoteStorageType.NONE
                },

                useHilt = moduleMap["use_hilt"] as? Boolean ?: true,

                dependencies = (moduleMap["dependencies"] as? List<String>) ?: emptyList()
            )
        }
    }

    private fun parseArchitecture(arch: Map<String, Any>?): FeatureArchitecture {
        if (arch == null) return FeatureArchitecture()

        val pattern = try {
            ArchitecturePattern.valueOf((arch["pattern"] as? String ?: "CLEAN_ARCHITECTURE").uppercase())
        } catch (e: Exception) {
            ArchitecturePattern.CLEAN_ARCHITECTURE
        }

        return FeatureArchitecture(
            pattern = pattern,
            useLayers = arch["use_layers"] as? Boolean ?: (pattern == ArchitecturePattern.CLEAN_ARCHITECTURE)
        )
    }

    private fun parseScreens(screens: List<Map<String, Any>>): List<Screen> {
        return screens.map { screenMap ->
            Screen(
                name = screenMap["name"] as? String ?: "Screen",
                description = screenMap["description"] as? String ?: "",
                uiFramework = try {
                    UIFramework.valueOf((screenMap["ui"] as? String ?: "COMPOSE").uppercase())
                } catch (e: Exception) {
                    UIFramework.COMPOSE
                },
                pattern = try {
                    ArchitecturePattern.valueOf((screenMap["pattern"] as? String ?: "CLEAN_ARCHITECTURE").uppercase())
                } catch (e: Exception) {
                    ArchitecturePattern.CLEAN_ARCHITECTURE
                },
                hasViewModel = screenMap["view_model"] as? Boolean ?: true
            )
        }
    }

    private fun parseModels(models: List<Map<String, Any>>): List<DataModel> {
        return models.map { modelMap ->
            DataModel(
                name = modelMap["name"] as? String ?: "Model",
                description = modelMap["description"] as? String ?: "",
                properties = (modelMap["properties"] as? Map<String, String>) ?: emptyMap(),
                generateEntity = modelMap["entity"] as? Boolean ?: true,
                generateDTO = modelMap["dto"] as? Boolean ?: true,
                tableName = modelMap["table"] as? String ?: "${(modelMap["name"] as? String ?: "model").lowercase()}_table"
            )
        }
    }

    private fun parseUseCases(useCases: List<Map<String, Any>>): List<UseCase> {
        return useCases.map { ucMap ->
            UseCase(
                name = ucMap["name"] as? String ?: "UseCase",
                description = ucMap["description"] as? String ?: "",
                inputModel = ucMap["input"] as? String,
                outputModel = ucMap["output"] as? String
            )
        }
    }

    private fun parseModuleType(type: String?): ModuleType {
        return try {
            ModuleType.valueOf((type ?: "FEATURE").uppercase().replace("-", "_"))
        } catch (e: Exception) {
            ModuleType.FEATURE
        }
    }

    private fun parseLocalStorageType(type: String?): LocalStorageType {
        return try {
            LocalStorageType.valueOf((type ?: "NONE").uppercase())
        } catch (e: Exception) {
            LocalStorageType.NONE
        }
    }

    private fun parseRemoteStorageType(type: String?): RemoteStorageType {
        return try {
            RemoteStorageType.valueOf((type ?: "NONE").uppercase())
        } catch (e: Exception) {
            RemoteStorageType.NONE
        }
    }
}