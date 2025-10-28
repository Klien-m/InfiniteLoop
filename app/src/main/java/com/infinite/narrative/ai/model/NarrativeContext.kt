package com.infinite.narrative.ai.model

import com.google.gson.annotations.SerializedName
import com.infinite.narrative.data.model.PlayerAttributes

/**
 * 叙事上下文数据类 - 对应deepseek_markdown.md中的结构化提示
 */
data class NarrativeContext(
    @SerializedName("system_instruction")
    val systemInstruction: String,

    @SerializedName("world_state")
    val worldState: WorldState,

    @SerializedName("recent_story")
    val recentStory: String,

    @SerializedName("player_attributes")
    val playerAttributes: PlayerAttributes,

    @SerializedName("available_worlds")
    val availableWorlds: List<String>,

    @SerializedName("active_quests")
    val activeQuests: List<String>,

    @SerializedName("inventory_items")
    val inventoryItems: List<String>,

    @SerializedName("faction_relations")
    val factionRelations: Map<String, String>,

    @SerializedName("narrative_anchor")
    val narrativeAnchor: String? = null
)

/**
 * 世界状态数据类
 */
data class WorldState(
    @SerializedName("current_world")
    val currentWorld: String,

    @SerializedName("key_characters")
    val keyCharacters: Map<String, String>,

    @SerializedName("unlocked_locations")
    val unlockedLocations: List<String>,

    @SerializedName("core_puzzles")
    val corePuzzles: Map<String, String>,

    @SerializedName("story_threads")
    val storyThreads: List<StoryThread>,

    @SerializedName("time_progression")
    val timeProgression: String? = null
)

/**
 * 故事线数据类
 */
data class StoryThread(
    @SerializedName("thread_id")
    val threadId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("status")
    val status: String, // "active", "dormant", "completed"

    @SerializedName("priority")
    val priority: Int, // 1-10

    @SerializedName("last_update")
    val lastUpdate: String
)

/**
 * AI生成响应数据类
 */
data class NarrativeResponse(
    @SerializedName("story_segment")
    val storySegment: String,

    @SerializedName("generated_options")
    val generatedOptions: List<StoryOption>,

    @SerializedName("new_world_state")
    val newWorldState: WorldState? = null,

    @SerializedName("context_updates")
    val contextUpdates: List<ContextUpdate>? = null,

    @SerializedName("confidence_score")
    val confidenceScore: Float
)

/**
 * 故事选项数据类
 */
data class StoryOption(
    @SerializedName("option_id")
    val optionId: String,

    @SerializedName("text")
    val text: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("required_attributes")
    val requiredAttributes: Map<String, Int>? = null,

    @SerializedName("potential_consequences")
    val potentialConsequences: List<String>? = null,

    @SerializedName("estimated_time_cost")
    val estimatedTimeCost: Int? = null // 分钟
)

/**
 * 上下文更新数据类
 */
data class ContextUpdate(
    @SerializedName("update_type")
    val updateType: String, // "character_relation", "item_acquired", "quest_progress", etc.

    @SerializedName("target_id")
    val targetId: String,

    @SerializedName("change_data")
    val changeData: Map<String, Any>,

    @SerializedName("timestamp")
    val timestamp: Long
)

/**
 * AI客户端配置
 */
data class AIClientConfig(
    val apiKey: String,
    val baseUrl: String = "https://api.katcoder.pro/v1",
    val timeoutSeconds: Long = 30,
    val maxRetries: Int = 3,
    val offlineModelPath: String? = null,
    val modelQuantization: String = "q4_0" // GGUF量化等级
)

/**
 * 世界配置数据类
 */
data class WorldConfig(
    @SerializedName("world_id")
    val worldId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("style_prompt")
    val stylePrompt: String,

    @SerializedName("starting_context")
    val startingContext: NarrativeContext,

    @SerializedName("available_characters")
    val availableCharacters: List<String>,

    @SerializedName("initial_items")
    val initialItems: List<String>,

    @SerializedName("story_templates")
    val storyTemplates: List<String>
)
