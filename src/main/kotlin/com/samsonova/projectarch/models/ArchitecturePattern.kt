package com.samsonova.projectarch.models

// ==================== ENUMS ====================

enum class ArchitecturePattern {
    CLEAN_ARCHITECTURE,
    MVVM,
    MVI,
    MVPI
}

enum class UIFramework {
    XML,
    COMPOSE,
    BOTH
}

enum class StorageType {
    ROOM,
    FIREBASE,
    REALM,
    DATASTORE
}

enum class ListType {
    RECYCLER_VIEW,
    LAZY_COLUMN,
    PAGING
}

// ==================== DATA MODELS ====================

data class AppArchitecture(
    val appName: String,
    val packageName: String,
    val pattern: ArchitecturePattern = ArchitecturePattern.CLEAN_ARCHITECTURE,
    val uiFramework: UIFramework = UIFramework.COMPOSE,
    val features: List<Feature>,
    val layers: List<Layer> = emptyList(),
    val storage: StorageType = StorageType.ROOM,
    val useHilt: Boolean = true,
    val useCoroutines: Boolean = true,
    val useFlow: Boolean = true,
    val minSdkVersion: Int = 24,
    val targetSdkVersion: Int = 34,
    val dependencies: List<Dependency> = emptyList()
)

data class Feature(
    val name: String,
    val description: String = "",
    val screens: List<Screen> = emptyList(),
    val models: List<DataModel> = emptyList(),
    val useCases: List<UseCase> = emptyList(),
    val repositories: List<String> = emptyList()
)

data class Screen(
    val name: String,
    val hasViewModel: Boolean = true,
    val hasRepository: Boolean = true,
    val useCompose: Boolean = true,
    val listType: ListType? = null,
    val models: List<String> = emptyList()
)

data class UseCase(
    val name: String,
    val description: String = "",
    val inputModel: String? = null,
    val outputModel: String? = null,
    val isInteractor: Boolean = false
)

data class DataModel(
    val name: String,
    val properties: Map<String, PropertyType> = emptyMap(),
    val isEntity: Boolean = true,
    val isDomain: Boolean = true,
    val isDto: Boolean = true,
    val tableName: String = ""
)

data class PropertyType(
    val type: String,
    val isNullable: Boolean = false,
    val isCollection: Boolean = false,
    val collectionType: String? = null
)

data class Layer(
    val name: String,
    val modules: List<String> = emptyList()
)

data class Dependency(
    val name: String,
    val version: String,
    val scope: String = "implementation"
)

// ==================== RESULT WRAPPER ====================

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }
}