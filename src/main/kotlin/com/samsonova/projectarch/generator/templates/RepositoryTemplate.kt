package com.samsonova.projectarch.generator.templates

import com.samsonova.projectarch.generator.templates.ui.decapitalize
import com.samsonova.projectarch.models.AppArchitecture
import com.samsonova.projectarch.models.DataModel
import com.samsonova.projectarch.models.Feature

object RepositoryTemplate {

    fun generateInterface(architecture: AppArchitecture, feature: Feature, model: DataModel): String {
        return """
            interface ${model.name}Repository {
                suspend fun get${model.name}(): Result<${model.name}>
                suspend fun get${model.name}ById(id: String): Result<${model.name}>
                suspend fun save${model.name}(${model.name.decapitalize()}: ${model.name}): Result<Unit>
                suspend fun delete${model.name}(id: String): Result<Unit>
                fun observe${model.name}(): Flow<${model.name}>
            }
        """.trimIndent()
    }

    fun generateDomainModel(feature: Feature, model: DataModel): String {
        val properties = model.properties
            .map { (name, type) -> "    val $name: ${type.type}${if (type.isNullable) "?" else ""}" }
            .joinToString(",\n")

        return """
            data class ${model.name}(
            $properties
            )
        """.trimIndent()
    }

    fun generateRoomEntity(architecture: AppArchitecture, feature: Feature, model: DataModel): String {
        val properties = model.properties
            .filter { it.key != "id" }
            .map { (name, type) -> "    val $name: ${type.type}${if (type.isNullable) "?" else ""}" }
            .joinToString(",\n")

        return """
            import androidx.room.Entity
            import androidx.room.PrimaryKey
            import kotlinx.serialization.Serializable
            
            @Serializable
            @Entity(tableName = "${model.tableName}")
            data class ${model.name}Entity(
                @PrimaryKey
                val id: String,
            $properties
            )
        """.trimIndent()
    }

    fun generateDAO(architecture: AppArchitecture, feature: Feature, model: DataModel): String {
        return """
            import androidx.room.*
            import kotlinx.coroutines.flow.Flow
            
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

    fun generateDTO(architecture: AppArchitecture, feature: Feature, model: DataModel): String {
        val properties = model.properties
            .map { (name, type) -> "    val $name: ${type.type}${if (type.isNullable) "?" else ""}" }
            .joinToString(",\n")

        return """
            import kotlinx.serialization.Serializable
            
            @Serializable
            data class ${model.name}Dto(
            $properties
            )
        """.trimIndent()
    }

    fun generateImplementation(architecture: AppArchitecture, feature: Feature, model: DataModel): String {
        return """
            import javax.inject.Inject
            import javax.inject.Singleton
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.map
            
            @Singleton
            class ${model.name}RepositoryImpl @Inject constructor(
                private val localDataSource: ${model.name}LocalDataSource,
                private val remoteDataSource: ${model.name}RemoteDataSource
            ) : ${model.name}Repository {
            
                override suspend fun get${model.name}(): Result<${model.name}> = try {
                    val data = localDataSource.get${model.name}()
                    Result.Success(data)
                } catch (e: Exception) {
                    Result.Error(e)
                }
            
                override suspend fun get${model.name}ById(id: String): Result<${model.name}> = try {
                    val data = localDataSource.get${model.name}ById(id)
                    Result.Success(data)
                } catch (e: Exception) {
                    Result.Error(e)
                }
            
                override suspend fun save${model.name}(${model.name.decapitalize()}: ${model.name}): Result<Unit> = try {
                    localDataSource.save${model.name}(${model.name.decapitalize()}.toEntity())
                    Result.Success(Unit)
                } catch (e: Exception) {
                    Result.Error(e)
                }
            
                override suspend fun delete${model.name}(id: String): Result<Unit> = try {
                    localDataSource.delete${model.name}(id)
                    Result.Success(Unit)
                } catch (e: Exception) {
                    Result.Error(e)
                }
            
                override fun observe${model.name}(): Flow<${model.name}> =
                    localDataSource.observe${model.name}().map { it.toDomain() }
            }
        """.trimIndent()
    }

    fun generateLocalDataSource(architecture: AppArchitecture, feature: Feature, model: DataModel): String {
        return """
            import javax.inject.Inject
            import kotlinx.coroutines.flow.Flow
            
            class ${model.name}LocalDataSource @Inject constructor(
                private val dao: ${model.name}Dao
            ) {
            
                suspend fun get${model.name}(): ${model.name} {
                    return ${model.name}()
                }
            
                suspend fun get${model.name}ById(id: String): ${model.name} {
                    return ${model.name}()
                }
            
                suspend fun save${model.name}(entity: ${model.name}Entity) {
                    dao.insert(entity)
                }
            
                suspend fun delete${model.name}(id: String) {
                    dao.deleteById(id)
                }
            
                fun observe${model.name}(): Flow<${model.name}Entity> {
                    return dao.getAll().map { it.firstOrNull() ?: ${model.name}Entity("") }
                }
            }
        """.trimIndent()
    }

    fun generateRemoteDataSource(architecture: AppArchitecture, feature: Feature, model: DataModel): String {
        return """
            import javax.inject.Inject
            
            class ${model.name}RemoteDataSource @Inject constructor(
                // Inject Retrofit API interface
            ) {
            
                suspend fun fetch${model.name}(): ${model.name}Dto {
                    return ${model.name}Dto()
                }
            
                suspend fun sync${model.name}(${model.name.decapitalize()}: ${model.name}) {
                    // Sync with remote server
                }
            }
        """.trimIndent()
    }
}