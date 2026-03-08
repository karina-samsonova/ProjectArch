package com.samsonova.projectarch.generator

import com.samsonova.projectarch.models.*
import java.io.File

class CodeGenerator {

    fun generate(
        architecture: AppArchitecture,
        baseDir: File
    ): Result<Unit> = try {
        val srcDir = File(baseDir, "app/src/main/kotlin/${architecture.packageName.replace(".", "/")}")

        val allModels = architecture.modules
            .filterIsInstance<Module>()
            .filter { it.type == ModuleType.FEATURE }
            .flatMap { it.models }

        val coreModules = architecture.modules.filter { it.type == ModuleType.CORE }
        if (coreModules.isNotEmpty()) {
            generateCoreModules(srcDir, architecture, coreModules, allModels)
        }

        val designSystem = architecture.modules.find { it.type == ModuleType.DESIGN_SYSTEM }
        if (designSystem != null) {
            generateDesignSystemModule(srcDir, architecture, designSystem)
        }

        architecture.modules
            .filter { it.type == ModuleType.FEATURE }
            .forEach { module ->
                generateFeatureModule(srcDir, architecture, module)
            }

        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(Exception("Code generation failed: ${e.message}", e))
    }

    private fun generateFeatureModule(
        baseDir: File,
        architecture: AppArchitecture,
        module: Module
    ) {
        val moduleDir = File(baseDir, "features/${module.name}")
        val packagePrefix = "${architecture.packageName}.features.${module.name}"
        val arch = module.architecture ?: return

        when (arch.pattern) {
            ArchitecturePattern.CLEAN_ARCHITECTURE -> {
                generateCleanArchitecture(moduleDir, packagePrefix, architecture, module)
            }
            ArchitecturePattern.MVVM -> {
                generateMVVM(moduleDir, packagePrefix, architecture, module)
            }
            ArchitecturePattern.MVI -> {
                generateMVI(moduleDir, packagePrefix, architecture, module)
            }
            ArchitecturePattern.MVPI -> {
                generateMVPI(moduleDir, packagePrefix, architecture, module)
            }
        }

        if (module.useHilt) {
            generateDI(moduleDir, packagePrefix, module)
        }
    }

    private fun generateCleanArchitecture(
        moduleDir: File,
        packagePrefix: String,
        architecture: AppArchitecture,
        module: Module
    ) {
        val dataDir = File(moduleDir, "data").apply { mkdirs() }
        val domainDir = File(moduleDir, "domain").apply { mkdirs() }
        val presentationDir = File(moduleDir, "presentation").apply { mkdirs() }

        module.models.forEach { model ->
            File(domainDir, "repositories").mkdirs()
            val repoInterface = Templates.repositoryInterface(packagePrefix, model)
            File(File(domainDir, "repositories"), "${model.name}Repository.kt").writeText(repoInterface)

            File(domainDir, "models").mkdirs()
            val domainCode = Templates.domainModel(packagePrefix, model)
            File(File(domainDir, "models"), "${model.name}.kt").writeText(domainCode)
        }

        module.useCases.forEach { useCase ->
            File(domainDir, "usecases").mkdirs()
            val useCaseCode = Templates.useCase(packagePrefix, useCase, module.useHilt)
            File(File(domainDir, "usecases"), "${useCase.name}UseCase.kt").writeText(useCaseCode)
        }

        module.models.forEach { model ->
            File(dataDir, "repositories").mkdirs()
            val repoImpl = Templates.repositoryImpl(packagePrefix, architecture, module, model)
            File(File(dataDir, "repositories"), "${model.name}RepositoryImpl.kt").writeText(repoImpl)

            File(dataDir, "datasources").mkdirs()
            if (module.localStorage != LocalStorageType.NONE) {
                val localCode = Templates.localDataSource(packagePrefix, model, module.useHilt)
                File(File(dataDir, "datasources"), "${model.name}LocalDataSource.kt").writeText(localCode)
            }
            if (module.remoteStorage != RemoteStorageType.NONE) {
                val remoteCode = Templates.remoteDataSource(packagePrefix, model, module.useHilt)
                File(File(dataDir, "datasources"), "${model.name}RemoteDataSource.kt").writeText(remoteCode)
            }
        }

        generatePresentationLayer(presentationDir, packagePrefix, architecture, module)
    }

