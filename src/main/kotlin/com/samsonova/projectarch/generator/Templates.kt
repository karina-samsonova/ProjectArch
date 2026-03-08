package com.samsonova.projectarch.generator

import com.samsonova.projectarch.models.*

object Templates {

    // ==================== CORE: DATABASE ====================

    fun roomEntity(packagePrefix: String, model: DataModel): String {
        val kdoc = if (model.description.isNotBlank()) {
            """/**
 * ${model.description}
 */
"""
        } else {
            ""
        }

        val properties = model.properties
            .filter { it.key != "id" }
            .map { (name, type) -> "val $name: $type" }
            .joinToString(",\n    ")

        return """
package $packagePrefix.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

$kdoc
@Entity(tableName = "${model.tableName}")
data class ${model.name}Entity(
    @PrimaryKey
    val id: String,
    $properties
)
""".trimIndent()
    }

    fun roomDAO(packagePrefix: String, model: DataModel): String {
        val kdoc = if (model.description.isNotBlank()) {
            """/**
 * ${model.description}
 */
"""
        } else {
            ""
        }

        return """
package $packagePrefix.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

$kdoc
@Dao
interface ${model.name}Dao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ${model.name}Entity)
    
    @Delete
    suspend fun delete(entity: ${model.name}Entity)
    
    @Query("SELECT * FROM ${model.tableName} WHERE id = :id")
    suspend fun getById(id: String): ${model.name}Entity?
    
    @Query("SELECT * FROM ${model.tableName}")
    fun getAll(): Flow<List<${model.name}Entity>>
    
    @Query("DELETE FROM ${model.tableName} WHERE id = :id")
    suspend fun deleteById(id: String)
}
""".trimIndent()
    }

    // ==================== CORE: NETWORK ====================

    fun dto(packagePrefix: String, model: DataModel): String {
        val kdoc = if (model.description.isNotBlank()) {
            """/**
 * ${model.description}
 */
"""
        } else {
            ""
        }

        val properties = model.properties
            .map { (name, type) -> "val $name: $type" }
            .joinToString(",\n    ")

        return """
package $packagePrefix.model

import kotlinx.serialization.Serializable

$kdoc
@Serializable
data class ${model.name}Dto(
    $properties
)
""".trimIndent()
    }

    // ==================== FEATURE: DOMAIN ====================

    fun domainModel(packagePrefix: String, model: DataModel): String {
        val kdoc = if (model.description.isNotBlank()) {
            """/**
 * ${model.description}
 */
"""
        } else {
            ""
        }

        val properties = model.properties
            .map { (name, type) -> "val $name: $type" }
            .joinToString(",\n    ")

        return """
package $packagePrefix.domain.models

$kdoc
data class ${model.name}(
    $properties
)
""".trimIndent()
    }

    fun repositoryInterface(packagePrefix: String, model: DataModel): String {
        val kdoc = if (model.description.isNotBlank()) {
            """/**
 * ${model.description}
 */
"""
        } else {
            ""
        }

        return """
package $packagePrefix.domain.repositories

$kdoc
interface ${model.name}Repository {
    
    suspend fun get${model.name}(): Result<${model.name}?>
    
    suspend fun save${model.name}(${model.name.decapitalize()}: ${model.name}): Result<Unit>
    
    suspend fun delete${model.name}(id: String): Result<Unit>
}
""".trimIndent()
    }

    fun useCase(packagePrefix: String, useCase: UseCase, useHilt: Boolean): String {
        val kdoc = if (useCase.description.isNotBlank()) {
            """/**
 * ${useCase.description}
 */
"""
        } else {
            ""
        }

        val input = if (useCase.inputModel != null) {
            "${useCase.inputModel.decapitalize()}: ${useCase.inputModel}"
        } else {
            ""
        }

        val injection = if (useHilt) {
            "@Inject constructor(\n    // TODO: Inject repositories\n) : Any()"
        } else {
            "constructor()"
        }

        val hiltAnnotation = if (useHilt) "@Inject\n" else ""

        return """
package $packagePrefix.domain.usecases

import javax.inject.Inject

$kdoc
${hiltAnnotation}class ${useCase.name}UseCase $injection {
    
    suspend operator fun invoke($input): Result<${useCase.outputModel ?: "Unit"}?> {
        return try {
            // TODO: Implement business logic
            Result.Success(null)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
""".trimIndent()
    }

    // ==================== FEATURE: DATA ====================

