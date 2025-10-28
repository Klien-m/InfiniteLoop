package com.infinite.narrative.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * 任务实体 - 支持多线程剧情的DAG结构
 */
@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val questType: QuestType,
    val difficulty: Int,
    val rewardExp: Int,
    val rewardItems: List<String> = emptyList(),
    val prerequisites: List<String> = emptyList(), // 前置任务ID列表
    val status: QuestStatus = QuestStatus.AVAILABLE,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val failedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 任务类型枚举
 */
enum class QuestType {
    @SerializedName("main")
    MAIN,       // 主线任务

    @SerializedName("side")
    SIDE,       // 支线任务

    @SerializedName("personal")
    PERSONAL,   // 角色个人任务

    @SerializedName("event")
    EVENT       // 世界事件任务
}

/**
 * 任务状态枚举
 */
enum class QuestStatus {
    AVAILABLE,      // 可接取
    IN_PROGRESS,    // 进行中
    COMPLETED,      // 已完成
    FAILED,         // 已失败
    LOCKED          // 已锁定（需要前置条件）
}

/**
 * 任务进度实体 - 记录任务的详细进度
 */
@Entity(
    tableName = "quest_progresses",
    primaryKeys = ["questId", "playerId"]
)
data class QuestProgress(
    val questId: String,
    val playerId: String,
    val currentStage: Int = 0,
    val totalStages: Int,
    val progressData: String? = null, // JSON格式的进度数据
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * 任务阶段实体
 */
@Entity(
    tableName = "quest_stages",
    primaryKeys = ["questId", "stageNumber"]
)
data class QuestStage(
    val questId: String,
    val stageNumber: Int,
    val title: String,
    val description: String,
    val objectives: List<String>,
    val rewards: List<String> = emptyList(),
    val unlockConditions: List<String> = emptyList(), // 解锁条件
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 世界状态快照 - 用于AI生成的关键锚点
 */
@Entity(tableName = "world_state_snapshots")
data class WorldStateSnapshot(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val playerLevel: Int,
    val playerAttributes: PlayerAttributes,
    val unlockedWorlds: List<String>,
    val activeQuests: List<String>,
    val keyItems: List<String>,
    val factionRelations: Map<String, Int>, // factionId -> 好感度
    val narrativeAnchor: String, // 关键剧情锚点描述
    val contextSummary: String, // 上下文摘要
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 玩家属性数据类
 */
data class PlayerAttributes(
    val insight: Int = 10,      // 洞察力
    val persuasion: Int = 10,   // 说服力  
    val 魄力: Int = 10,         // 魄力
    val skills: Map<String, Int> = emptyMap() // 技能名称 -> 等级
)

/**
 * 因果收藏馆实体 - 跨故事联动的核心
 */
@Entity(tableName = "causal_collection")
data class CausalCollection(
    @PrimaryKey val id: String,
    val playerId: String,
    val collectionType: CollectionType,
    val contentId: String, // 关联的任务ID、物品ID或事件ID
    val title: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val unlockConditions: List<String> = emptyList(), // 在其他世界中解锁的条件
    val acquiredAt: Long = System.currentTimeMillis()
)

/**
 * 收藏品类型枚举
 */
enum class CollectionType {
    @SerializedName("quest_end")
    QUEST_END,        // 任务结局

    @SerializedName("unique_item")
    UNIQUE_ITEM,      // 独特物品

    @SerializedName("character_memory")
    CHARACTER_MEMORY, // 角色记忆

    @SerializedName("world_event")
    WORLD_EVENT       // 世界事件
}

/**
 * 叙事上下文实体 - 存储AI生成的上下文信息
 */
@Entity(tableName = "narrative_contexts")
data class NarrativeContext(
    @PrimaryKey val id: String,
    val playerId: String,
    val worldId: String,
    val contextType: ContextType,
    val content: String, // JSON格式的上下文数据
    val relevanceScore: Float, // 相关性评分
    val expiresAt: Long? = null, // 过期时间
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 上下文类型枚举
 */
enum class ContextType {
    @SerializedName("story_history")
    STORY_HISTORY,    // 故事历史

    @SerializedName("character_dialogue")
    CHARACTER_DIALOGUE, // 角色对话

    @SerializedName("world_description")
    WORLD_DESCRIPTION, // 世界描述

    @SerializedName("quest_progress")
    QUEST_PROGRESS    // 任务进度
}
