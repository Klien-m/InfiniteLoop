package com.infinite.narrative.engine

import com.infinite.narrative.ai.AIClient
import com.infinite.narrative.ai.ContentFilter
import com.infinite.narrative.ai.model.*
import com.infinite.narrative.data.model.*
import kotlinx.coroutines.flow.Flow
import com.infinite.narrative.ai.model.NarrativeContext


/**
 * 动态叙事引擎 - 核心的AI生成系统
 */
class NarrativeEngine(
    private val aiClient: AIClient,
    private val contentFilter: ContentFilter,
    private val worldStateManager: WorldStateManager,
    private val contextManager: NarrativeContextManager
) {

    /**
     * 生成新的故事片段
     * @param playerId 玩家ID
     * @param worldId 世界ID
     * @param playerChoice 玩家选择的选项ID（可选）
     * @return 生成的故事响应
     */
    suspend fun generateStory(
        playerId: String,
        worldId: String,
        playerChoice: String? = null
    ): StoryGenerationResult {

        // 1. 构建叙事上下文
        val context = contextManager.buildNarrativeContext(
            playerId = playerId,
            worldId = worldId,
            playerChoice = playerChoice
        )

        // 2. 生成AI响应
        val rawResponse = aiClient.generateNarrative(context)

        // 3. 内容安全过滤
        val filteredResponse = filterResponse(rawResponse)

        // 4. 更新世界状态
        val updatedState = worldStateManager.updateWorldState(
            playerId = playerId,
            worldId = worldId,
            response = filteredResponse,
            context = context
        )

        // 5. 保存上下文快照
        contextManager.saveContextSnapshot(
            playerId = playerId,
            worldId = worldId,
            context = context,
            response = filteredResponse
        )

        return StoryGenerationResult(
            storySegment = filteredResponse.storySegment,
            options = filteredResponse.generatedOptions,
            worldState = updatedState,
            confidenceScore = filteredResponse.confidenceScore
        )
    }

    /**
     * 构建世界选择上下文
     * @param playerId 玩家ID
     * @return 世界选择建议
     */
    suspend fun generateWorldSelection(playerId: String): WorldSelectionResult {
        val playerProgress = worldStateManager.getPlayerProgress(playerId)
        val availableWorlds = worldStateManager.getAvailableWorlds(playerId)

        // 构建世界选择上下文
        val context = NarrativeContext(
            systemInstruction = "你是一位世界引导者，需要根据玩家的成长和经历，推荐最适合的下一个故事世界。每个世界都有独特的风格和挑战。",
            worldState = WorldState(
                currentWorld = "nexus", // 叙事行者的大本营
                keyCharacters = emptyMap(),
                unlockedLocations = listOf("万象之根", "叙事长廊"),
                corePuzzles = emptyMap(),
                storyThreads = emptyList()
            ),
            recentStory = buildPlayerHistorySummary(playerProgress),
            playerAttributes = playerProgress.playerAttributes,
            availableWorlds = availableWorlds,
            activeQuests = playerProgress.activeQuests,
            inventoryItems = playerProgress.inventoryItems,
            factionRelations = playerProgress.factionRelations
        )

        val response = aiClient.generateNarrative(context)
        val filteredResponse = filterResponse(response)

        return WorldSelectionResult(
            recommendation = filteredResponse.storySegment,
            worldOptions = extractWorldOptions(filteredResponse.generatedOptions),
            reasoning = extractWorldReasoning(filteredResponse.storySegment)
        )
    }

    /**
     * 过滤AI生成的响应
     */
    private fun filterResponse(response: NarrativeResponse): NarrativeResponse {
        val filteredStory = contentFilter.filterContent(response.storySegment)

        val filteredOptions = response.generatedOptions.map { option ->
            val filteredText = contentFilter.filterContent(option.text)
            val filteredDescription = contentFilter.filterContent(option.description)

            option.copy(
                text = filteredText.filtered,
                description = filteredDescription.filtered
            )
        }

        return response.copy(
            storySegment = filteredStory.filtered,
            generatedOptions = filteredOptions
        )
    }

    /**
     * 构建玩家历史摘要
     */
    private fun buildPlayerHistorySummary(progress: PlayerProgress): String {
        val sb = StringBuilder()
        sb.append("玩家叙事行者的历史摘要：\n")

        if (progress.completedWorlds.isNotEmpty()) {
            sb.append("已完成的世界：${progress.completedWorlds.joinToString(", ")}\n")
        }

        if (progress.acquiredItems.isNotEmpty()) {
            sb.append("获得的独特物品：${progress.acquiredItems.take(5).joinToString(", ")}\n")
        }

        if (progress.keyDecisions.isNotEmpty()) {
            sb.append("关键抉择：${progress.keyDecisions.take(3).joinToString("； ")}\n")
        }

        sb.append("当前属性：洞察力${progress.playerAttributes.insight}，说服力${progress.playerAttributes.persuasion}，魄力${progress.playerAttributes.魄力}")

        return sb.toString()
    }

    /**
     * 从选项中提取世界选择
     */
    private fun extractWorldOptions(options: List<StoryOption>): List<WorldOption> {
        return options.map { option ->
            WorldOption(
                worldId = option.optionId,
                name = option.text,
                description = option.description,
                requiredAttributes = option.requiredAttributes ?: emptyMap()
            )
        }
    }

    /**
     * 从故事中提取世界推荐理由
     */
    private fun extractWorldReasoning(story: String): String {
        // 简单实现：提取包含"因为"、"所以"、"适合"等关键词的句子
        return story.lines()
            .filter { line ->
                line.contains("因为") || line.contains("所以") ||
                        line.contains("适合") || line.contains("推荐")
            }
            .joinToString(" ")
            .ifEmpty { "基于你的经历和能力，这个选择最为合适。" }
    }

    /**
     * 生成结果数据类
     */
    data class StoryGenerationResult(
        val storySegment: String,
        val options: List<StoryOption>,
        val worldState: WorldState,
        val confidenceScore: Float
    )

    /**
     * 世界选择结果数据类
     */
    data class WorldSelectionResult(
        val recommendation: String,
        val worldOptions: List<WorldOption>,
        val reasoning: String
    )

    /**
     * 世界选项数据类
     */
    data class WorldOption(
        val worldId: String,
        val name: String,
        val description: String,
        val requiredAttributes: Map<String, Int>
    )
}

