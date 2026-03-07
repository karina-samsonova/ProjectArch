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
            pattern = parsePattern(data["pattern"] as? String),
            uiFramework = parseUIFramework(data["ui_framework"] as? String),
            features = parseFeatures(data["features"] as? List<Map<String, Any>> ?: emptyList()),
            layers = parseLayers(data["layers"] as? List<Map<String, Any>> ?: emptyList()),
            storage = parseStorage(data["storage"] as? String),
            useHilt = data["use_hilt"] as? Boolean ?: true,
            useCoroutines = data["use_coroutines"] as? Boolean ?: true,
            useFlow = data["use_flow"] as? Boolean ?: true,
            minSdkVersion = (data["min_sdk"] as? Number)?.toInt() ?: 24,
            targetSdkVersion = (data["target_sdk"] as? Number)?.toInt() ?: 34,
            dependencies = parseDependencies(data["dependencies"] as? List<Map<String, String>> ?: emptyList())
        )
        
        Result.Success(architecture)
    } catch (e: Exception) {
        Result.Error(Exception("Failed to parse YAML: ${e.message}", e))
    }
    
    private fun parseFeatures(features: List<Map<String, Any>>): List<Feature> {
        return features.map { featureMap ->
            Feature(
                name = featureMap["name"] as? String ?: "Feature",
                description = featureMap["description"] as? String ?: "",
                screens = parseScreens(featureMap["screens"] as? List<Map<String, Any>> ?: emptyList()),
                models = parseModels(featureMap["models"] as? List<Map<String, Any>> ?: emptyList()),
                useCases = parseUseCases(featureMap["use_cases"] as? List<Map<String, Any>> ?: emptyList()),
                repositories = (featureMap["repositories"] as? List<String>) ?: emptyList()
            )
        }
    }
    
    private fun parseScreens(screens: List<Map<String, Any>>): List<Screen> {
        return screens.map { screenMap ->
            Screen(
                name = screenMap["name"] as? String ?: "Screen",
                hasViewModel = screenMap["has_view_model"] as? Boolean ?: true,
                hasRepository = screenMap["has_repository"] as? Boolean ?: true,
                useCompose = screenMap["use_compose"] as? Boolean ?: true,
                listType = (screenMap["list_type"] as? String)?.let { 
                    try { ListType.valueOf(it.uppercase()) } catch (e: Exception) { null }
                },
                models = (screenMap["models"] as? List<String>) ?: emptyList()
            )
        }
    }
    
    private fun parseModels(models: List<Map<String, Any>>): List<DataModel> {
        return models.map { modelMap ->
            DataModel(
                name = modelMap["name"] as? String ?: "Model",
                properties = parseProperties(modelMap["properties"] as? Map<String, Any> ?: emptyMap()),
                isEntity = modelMap["is_entity"] as? Boolean ?: true,
                isDomain = modelMap["is_domain"] as? Boolean ?: true,
                isDto = modelMap["is_dto"] as? Boolean ?: true,
                tableName = modelMap["table_name"] as? String ?: ""
            )
        }
    }
    
    private fun parseProperties(props: Map<String, Any>): Map<String, PropertyType> {
        return props.mapValues { (_, value) ->
            when (value) {
                is String -> PropertyType(value)
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val propMap = value as Map<String, Any>
                    PropertyType(
                        type = propMap["type"] as? String ?: "String",
                        isNullable = propMap["nullable"] as? Boolean ?: false,
                        isCollection = propMap["collection"] as? Boolean ?: false,
                        collectionType = propMap["collection_type"] as? String
                    )
                }
                else -> PropertyType("String")
            }
        }
    }
    
    private fun parseUseCases(useCases: List<Map<String, Any>>): List<UseCase> {
        return useCases.map { ucMap ->
            UseCase(
                name = ucMap["name"] as? String ?: "UseCase",
                description = ucMap["description"] as? String ?: "",
                inputModel = ucMap["input_model"] as? String,
                outputModel = ucMap["output_model"] as? String,
                isInteractor = ucMap["is_interactor"] as? Boolean ?: false
            )
        }
    }
    
    private fun parseLayers(layers: List<Map<String, Any>>): List<Layer> {
        return layers.map { layerMap ->
            Layer(
                name = layerMap["name"] as? String ?: "Layer",
                modules = (layerMap["modules"] as? List<String>) ?: emptyList()
            )
        }
    }
    
    private fun parseDependencies(deps: List<Map<String, String>>): List<Dependency> {
        return deps.map { depMap ->
            Dependency(
                name = depMap["name"] ?: "",
                version = depMap["version"] ?: "",
                scope = depMap["scope"] ?: "implementation"
            )
        }
    }
    
    private fun parsePattern(pattern: String?): ArchitecturePattern {
        return try {
            ArchitecturePattern.valueOf((pattern ?: "CLEAN_ARCHITECTURE").uppercase())
        } catch (e: Exception) {
            ArchitecturePattern.CLEAN_ARCHITECTURE
        }
    }
    
    private fun parseUIFramework(framework: String?): UIFramework {
        return try {
            UIFramework.valueOf((framework ?: "COMPOSE").uppercase())
        } catch (e: Exception) {
            UIFramework.COMPOSE
        }
    }
    
    private fun parseStorage(storage: String?): StorageType {
        return try {
            StorageType.valueOf((storage ?: "ROOM").uppercase())
        } catch (e: Exception) {
            StorageType.ROOM
        }
    }
}