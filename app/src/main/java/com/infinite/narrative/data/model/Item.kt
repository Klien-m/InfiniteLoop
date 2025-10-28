package com.infinite.narrative.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * 物品实体 - 支持概念性物品的抽象数据结构
 */
@Entity(tableName = "items")
@TypeConverters(ItemTypeConverters::class)
data class Item(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val itemType: ItemType,
    val rarity: Rarity,
    val value: Int,
    val isUnique: Boolean = false,
    val metadata: ItemMetadata? = null,
    val acquiredAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 物品类型枚举
 */
enum class ItemType {
    @SerializedName("physical")
    PHYSICAL, // 实体物品：武器、装备、道具

    @SerializedName("conceptual")
    CONCEPTUAL, // 概念性物品：把柄、契约、记忆

    @SerializedName("knowledge")
    KNOWLEDGE, // 知识类物品：技能、秘籍、情报

    @SerializedName("spiritual")
    SPIRITUAL // 精神类物品：信仰、祝福、诅咒
}

/**
 * 稀有度枚举
 */
enum class Rarity {
    COMMON,     // 普通
    UNCOMMON,   // 不常见
    RARE,       // 稀有
    EPIC,       // 史诗
    LEGENDARY   // 传说
}

/**
 * 物品元数据 - 用于存储特定类型物品的额外信息
 */
sealed class ItemMetadata {

    // 实体物品元数据
    data class PhysicalMetadata(
        val durability: Int = 100,
        val weight: Float = 1f,
        val effects: List<String> = emptyList()
    ) : ItemMetadata()

    // 概念性物品元数据
    data class ConceptualMetadata(
        val target: String? = null, // 目标人物/组织
        val expiration: Long? = null, // 过期时间
        val conditions: List<String> = emptyList() // 使用条件
    ) : ItemMetadata()

    // 知识类物品元数据
    data class KnowledgeMetadata(
        val skillLevel: Int = 1,
        val learningTime: Int = 0, // 学习所需时间（分钟）
        val prerequisites: List<String> = emptyList() // 前置条件
    ) : ItemMetadata()

    // 精神类物品元数据
    data class SpiritualMetadata(
        val power: Int = 1,
        val source: String = "", // 力量来源
        val sideEffects: List<String> = emptyList() // 副作用
    ) : ItemMetadata()
}

/**
 * 物品持有者关系
 */
@Entity(
    tableName = "item_ownerships",
    primaryKeys = ["itemId", "ownerId"]
)
data class ItemOwnership(
    val itemId: String,
    val ownerId: String, // 可以是角色ID或玩家ID
    val quantity: Int = 1,
    val acquiredFrom: String? = null, // 从谁那里获得
    val acquiredAt: Long = System.currentTimeMillis()
)

/**
 * 物品类型转换器
 */
class ItemTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromItemType(itemType: ItemType): String = itemType.name

    @TypeConverter
    fun toItemType(itemType: String): ItemType = ItemType.valueOf(itemType)

    @TypeConverter
    fun fromItemMetadata(metadata: ItemMetadata?): String? {
        return metadata?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toItemMetadata(metadata: String?): ItemMetadata? {
        return metadata?.let { gson.fromJson(it, ItemMetadata::class.java) }
    }
}