    private fun generateMVVM(
        moduleDir: File,
        packagePrefix: String,
        architecture: AppArchitecture,
        module: Module
    ) {
        val presentationDir = File(moduleDir, "presentation").apply { mkdirs() }
        generatePresentationLayer(presentationDir, packagePrefix, architecture, module)
    }

    private fun generateMVI(
        moduleDir: File,
        packagePrefix: String,
        architecture: AppArchitecture,
        module: Module
    ) {
        val presentationDir = File(moduleDir, "presentation").apply { mkdirs() }
        generateMVIPresentationLayer(presentationDir, packagePrefix, module)
    }

    private fun generateMVPI(
        moduleDir: File,
        packagePrefix: String,
        architecture: AppArchitecture,
        module: Module
    ) {
        val presentationDir = File(moduleDir, "presentation").apply { mkdirs() }

        module.screens.forEach { screen ->
            File(presentationDir, "screens").mkdirs()

            when (screen.uiFramework) {
                UIFramework.COMPOSE -> {
                    val screenCode = Templates.composeScreen(packagePrefix, screen, module.useHilt)
                    File(File(presentationDir, "screens"), "${screen.name}Screen.kt").writeText(screenCode)
                }
                UIFramework.VIEW -> {
                    val fragmentCode = Templates.fragment(packagePrefix, screen, module.useHilt)
                    File(File(presentationDir, "screens"), "${screen.name}Fragment.kt").writeText(fragmentCode)
                }
                UIFramework.BOTH -> {
                    val screenCode = Templates.composeScreen(packagePrefix, screen, module.useHilt)
                    File(File(presentationDir, "screens"), "${screen.name}Screen.kt").writeText(screenCode)
                    val fragmentCode = Templates.fragment(packagePrefix, screen, module.useHilt)
                    File(File(presentationDir, "screens"), "${screen.name}Fragment.kt").writeText(fragmentCode)
                }
            }

            File(presentationDir, "presenters").mkdirs()
            val presenterCode = Templates.presenter(packagePrefix, screen, module.useHilt)
            File(File(presentationDir, "presenters"), "${screen.name}Presenter.kt").writeText(presenterCode)

            File(presentationDir, "contracts").mkdirs()
            val contractCode = Templates.contract(packagePrefix, screen)
            File(File(presentationDir, "contracts"), "${screen.name}Contract.kt").writeText(contractCode)
        }
    }

    private fun generatePresentationLayer(
        presentationDir: File,
        packagePrefix: String,
        architecture: AppArchitecture,
        module: Module
    ) {
        module.screens.forEach { screen ->
            File(presentationDir, "screens").mkdirs()

            // UI component
            when (screen.uiFramework) {
                UIFramework.COMPOSE -> {
                    val screenCode = Templates.composeScreen(packagePrefix, screen, module.useHilt)
                    File(File(presentationDir, "screens"), "${screen.name}Screen.kt").writeText(screenCode)
                }
                UIFramework.VIEW -> {
                    val fragmentCode = Templates.fragment(packagePrefix, screen, module.useHilt)
                    File(File(presentationDir, "screens"), "${screen.name}Fragment.kt").writeText(fragmentCode)
                }
                UIFramework.BOTH -> {
                    val screenCode = Templates.composeScreen(packagePrefix, screen, module.useHilt)
                    File(File(presentationDir, "screens"), "${screen.name}Screen.kt").writeText(screenCode)
                    val fragmentCode = Templates.fragment(packagePrefix, screen, module.useHilt)
                    File(File(presentationDir, "screens"), "${screen.name}Fragment.kt").writeText(fragmentCode)
                }
            }

            if (screen.hasViewModel) {
                File(presentationDir, "viewmodels").mkdirs()
                val viewModelCode = Templates.viewModel(packagePrefix, screen, module.useHilt)
                File(File(presentationDir, "viewmodels"), "${screen.name}ViewModel.kt").writeText(viewModelCode)
            }
        }
    }

