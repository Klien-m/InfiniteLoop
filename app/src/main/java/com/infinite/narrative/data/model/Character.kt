package com.infinite.narrative.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 角色实体 - 支持人物关系图的核心数据结构
 */
@Entity(tableName = "characters")
data class Character(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val faction: String,
    val imageUrl: String? = null,
    val isPlayer: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 角色关系实体 - 用于构建人物关系图
 */
@Entity(
    tableName = "character_relations",
    primaryKeys = ["characterId", "relatedCharacterId"]
)
data class CharacterRelation(
    val characterId: String,
    val relatedCharacterId: String,
    val relationType: String, // "friend", "enemy", "ally", "lover", "rival"等
    val strength: Int, // 关系强度 1-10
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 角色好感度实体
 */
@Entity(
    tableName = "character_factions",
    primaryKeys = ["characterId", "factionId"]
)
data class CharacterFaction(
    val characterId: String,
    val factionId: String,
    val 好感度: Int, // 好感度 -10到10
    val lastInteraction: Long = System.currentTimeMillis()
)
