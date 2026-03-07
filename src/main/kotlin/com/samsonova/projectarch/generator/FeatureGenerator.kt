package com.samsonova.projectarch.generator

import com.samsonova.projectarch.generator.templates.GradleTemplate
import com.samsonova.projectarch.generator.templates.PresenterTemplate
import com.samsonova.projectarch.generator.templates.RepositoryTemplate
import com.samsonova.projectarch.generator.templates.UseCaseTemplate
import com.samsonova.projectarch.generator.templates.ui.*
import com.samsonova.projectarch.models.*
import java.io.File

object FeatureGenerator {
    
    // ==================== CLEAN ARCHITECTURE ====================
    
    fun generateCleanArchitecture(
        baseDir: File,
        architecture: AppArchitecture,
        feature: Feature
    ) {
        val featureDir = File(baseDir, feature.name.lowercase())
        
        val presentationDir = File(featureDir, "presentation").apply { mkdirs() }
        val domainDir = File(featureDir, "domain").apply { mkdirs() }
        val dataDir = File(featureDir, "data").apply { mkdirs() }
        
        // Presentation Layer
        feature.screens.forEach { screen ->
            if (architecture.uiFramework in listOf(UIFramework.COMPOSE, UIFramework.BOTH)) {
                val code = ComposeScreenTemplate.generate(architecture, feature, screen)
                File(presentationDir, "${screen.name}Screen.kt").writeText(code)
            }
            
            if (architecture.uiFramework in listOf(UIFramework.XML, UIFramework.BOTH)) {
                val code = XmlFragmentTemplate.generate(architecture, feature, screen)
                File(presentationDir, "${screen.name}Fragment.kt").writeText(code)
                
                if (screen.listType == ListType.RECYCLER_VIEW) {
                    val adapterCode = RecyclerViewAdapterTemplate.generate(architecture, feature, screen)
                    File(presentationDir, "${screen.name}Adapter.kt").writeText(adapterCode)
                }
            }
            
            val viewModelCode = ViewModelTemplate.generate(architecture, feature, screen)
            File(presentationDir, "${screen.name}ViewModel.kt").writeText(viewModelCode)
            
            val uiStateCode = UiStateTemplate.generate(feature, screen)
            File(presentationDir, "${screen.name}UiState.kt").writeText(uiStateCode)
        }
        
        // Domain Layer
        feature.models.forEach { model ->
            val repositoryCode = RepositoryTemplate.generateInterface(architecture, feature, model)
            File(domainDir, "${model.name}Repository.kt").writeText(repositoryCode)
            
            val domainModelCode = RepositoryTemplate.generateDomainModel(feature, model)
            File(domainDir, "${model.name}.kt").writeText(domainModelCode)
        }
        
        feature.useCases.forEach { useCase ->
            val useCaseCode = UseCaseTemplate.generate(architecture, feature, useCase)
            File(domainDir, "${useCase.name}UseCase.kt").writeText(useCaseCode)
        }
        
        // Data Layer
        feature.models.forEach { model ->
            if (architecture.storage == StorageType.ROOM) {
                val entityCode = RepositoryTemplate.generateRoomEntity(architecture, feature, model)
                File(dataDir, "${model.name}Entity.kt").writeText(entityCode)
                
                val daoCode = RepositoryTemplate.generateDAO(architecture, feature, model)
                File(dataDir, "${model.name}Dao.kt").writeText(daoCode)
            }
            
            val dtoCode = RepositoryTemplate.generateDTO(architecture, feature, model)
            File(dataDir, "${model.name}Dto.kt").writeText(dtoCode)
            
            val repoImplCode = RepositoryTemplate.generateImplementation(architecture, feature, model)
            File(dataDir, "${model.name}RepositoryImpl.kt").writeText(repoImplCode)
            
            val localDataSourceCode = RepositoryTemplate.generateLocalDataSource(architecture, feature, model)
            File(dataDir, "${model.name}LocalDataSource.kt").writeText(localDataSourceCode)
            
            val remoteDataSourceCode = RepositoryTemplate.generateRemoteDataSource(architecture, feature, model)
            File(dataDir, "${model.name}RemoteDataSource.kt").writeText(remoteDataSourceCode)
        }
    }
    
    // ==================== MVVM ====================
    
    fun generateMVVM(
        baseDir: File,
        architecture: AppArchitecture,
        feature: Feature
    ) {
        val featureDir = File(baseDir, feature.name.lowercase()).apply { mkdirs() }
        
        feature.screens.forEach { screen ->
            if (architecture.uiFramework in listOf(UIFramework.COMPOSE, UIFramework.BOTH)) {
                val code = ComposeScreenTemplate.generate(architecture, feature, screen)
                File(featureDir, "${screen.name}Screen.kt").writeText(code)
            }
            
            if (architecture.uiFramework in listOf(UIFramework.XML, UIFramework.BOTH)) {
                val code = XmlFragmentTemplate.generate(architecture, feature, screen)
                File(featureDir, "${screen.name}Fragment.kt").writeText(code)
            }
            
            val viewModelCode = ViewModelTemplate.generate(architecture, feature, screen)
            File(featureDir, "${screen.name}ViewModel.kt").writeText(viewModelCode)
        }
    }
    