    fun repositoryImpl(
        packagePrefix: String,
        architecture: AppArchitecture,
        module: Module,
        model: DataModel
    ): String {
        val kdoc = if (model.description.isNotBlank()) {
            """/**
 * ${model.description}
 */
"""
        } else {
            ""
        }

        val hiltAnnotation = if (module.useHilt) "@Singleton\n" else ""
        val injection = if (module.useHilt) {
            "@Inject constructor(\n    // TODO: Inject data sources\n)"
        } else {
            "constructor()"
        }

        return """
package $packagePrefix.data.repositories

import javax.inject.Inject
import javax.inject.Singleton

$kdoc
$hiltAnnotation
class ${model.name}RepositoryImpl $injection : ${model.name}Repository {
    
    override suspend fun get${model.name}(): Result<${model.name}?> {
        return try {
            // TODO: Implement
            Result.Success(null)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
""".trimIndent()
    }

    fun localDataSource(packagePrefix: String, model: DataModel, useHilt: Boolean): String {
        val kdoc = if (model.description.isNotBlank()) {
            """/**
 * ${model.description}
 */
"""
        } else {
            ""
        }

        val injection = if (useHilt) "@Inject constructor()" else "constructor()"
        val hiltImport = if (useHilt) "import javax.inject.Inject\n" else ""

        return """
package $packagePrefix.data.datasources

$kdoc
${hiltImport}class ${model.name}LocalDataSource $injection {
    
    suspend fun get${model.name}(): ${model.name}Entity? {
        // TODO: Implement Room query
        return null
    }
    
    suspend fun save${model.name}(entity: ${model.name}Entity) {
        // TODO: Implement Room insert
    }
}
""".trimIndent()
    }

    fun remoteDataSource(packagePrefix: String, model: DataModel, useHilt: Boolean): String {
        val kdoc = if (model.description.isNotBlank()) {
            """/**
 * ${model.description}
 */
"""
        } else {
            ""
        }

        val injection = if (useHilt) "@Inject constructor()" else "constructor()"
        val hiltImport = if (useHilt) "import javax.inject.Inject\n" else ""

        return """
package $packagePrefix.data.datasources

$kdoc
${hiltImport}class ${model.name}RemoteDataSource $injection {
    
    suspend fun fetch${model.name}(): ${model.name}Dto? {
        // TODO: Implement Retrofit call
        return null
    }
}
""".trimIndent()
    }

    // ==================== FEATURE: PRESENTATION ====================

    fun composeScreen(packagePrefix: String, screen: Screen, useHilt: Boolean): String {
        val kdoc = if (screen.description.isNotBlank()) {
            """/**
 * ${screen.description}
 */
"""
        } else {
            ""
        }

        val vmParam = if (screen.hasViewModel) {
            "\n    viewModel: ${screen.name}ViewModel = hiltViewModel()"
        } else {
            ""
        }

        val hiltImport = if (screen.hasViewModel && useHilt) {
            "import androidx.hilt.navigation.compose.hiltViewModel\n"
        } else {
            ""
        }

        return """
package $packagePrefix.presentation.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
$hiltImport

$kdoc
@Composable
fun ${screen.name}Screen($vmParam) {
    Text("${screen.name}")
}
""".trimIndent()
    }

    fun fragment(packagePrefix: String, screen: Screen, useHilt: Boolean): String {
        val kdoc = if (screen.description.isNotBlank()) {
            """/**
 * ${screen.description}
 */
"""
        } else {
            ""
        }

        val hiltAnnotation = if (useHilt) "@AndroidEntryPoint\n" else ""
        val hiltImport = if (useHilt) "import dagger.hilt.android.AndroidEntryPoint\n" else ""
        val viewModelField = if (screen.hasViewModel) {
            "\nprivate val viewModel: ${screen.name}ViewModel by viewModels()"
        } else {
            ""
        }

        val viewModelsImport = if (screen.hasViewModel) {
            "import androidx.fragment.app.viewModels\n"
        } else {
            ""
        }

        return """
package $packagePrefix.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
${viewModelsImport}${hiltImport}

$kdoc
$hiltAnnotation
class ${screen.name}Fragment : Fragment() {
$viewModelField
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // TODO: Implement layout
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
""".trimIndent()
    }

