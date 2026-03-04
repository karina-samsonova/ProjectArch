package com.samsonova.projectarch.parcer

import org.yaml.snakeyaml.Yaml

data class AppArchitecture(
    val appName: String,
    val packageName: String,
    val features: List<Feature>,
    val layers: List<Layer>
)

data class Feature(
    val name: String,
    val screens: List<Screen>,
    val models: List<DataModel>
)

data class Screen(
    val name: String,
    val hasViewModel: Boolean,
    val hasRepository: Boolean
)

data class Layer(
    val name: String,
    val modules: List<String>
)

data class DataModel(
    val name: String,
    val properties: Map<String, String>
)

class YamlDslParser {
    fun parse(yaml: String): AppArchitecture {
        val yamlObject = Yaml().load<Map<String, Any>>(yaml)
        
        return AppArchitecture(
            appName = yamlObject["app_name"] as String,
            packageName = yamlObject["package_name"] as String,
            features = parseFeatures(yamlObject["features"] as List<Map<String, Any>>),
            layers = parseLayers(yamlObject["layers"] as List<Map<String, String>>)
        )
    }
    
    private fun parseFeatures(features: List<Map<String, Any>>): List<Feature> {
        return features.map { featureMap ->
            Feature(
                name = featureMap["name"] as String,
                screens = (featureMap["screens"] as? List<Map<String, Any>> ?: emptyList())
                    .map { Screen(it["name"] as String, true, true) },
                models = parseModels(featureMap["models"] as? List<Map<String, Any>> ?: emptyList())
            )
        }
    }
    
    private fun parseModels(models: List<Map<String, Any>>): List<DataModel> {
        return models.map {
            DataModel(it["name"] as String, it["properties"] as Map<String, String>)
        }
    }
    
    private fun parseLayers(layers: List<Map<String, String>>): List<Layer> {
        return layers.map { Layer(it["name"] ?: "", it["modules"]?.split(",") ?: emptyList()) }
    }
}