    // ==================== MVI ====================
    
    fun generateMVI(
        baseDir: File,
        architecture: AppArchitecture,
        feature: Feature
    ) {
        val featureDir = File(baseDir, feature.name.lowercase()).apply { mkdirs() }
        
        feature.screens.forEach { screen ->
            val intentCode = """
                sealed class ${screen.name}Intent {
                    object Load : ${screen.name}Intent()
                    object Refresh : ${screen.name}Intent()
                }
            """.trimIndent()
            File(featureDir, "${screen.name}Intent.kt").writeText(intentCode)
            
            val stateCode = """
                data class ${screen.name}State(
                    val isLoading: Boolean = false,
                    val data: Any? = null,
                    val error: String? = null
                )
            """.trimIndent()
            File(featureDir, "${screen.name}State.kt").writeText(stateCode)
            
            val viewModelCode = ViewModelTemplate.generate(architecture, feature, screen)
            File(featureDir, "${screen.name}ViewModel.kt").writeText(viewModelCode)
        }
    }
    
    // ==================== MVPI ====================
    
    fun generateMVPI(
        baseDir: File,
        architecture: AppArchitecture,
        feature: Feature
    ) {
        val featureDir = File(baseDir, feature.name.lowercase()).apply { mkdirs() }
        val presenterDir = File(featureDir, "presenter").apply { mkdirs() }
        val interactorDir = File(featureDir, "interactor").apply { mkdirs() }
        
        feature.screens.forEach { screen ->
            val presenterCode = PresenterTemplate.generate(architecture, feature, screen)
            File(presenterDir, "${screen.name}Presenter.kt").writeText(presenterCode)
            
            val contractCode = PresenterTemplate.generateContract(feature, screen)
            File(featureDir, "${screen.name}Contract.kt").writeText(contractCode)
        }
        
        feature.useCases.forEach { useCase ->
            val interactorCode = PresenterTemplate.generateInteractor(architecture, feature, useCase)
            File(interactorDir, "${useCase.name}Interactor.kt").writeText(interactorCode)
        }
    }
    
    // ==================== HILT MODULES ====================
    
    fun generateHiltModules(
        baseDir: File,
        architecture: AppArchitecture
    ) {
        val modulesDir = File(baseDir, "di").apply { mkdirs() }
        
        // App Module
        val appModuleCode = """
            package ${architecture.packageName}.di
            
            import android.app.Application
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import javax.inject.Singleton
            
            @Module
            @InstallIn(SingletonComponent::class)
            object AppModule {
                
                @Provides
                @Singleton
                fun provideApplication(): Application {
                    return Application()
                }
            }
        """.trimIndent()
        File(modulesDir, "AppModule.kt").writeText(appModuleCode)
        
        // Database Module
        if (architecture.storage == StorageType.ROOM) {
            val dbModuleCode = """
                package ${architecture.packageName}.di
                
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
                    fun provideDatabase(app: Application): AppDatabase {
                        return Room.databaseBuilder(
                            app,
                            AppDatabase::class.java,
                            "${architecture.appName.lowercase()}_db"
                        ).build()
                    }
                }
            """.trimIndent()
            File(modulesDir, "DatabaseModule.kt").writeText(dbModuleCode)
        }
        
        // Network Module
        val networkModuleCode = """
            package ${architecture.packageName}.di
            
            import dagger.Module
            import dagger.Provides
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import retrofit2.Retrofit
            import retrofit2.converter.kotlinx.serialization.asConverterFactory
            import kotlinx.serialization.json.Json
            import okhttp3.MediaType.Companion.toMediaType
            import javax.inject.Singleton
            
            @Module
            @InstallIn(SingletonComponent::class)
            object NetworkModule {
                
                @Provides
                @Singleton
                fun provideRetrofit(): Retrofit {
                    return Retrofit.Builder()
                        .baseUrl("https://api.example.com/")
                        .addConverterFactory(
                            Json.asConverterFactory("application/json".toMediaType())
                        )
                        .build()
                }
            }
        """.trimIndent()
        File(modulesDir, "NetworkModule.kt").writeText(networkModuleCode)
    }
    
    // ==================== GRADLE & MANIFEST ====================
    
    fun generateGradleFile(baseDir: File, architecture: AppArchitecture) {
        val gradleContent = GradleTemplate.generate(architecture)
        File(baseDir, "build.gradle.kts").writeText(gradleContent)
    }
    
    /*fun generateManifestFile(baseDir: File, architecture: AppArchitecture) {
        val manifestContent = """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                
                <uses-permission android:name="android.permission.INTERNET" />
                
                <application
                    android:label="@string/app_name"
                    android:theme="@style/Theme.AppCompat">
                    
                    <activity
                        android:name=".MainActivity"
                        android:exported="true">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                        </intent-filter>
                    </activity>
                    
                </application>
                
            </manifest>
        """.trimIndent()
        
        val manifestDir = File(baseDir, "app/src/main").apply { mkdirs() }
        File(manifestDir, "AndroidManifest.xml").writeText(manifestContent)
    }*/
}