    fun viewModel(packagePrefix: String, screen: Screen, useHilt: Boolean): String {
        val kdoc = if (screen.description.isNotBlank()) {
            """/**
 * ${screen.description}
 */
"""
        } else {
            ""
        }

        val hiltAnnotation = if (useHilt) "@HiltViewModel\n" else ""
        val injection = if (useHilt) "@Inject constructor()" else "constructor()"
        val hiltImport = if (useHilt) {
            "import dagger.hilt.android.lifecycle.HiltViewModel\nimport javax.inject.Inject\n"
        } else {
            ""
        }

        return """
package $packagePrefix.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
${hiltImport}import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

$kdoc
$hiltAnnotation
class ${screen.name}ViewModel $injection : ViewModel() {
    
    private val _uiState = MutableStateFlow<${screen.name}UiState>(${screen.name}UiState.Loading)
    val uiState: StateFlow<${screen.name}UiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        // TODO: Implement data loading
    }
}

sealed class ${screen.name}UiState {
    object Loading : ${screen.name}UiState()
    data class Success(val data: Any) : ${screen.name}UiState()
    data class Error(val message: String) : ${screen.name}UiState()
}
""".trimIndent()
    }

    fun mviViewModel(packagePrefix: String, screen: Screen, useHilt: Boolean): String {
        val kdoc = if (screen.description.isNotBlank()) {
            """/**
 * ${screen.description}
 */
"""
        } else {
            ""
        }

        val hiltAnnotation = if (useHilt) "@HiltViewModel\n" else ""
        val injection = if (useHilt) "@Inject constructor()" else "constructor()"
        val hiltImport = if (useHilt) {
            "import dagger.hilt.android.lifecycle.HiltViewModel\nimport javax.inject.Inject\n"
        } else {
            ""
        }

        return """
package $packagePrefix.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
${hiltImport}import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

$kdoc
$hiltAnnotation
class ${screen.name}ViewModel $injection : ViewModel() {
    
    private val intentFlow = MutableSharedFlow<${screen.name}Intent>()
    private val _state = MutableStateFlow(${screen.name}State())
    val state: StateFlow<${screen.name}State> = _state.asStateFlow()
    
    fun sendIntent(intent: ${screen.name}Intent) {
        // TODO: Handle intent
    }
}

sealed class ${screen.name}Intent {
    object Load : ${screen.name}Intent()
    object Refresh : ${screen.name}Intent()
}

data class ${screen.name}State(
    val isLoading: Boolean = false,
    val data: Any? = null,
    val error: String? = null
)
""".trimIndent()
    }

    fun presenter(packagePrefix: String, screen: Screen, useHilt: Boolean): String {
        val kdoc = if (screen.description.isNotBlank()) {
            """/**
 * ${screen.description}
 */
"""
        } else {
            ""
        }

        val injection = if (useHilt) "@Inject constructor()" else "constructor()"
        val hiltImport = if (useHilt) "import javax.inject.Inject\n" else ""

        return """
package $packagePrefix.presentation.presenters

$kdoc
${hiltImport}class ${screen.name}Presenter $injection : ${screen.name}Contract.Presenter {
    
    private var view: ${screen.name}Contract.View? = null
    
    override fun attach(view: ${screen.name}Contract.View) {
        this.view = view
    }
    
    override fun detach() {
        this.view = null
    }
    
    override fun load() {
        // TODO: Implement
    }
}
""".trimIndent()
    }

    fun contract(packagePrefix: String, screen: Screen): String {
        return """
package $packagePrefix.presentation.contracts

interface ${screen.name}Contract {
    
    interface View {
        fun showLoading()
        fun showContent(data: Any)
        fun showError(message: String)
    }
    
    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun load()
    }
}
""".trimIndent()
    }

    // ==================== DI MODULES ====================

    fun hiltModule(packagePrefix: String, module: Module): String {
        val moduleName = module.name.replaceFirstChar { it.uppercase() }

        return """
package $packagePrefix.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ${moduleName}Module {
    // TODO: Add providers
}
""".trimIndent()
    }

    fun networkModule(packagePrefix: String): String {
        return """
package $packagePrefix.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .build()
    }
}
""".trimIndent()
    }

    fun databaseModule(packagePrefix: String): String {
        return """
package $packagePrefix.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(app: Application) {
        // TODO: Implement database creation
    }
}
""".trimIndent()
    }

    // ==================== DESIGN SYSTEM ====================

    fun theme(): String {
        return """
package com.example.app.design_system.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}
""".trimIndent()
    }
}

// Extension functions
fun String.decapitalize(): String = this.replaceFirstChar { it.lowercase() }