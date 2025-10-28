package com.infinite.narrative.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.infinite.narrative.data.model.Character
import com.infinite.narrative.data.model.CharacterFaction
import com.infinite.narrative.data.model.CharacterRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    // Character CRUD操作
    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: String): Character?

    @Query("SELECT * FROM characters WHERE name = :name")
    suspend fun getCharacterByName(name: String): Character?

    @Query("SELECT * FROM characters WHERE isPlayer = :isPlayer")
    fun getCharactersByType(isPlayer: Boolean): Flow<List<Character>>

    @Query("SELECT * FROM characters WHERE id IN (:ids)")
    suspend fun getCharactersByIds(ids: List<String>): List<Character>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: Character)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<Character>)

    @Update
    suspend fun updateCharacter(character: Character)

    @Delete
    suspend fun deleteCharacter(character: Character)

    // CharacterRelation操作
    @Query("SELECT * FROM character_relations WHERE characterId = :characterId")
    suspend fun getRelationsByCharacter(characterId: String): List<CharacterRelation>

    @Query("SELECT * FROM character_relations WHERE relatedCharacterId = :characterId")
    suspend fun getRelationsToCharacter(characterId: String): List<CharacterRelation>

    @Query("SELECT * FROM character_relations WHERE characterId = :fromId AND relatedCharacterId = :toId")
    suspend fun getRelation(fromId: String, toId: String): CharacterRelation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelation(relation: CharacterRelation)

    @Update
    suspend fun updateRelation(relation: CharacterRelation)

    @Delete
    suspend fun deleteRelation(relation: CharacterRelation)

    // CharacterFaction操作
    @Query("SELECT * FROM character_factions WHERE characterId = :characterId")
    suspend fun getFactionsByCharacter(characterId: String): List<CharacterFaction>

    @Query("SELECT * FROM character_factions WHERE factionId = :factionId")
    suspend fun getCharactersByFaction(factionId: String): List<CharacterFaction>

    @Query("SELECT * FROM character_factions WHERE characterId = :characterId AND factionId = :factionId")
    suspend fun getFactionRelation(characterId: String, factionId: String): CharacterFaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactionRelation(faction: CharacterFaction)

    @Update
    suspend fun updateFactionRelation(faction: CharacterFaction)

    @Delete
    suspend fun deleteFactionRelation(faction: CharacterFaction)

    // 复杂查询
    @Transaction
    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterWithRelations(id: String): CharacterWithRelations?

    @Transaction
    @Query("SELECT * FROM characters WHERE faction = :faction")
    fun getFactionMembers(faction: String): Flow<List<CharacterWithRelations>>

    // 批量操作
    @Transaction
    suspend fun insertCharacterWithRelations(
        character: Character,
        relations: List<CharacterRelation> = emptyList(),
        factions: List<CharacterFaction> = emptyList()
    ) {
        insertCharacter(character)

        if (relations.isNotEmpty()) {
            insertRelations(relations)
        }

        if (factions.isNotEmpty()) {
            insertFactionRelations(factions)
        }
    }

    @Transaction
    suspend fun updateCharacterRelationships(
        characterId: String,
        newRelations: List<CharacterRelation>,
        newFactions: List<CharacterFaction>
    ) {
        // 删除旧的关系
        deleteAllRelations(characterId)
        deleteAllFactions(characterId)

        // 插入新的关系
        if (newRelations.isNotEmpty()) {
            insertRelations(newRelations)
        }

        if (newFactions.isNotEmpty()) {
            insertFactionRelations(newFactions)
        }
    }

    // 批量插入关系
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelations(relations: List<CharacterRelation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactionRelations(factions: List<CharacterFaction>)

    // 删除操作
    @Query("DELETE FROM character_relations WHERE characterId = :characterId OR relatedCharacterId = :characterId")
    suspend fun deleteAllRelations(characterId: String)

    @Query("DELETE FROM character_factions WHERE characterId = :characterId")
    suspend fun deleteAllFactions(characterId: String)

    // 获取角色好感度
    @Query("SELECT AVG(好感度) FROM character_factions WHERE factionId IN (SELECT faction FROM characters WHERE id = :characterId)")
    suspend fun getCharacterAverageDisposition(characterId: String): Double

    // 检查角色是否存在
    @Query("SELECT EXISTS(SELECT 1 FROM characters WHERE id = :id)")
    suspend fun characterExists(id: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM characters WHERE name = :name)")
    suspend fun characterNameExists(name: String): Boolean
}

/**
 * 带关系的角色数据类
 */
data class CharacterWithRelations(
    @Embedded
    val character: Character,

    @Relation(
        parentColumn = "id",
        entityColumn = "characterId"
    )
    val relations: List<CharacterRelation> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "characterId"
    )
    val factions: List<CharacterFaction> = emptyList()
)