    private fun generateMVIPresentationLayer(
        presentationDir: File,
        packagePrefix: String,
        module: Module
    ) {
        module.screens.forEach { screen ->
            // UI component
            File(presentationDir, "screens").mkdirs()
            when (screen.uiFramework) {
                UIFramework.COMPOSE -> {
                    val screenCode = Templates.composeScreen(packagePrefix, screen, module.useHilt)
                    File(File(presentationDir, "screens"), "${screen.name}Screen.kt").writeText(screenCode)
                }
                UIFramework.VIEW -> {
                    val fragmentCode = Templates.fragment(packagePrefix, screen, module.useHilt)
                    File(File(presentationDir, "screens"), "${screen.name}Fragment.kt").writeText(fragmentCode)
                }
                UIFramework.BOTH -> {
                    val screenCode = Templates.composeScreen(packagePrefix, screen, module.useHilt)
                    File(File(presentationDir, "screens"), "${screen.name}Screen.kt").writeText(screenCode)
                    val fragmentCode = Templates.fragment(packagePrefix, screen, module.useHilt)
                    File(File(presentationDir, "screens"), "${screen.name}Fragment.kt").writeText(fragmentCode)
                }
            }

            if (screen.hasViewModel) {
                File(presentationDir, "viewmodels").mkdirs()
                val viewModelCode = Templates.mviViewModel(packagePrefix, screen, module.useHilt)
                File(File(presentationDir, "viewmodels"), "${screen.name}ViewModel.kt").writeText(viewModelCode)
            }
        }
    }

    private fun generateCoreModules(
        baseDir: File,
        architecture: AppArchitecture,
        coreModules: List<Module>,
        allModels: List<DataModel>
    ) {
        coreModules.forEach { module ->
            val moduleDir = File(baseDir, "core/${module.name}")
            val packagePrefix = "${architecture.packageName}.core.${module.name}"

            when (module.name) {
                "database" -> {
                    // Generate entities
                    allModels.forEach { model ->
                        File(moduleDir, "entities").mkdirs()
                        val entityCode = Templates.roomEntity(packagePrefix, model)
                        File(File(moduleDir, "entities"), "${model.name}Entity.kt").writeText(entityCode)

                        File(moduleDir, "dao").mkdirs()
                        val daoCode = Templates.roomDAO(packagePrefix, model)
                        File(File(moduleDir, "dao"), "${model.name}Dao.kt").writeText(daoCode)
                    }

                    File(moduleDir, "di").mkdirs()
                    val moduleCode = Templates.databaseModule(packagePrefix)
                    File(File(moduleDir, "di"), "DatabaseModule.kt").writeText(moduleCode)
                }
                "network" -> {
                    // Generate DTOs
                    allModels.forEach { model ->
                        File(moduleDir, "model").mkdirs()
                        val dtoCode = Templates.dto(packagePrefix, model)
                        File(File(moduleDir, "model"), "${model.name}Dto.kt").writeText(dtoCode)
                    }

                    File(moduleDir, "di").mkdirs()
                    val moduleCode = Templates.networkModule(packagePrefix)
                    File(File(moduleDir, "di"), "NetworkModule.kt").writeText(moduleCode)
                }
                else -> {
                    File(moduleDir, "common").mkdirs()
                }
            }
        }
    }

    private fun generateDesignSystemModule(
        baseDir: File,
        architecture: AppArchitecture,
        module: Module
    ) {
        val moduleDir = File(baseDir, "design-system")

        File(moduleDir, "theme").mkdirs()
        val themeCode = Templates.theme()
        File(File(moduleDir, "theme"), "Theme.kt").writeText(themeCode)
    }

    private fun generateDI(
        moduleDir: File,
        packagePrefix: String,
        module: Module
    ) {
        File(moduleDir, "di").mkdirs()
        val moduleCode = Templates.hiltModule(packagePrefix, module)
        File(File(moduleDir, "di"), "${module.name.replaceFirstChar { it.uppercase() }}Module.kt").writeText(moduleCode)
    }
}