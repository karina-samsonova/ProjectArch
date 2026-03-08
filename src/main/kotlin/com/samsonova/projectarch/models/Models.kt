package com.samsonova.projectarch.models

enum class ArchitecturePattern {
    CLEAN_ARCHITECTURE,
    MVVM,
    MVI,
    MVPI
}

enum class UIFramework {
    COMPOSE,
    VIEW,
    BOTH
}

enum class LocalStorageType {
    ROOM,
    DATASTORE,
    NONE
}

enum class RemoteStorageType {
    RETROFIT,
    FIREBASE,
    NONE
}

data class AppArchitecture(
    val appName: String,
    val packageName: String,
    val modules: List<Module>
)

data class Module(
    val name: String,
    val type: ModuleType,
    val description: String = "",

    val architecture: FeatureArchitecture? = null,
    val screens: List<Screen> = emptyList(),
    val models: List<DataModel> = emptyList(),
    val useCases: List<UseCase> = emptyList(),

    val localStorage: LocalStorageType = LocalStorageType.NONE,
    val remoteStorage: RemoteStorageType = RemoteStorageType.NONE,

    val useHilt: Boolean = true,

    val dependencies: List<String> = emptyList()
)

enum class ModuleType {
    FEATURE,      // features:auth, features:posts
    CORE,         // core:network, core:database, core:ui
    DESIGN_SYSTEM // design-system
}

data class FeatureArchitecture(
    val pattern: ArchitecturePattern = ArchitecturePattern.CLEAN_ARCHITECTURE,
    val useLayers: Boolean = pattern == ArchitecturePattern.CLEAN_ARCHITECTURE
)

data class Screen(
    val name: String,
    val description: String = "",
    val uiFramework: UIFramework = UIFramework.COMPOSE,
    val pattern: ArchitecturePattern = ArchitecturePattern.CLEAN_ARCHITECTURE,
    val hasViewModel: Boolean = true
)

data class DataModel(
    val name: String,
    val description: String = "",
    val properties: Map<String, String> = emptyMap(),
    val generateEntity: Boolean = true,
    val generateDTO: Boolean = true,
    val tableName: String = ""
)

data class UseCase(
    val name: String,
    val description: String = "",
    val inputModel: String? = null,
    val outputModel: String? = null
)

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}