package com.samsonova.projectarch.generator

import com.intellij.openapi.project.Project
import com.samsonova.projectarch.parcer.AppArchitecture
import com.samsonova.projectarch.parcer.DataModel
import com.samsonova.projectarch.parcer.Feature
import java.io.File

class ArchitectureGenerator(val project: Project?) {

    fun generate(architecture: AppArchitecture) {
        val baseDir = project?.basePath?.let { File(it) } ?: return
        val srcDir = File(baseDir, "app/src/main/kotlin/${architecture.packageName.replace(".", "/")}")
        srcDir.mkdirs()

        architecture.features.forEach { feature ->
            generateCleanArchitectureFeature(srcDir, architecture.packageName, feature)
        }
    }

    private fun generateCleanArchitectureFeature(
        baseDir: File,
        packageName: String,
        feature: Feature
    ) {
        val featureDir = File(baseDir, feature.name.lowercase())

        // Presentation Layer
        val presentationDir = File(featureDir, "presentation")
        presentationDir.mkdirs()

        // Domain Layer
        val domainDir = File(featureDir, "domain")
        domainDir.mkdirs()

        // Data Layer
        val dataDir = File(featureDir, "data")
        dataDir.mkdirs()

        feature.screens.forEach { screen ->
            // Presentation
            generateViewModel(presentationDir, packageName, feature.name, screen.name)
            generateScreen(presentationDir, packageName, feature.name, screen.name)
            generateUiState(presentationDir, packageName, feature.name, screen.name)
        }

        // Domain
        feature.models.forEach { model ->
            generateRepositoryInterface(domainDir, packageName, feature.name, model.name)
            generateUseCases(domainDir, packageName, feature.name, model.name)
        }

        // Data
        feature.models.forEach { model ->
            generateDataModel(dataDir, packageName, feature.name, model)
            generateRepositoryImpl(dataDir, packageName, feature.name, model.name)
            generateDataSource(dataDir, packageName, feature.name, model.name)
        }
    }

    // ===== PRESENTATION LAYER =====

    private fun generateViewModel(
        dir: File,
        packageName: String,
        featureName: String,
        screenName: String
    ) {
        val className = "${screenName}ViewModel"
        val code = """
            package $packageName.${featureName.lowercase()}.presentation
            
            import androidx.lifecycle.ViewModel
            import androidx.lifecycle.viewModelScope
            import dagger.hilt.android.lifecycle.HiltViewModel
            import kotlinx.coroutines.flow.MutableStateFlow
            import kotlinx.coroutines.flow.StateFlow
            import kotlinx.coroutines.launch
            import javax.inject.Inject
            
            @HiltViewModel
            class $className @Inject constructor(
                // Inject use cases here
            ) : ViewModel() {
                
                private val _uiState = MutableStateFlow<${screenName}UiState>(${screenName}UiState.Loading)
                val uiState: StateFlow<${screenName}UiState> = _uiState
                
                init {
                    loadData()
                }
                
                private fun loadData() {
                    viewModelScope.launch {
                        _uiState.value = ${screenName}UiState.Loading
                        try {
                            // Call use case here
                            _uiState.value = ${screenName}UiState.Success(Unit)
                        } catch (e: Exception) {
                            _uiState.value = ${screenName}UiState.Error(e.message ?: "Unknown error")
                        }
                    }
                }
            }
        """.trimIndent()

        val file = File(dir, "$className.kt")
        file.writeText(code)
    }

    private fun generateScreen(
        dir: File,
        packageName: String,
        featureName: String,
        screenName: String
    ) {
        val code = """
            package $packageName.${featureName.lowercase()}.presentation
            
            import androidx.compose.material3.CircularProgressIndicator
            import androidx.compose.material3.Scaffold
            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.collectAsState
            import androidx.compose.runtime.getValue
            import androidx.hilt.navigation.compose.hiltViewModel
            
            @Composable
            fun ${screenName}Screen(
                viewModel: ${screenName}ViewModel = hiltViewModel()
            ) {
                val uiState by viewModel.uiState.collectAsState()
                
                Scaffold { paddingValues ->
                    when (uiState) {
                        is ${screenName}UiState.Loading -> CircularProgressIndicator()
                        is ${screenName}UiState.Success -> {
                            // Render success state
                            Text("${screenName} Screen")
                        }
                        is ${screenName}UiState.Error -> {
                            val error = (uiState as ${screenName}UiState.Error).message
                            Text("Error: ${'$'}error")
                        }
                    }
                }
            }
        """.trimIndent()

        val file = File(dir, "${screenName}Screen.kt")
        file.writeText(code)
    }

    private fun generateUiState(
        dir: File,
        packageName: String,
        featureName: String,
        screenName: String
    ) {
        val code = """
            package $packageName.${featureName.lowercase()}.presentation
            
            sealed class ${screenName}UiState {
                object Loading : ${screenName}UiState()
                data class Success(val data: Any) : ${screenName}UiState()
                data class Error(val message: String) : ${screenName}UiState()
            }
        """.trimIndent()

        val file = File(dir, "${screenName}UiState.kt")
        file.writeText(code)
    }

    // ===== DOMAIN LAYER =====

