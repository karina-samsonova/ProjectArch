package com.samsonova.projectarch.generator

import com.samsonova.projectarch.models.AppArchitecture
import com.samsonova.projectarch.models.ArchitecturePattern
import java.io.File

class CodeGenerator(private val project: Any? = null) {
    
    fun generate(architecture: AppArchitecture, baseDir: File): Result<Unit> = try {
        val srcDir = createSourceDirectory(baseDir, architecture.packageName)

        when (architecture.pattern) {
            ArchitecturePattern.CLEAN_ARCHITECTURE -> {
                architecture.features.forEach { feature ->
                    FeatureGenerator.generateCleanArchitecture(srcDir, architecture, feature)
                }
            }
            ArchitecturePattern.MVVM -> {
                architecture.features.forEach { feature ->
                    FeatureGenerator.generateMVVM(srcDir, architecture, feature)
                }
            }
            ArchitecturePattern.MVI -> {
                architecture.features.forEach { feature ->
                    FeatureGenerator.generateMVI(srcDir, architecture, feature)
                }
            }
            ArchitecturePattern.MVPI -> {
                architecture.features.forEach { feature ->
                    FeatureGenerator.generateMVPI(srcDir, architecture, feature)
                }
            }
        }
        
        if (architecture.useHilt) {
            FeatureGenerator.generateHiltModules(srcDir, architecture)
        }
        
        FeatureGenerator.generateGradleFile(baseDir, architecture)
        //FeatureGenerator.generateManifestFile(baseDir, architecture)
        
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(Exception("Code generation failed: ${e.message}", e))
    }
    
    private fun createSourceDirectory(baseDir: File, packageName: String): File {
        val srcDir = File(baseDir, "app/src/main/kotlin/${packageName.replace(".", "/")}")
        srcDir.mkdirs()
        return srcDir
    }
}

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}