/**
 * 世界状态管理器接口
 */
interface WorldStateManager {

    /**
     * 获取玩家进度信息
     */
    suspend fun getPlayerProgress(playerId: String): PlayerProgress

    /**
     * 获取可用的世界列表
     */
    suspend fun getAvailableWorlds(playerId: String): List<String>

    /**
     * 更新世界状态
     */
    suspend fun updateWorldState(
        playerId: String,
        worldId: String,
        response: NarrativeResponse,
        context: NarrativeContext
    ): WorldState

    /**
     * 解锁新世界
     */
    suspend fun unlockWorld(playerId: String, worldId: String)

    /**
     * 记录关键事件
     */
    suspend fun recordKeyEvent(playerId: String, eventId: String, eventData: Map<String, Any>)
}

/**
 * 叙事上下文管理器接口
 */
interface NarrativeContextManager {

    /**
     * 构建叙事上下文
     */
    suspend fun buildNarrativeContext(
        playerId: String,
        worldId: String,
        playerChoice: String? = null
    ): NarrativeContext

    /**
     * 保存上下文快照
     */
    suspend fun saveContextSnapshot(
        playerId: String,
        worldId: String,
        context: NarrativeContext,
        response: NarrativeResponse
    )

    /**
     * 获取相关上下文
     */
    fun getRelevantContexts(
        playerId: String,
        worldId: String,
        contextType: String
    ): Flow<List<NarrativeContext>>

    /**
     * 清理过期上下文
     */
    suspend fun cleanupExpiredContexts(playerId: String)
}

/**
 * 玩家进度数据类
 */
data class PlayerProgress(
    val playerId: String,
    val playerAttributes: PlayerAttributes,
    val completedWorlds: List<String>,
    val activeQuests: List<String>,
    val inventoryItems: List<String>,
    val factionRelations: Map<String, String>,
    val acquiredItems: List<String>,
    val keyDecisions: List<String>,
    val currentWorld: String? = null
)