    private fun generateRepositoryInterface(
        dir: File,
        packageName: String,
        featureName: String,
        modelName: String
    ) {
        val code = """
            package $packageName.${featureName.lowercase()}.domain
            
            interface ${modelName}Repository {
                suspend fun get${modelName}(): Result<${modelName}>
                suspend fun save${modelName}(${modelName.lowercase()}: ${modelName}): Result<Unit>
                suspend fun delete${modelName}(id: String): Result<Unit>
            }
        """.trimIndent()

        val file = File(dir, "${modelName}Repository.kt")
        file.writeText(code)
    }

    private fun generateUseCases(
        dir: File,
        packageName: String,
        featureName: String,
        modelName: String
    ) {
        val code = """
            package $packageName.${featureName.lowercase()}.domain
            
            import javax.inject.Inject
            
            class Get${modelName}UseCase @Inject constructor(
                private val repository: ${modelName}Repository
            ) {
                suspend operator fun invoke(): Result<${modelName}> {
                    return repository.get${modelName}()
                }
            }
            
            class Save${modelName}UseCase @Inject constructor(
                private val repository: ${modelName}Repository
            ) {
                suspend operator fun invoke(${modelName.lowercase()}: ${modelName}): Result<Unit> {
                    return repository.save${modelName}(${modelName.lowercase()})
                }
            }
            
            sealed class Result<out T> {
                data class Success<T>(val data: T) : Result<T>()
                data class Error(val exception: Exception) : Result<Nothing>()
            }
        """.trimIndent()

        val file = File(dir, "${modelName}UseCases.kt")
        file.writeText(code)
    }

    // ===== DATA LAYER =====

    private fun generateDataModel(
        dir: File,
        packageName: String,
        featureName: String,
        model: DataModel
    ) {
        val properties = model.properties
            .map { (name, type) -> "    val $name: $type" }
            .joinToString(",\n")

        val code = """
            package $packageName.${featureName.lowercase()}.data
            
            import androidx.room.Entity
            import androidx.room.PrimaryKey
            import kotlinx.serialization.Serializable
            
            @Serializable
            @Entity(tableName = "${model.name.lowercase()}_table")
            data class ${model.name}Entity(
                @PrimaryKey
                val id: String,
            $properties
            )
            
            // Domain model (для использования в других слоях)
            data class ${model.name}(
            $properties
            ) {
                val id: String = ""
            }
        """.trimIndent()

        val file = File(dir, "${model.name}Entity.kt")
        file.writeText(code)
    }

    private fun generateRepositoryImpl(
        dir: File,
        packageName: String,
        featureName: String,
        modelName: String
    ) {
        val code = """
            package $packageName.${featureName.lowercase()}.data
            
            import javax.inject.Inject
            import javax.inject.Singleton
            
            @Singleton
            class ${modelName}RepositoryImpl @Inject constructor(
                private val localDataSource: ${modelName}LocalDataSource,
                private val remoteDataSource: ${modelName}RemoteDataSource
            ) : ${modelName}Repository {
                
                override suspend fun get${modelName}(): Result<${modelName}> {
                    return try {
                        val data = localDataSource.get${modelName}()
                        Result.Success(data)
                    } catch (e: Exception) {
                        Result.Error(e)
                    }
                }
                
                override suspend fun save${modelName}(${modelName.lowercase()}: ${modelName}): Result<Unit> {
                    return try {
                        localDataSource.save${modelName}(${modelName.lowercase()}.toEntity())
                        remoteDataSource.sync${modelName}(${modelName.lowercase()})
                        Result.Success(Unit)
                    } catch (e: Exception) {
                        Result.Error(e)
                    }
                }
                
                override suspend fun delete${modelName}(id: String): Result<Unit> {
                    return try {
                        localDataSource.delete${modelName}(id)
                        Result.Success(Unit)
                    } catch (e: Exception) {
                        Result.Error(e)
                    }
                }
                
                private fun ${modelName}.toEntity() = ${modelName}Entity(
                    id = this.id,
                    // Map other properties
                )
            }
        """.trimIndent()

        val file = File(dir, "${modelName}RepositoryImpl.kt")
        file.writeText(code)
    }

    private fun generateDataSource(
        dir: File,
        packageName: String,
        featureName: String,
        modelName: String
    ) {
        val localCode = """
            package $packageName.${featureName.lowercase()}.data
            
            import javax.inject.Inject
            
            class ${modelName}LocalDataSource @Inject constructor(
                // Inject Room DAO
            ) {
                suspend fun get${modelName}(): ${modelName} {
                    // Get from local database
                    return ${modelName}(id = "1")
                }
                
                suspend fun save${modelName}(entity: ${modelName}Entity) {
                    // Save to local database
                }
                
                suspend fun delete${modelName}(id: String) {
                    // Delete from local database
                }
            }
        """.trimIndent()

        val remoteCode = """
            package $packageName.${featureName.lowercase()}.data
            
            import javax.inject.Inject
            
            class ${modelName}RemoteDataSource @Inject constructor(
                // Inject Retrofit API
            ) {
                suspend fun sync${modelName}(${modelName.lowercase()}: ${modelName}) {
                    // Sync with remote server
                }
            }
        """.trimIndent()

        File(dir, "${modelName}LocalDataSource.kt").writeText(localCode)
        File(dir, "${modelName}RemoteDataSource.kt").writeText(remoteCode)
    }
}