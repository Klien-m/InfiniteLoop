package com.infinite.narrative

import android.app.Application
import androidx.room.Room
import com.infinite.narrative.ai.AIClient
import com.infinite.narrative.ai.ContentFilter
import com.infinite.narrative.ai.ModelInfo
import com.infinite.narrative.ai.filter.ContentFilterImpl
import com.infinite.narrative.ai.model.NarrativeContext
import com.infinite.narrative.ai.model.NarrativeResponse
import com.infinite.narrative.ai.model.StoryOption
import com.infinite.narrative.ai.model.WorldState
import com.infinite.narrative.data.InfiniteNarrativeDatabase
import com.infinite.narrative.data.model.PlayerAttributes
import com.infinite.narrative.data.seed.WorldSeedData
import com.infinite.narrative.engine.NarrativeEngine
import com.infinite.narrative.engine.WorldStateManager
import com.infinite.narrative.engine.NarrativeContextManager
import com.infinite.narrative.engine.PlayerProgress
import kotlinx.coroutines.*

/**
 * MVP应用主类 - 集成所有核心组件
 */
class MVPApplication : Application() {
    
    // 数据库实例
    lateinit var database: InfiniteNarrativeDatabase
        private set
    
    // AI客户端（混合模式）
    lateinit var aiClient: AIClient
        private set
    
    // 内容过滤器
    lateinit var contentFilter: ContentFilter
        private set
    
    // 叙事引擎
    lateinit var narrativeEngine: NarrativeEngine
        private set
    
    // 世界状态管理器
    lateinit var worldStateManager: WorldStateManager
        private set
    
    // 叙事上下文管理器
    lateinit var contextManager: NarrativeContextManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        initializeComponents()
        setupInitialData()
    }
    
    /**
     * 初始化所有核心组件
     */
    private fun initializeComponents() {
        // 初始化数据库
        database = Room.databaseBuilder(
            applicationContext,
            InfiniteNarrativeDatabase::class.java,
            InfiniteNarrativeDatabase.DATABASE_NAME
        ).build()
        
        // 初始化内容过滤器
        contentFilter = ContentFilterImpl()
        
        // 初始化AI客户端（这里使用模拟实现，实际项目中需要集成真实API）
        aiClient = createMockAIClient()
        
        // 初始化管理器
        worldStateManager = createWorldStateManager()
        contextManager = createNarrativeContextManager()
        
        // 初始化叙事引擎
        narrativeEngine = NarrativeEngine(
            aiClient = aiClient,
            contentFilter = contentFilter,
            worldStateManager = worldStateManager,
            contextManager = contextManager
        )
    }
    
    /**
     * 设置初始数据
     */
    private fun setupInitialData() {
        // 使用协程在后台线程中设置初始世界观数据
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 插入初始角色数据
                database.characterDao().insertCharacters(
                    WorldSeedData.INITIAL_CHARACTERS
                )
                
                // 插入初始物品数据
                database.itemDao().insertItems(
                    WorldSeedData.INITIAL_ITEMS
                )
                
                // 可以在这里添加更多初始数据...
                
            } catch (e: Exception) {
                // 处理初始化错误
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 创建模拟AI客户端（用于MVP演示）
     */
    private fun createMockAIClient(): AIClient {
        return object : AIClient {
            override suspend fun generateNarrative(context: NarrativeContext): NarrativeResponse {
                // 检查是否是世界选择上下文
                if (context.worldState.currentWorld == "nexus" &&
                    context.systemInstruction.contains("世界引导者")) {
                    return generateWorldSelectionResponse(context)
                }
                
                // 根据不同的世界返回不同的模拟响应
                return when (context.worldState.currentWorld) {
                    "cyber_penglai" -> generateCyberPenglaiResponse(context)
                    "law_maze" -> generateLawMazeResponse(context)
                    "decaying_throne" -> generateDecayingThroneResponse(context)
                    else -> generateDefaultResponse(context)
                }
            }
            
            override fun isOnline(): Boolean = true
            
            override fun getModelInfo(): ModelInfo {
                return ModelInfo(
                    name = "KAT-Coder-Pro V1",
                    version = "1.0.0",
                    provider = "DeepSeek",
                    capabilities = listOf("text-generation", "storytelling", "dialogue")
                )
            }
            
            override suspend fun warmup() {}
            
            override fun cleanup() {}
            
            /**
             * 生成世界选择响应
             */
            private fun generateWorldSelectionResponse(context: NarrativeContext): NarrativeResponse {
                return NarrativeResponse(
                    storySegment = """
                        欢迎来到无限叙事世界！作为一位叙事行者，你站在万象之根的交汇处，
                        感受到无数故事世界的召唤。根据你的潜力和经历，我为你推荐以下世界：
                        
                        每个世界都有独特的风格和挑战，选择一个开始你的冒险吧！
                    """.trimIndent(),
                    generatedOptions = listOf(
                        StoryOption(
                            optionId = "cyber_penglai",
                            text = "赛博蓬莱",
                            description = "在未来都市，你的意识可以上传至仙境网络，在代码与灵气的冲突中寻求长生。",
                            requiredAttributes = mapOf("洞察力" to 10),
                            potentialConsequences = listOf("体验科技与传统的融合", "面对数据安全的挑战"),
                            estimatedTimeCost = 60
                        ),
                        StoryOption(
                            optionId = "law_maze",
                            text = "法典迷城",
                            description = "在一个法律条文能直接改写物理规则的世界，你是一名律法侠盗，利用法律的漏洞施展超能力。",
                            requiredAttributes = mapOf("说服力" to 12),
                            potentialConsequences = listOf("掌握法律的力量", "挑战司法体系"),
                            estimatedTimeCost = 45
                        ),
                        StoryOption(
                            optionId = "decaying_throne",
                            text = "衰败王座",
                            description = "你是某个被遗忘之神的最后一位祭司，在一个无神的世界里，收集信仰，重现神迹。",
                            requiredAttributes = mapOf("魄力" to 14),
                            potentialConsequences = listOf("唤醒沉睡的神力", "面对信仰的考验"),
                            estimatedTimeCost = 50
                        )
                    ),
                    confidenceScore = 0.95f
                )
            }
        }
    }
    
    /**
     * 生成赛博蓬莱的模拟响应
     */
    private fun generateCyberPenglaiResponse(context: NarrativeContext): NarrativeResponse {
        return NarrativeResponse(
            storySegment = """
                霓虹灯在雨夜中闪烁，数据流如银河般在空中流淌。你站在赛博都市的街头，
                感受到一股神秘的力量在召唤。远处的云端仙境若隐若现，仿佛在诉说着古老的秘密。
                
                破损的玉简在你手中微微发热，上面的符文开始闪烁着微弱的蓝光。这似乎是通往
                云端仙境的关键线索。
            """.trimIndent(),
            generatedOptions = listOf(
                StoryOption(
                    optionId = "explore_data_temple",
                    text = "前往数据庙宇寻找修复玉简的方法",
                    description = "数据庙宇是赛博都市中保存古老数据的地方，或许能找到修复玉简的技术。",
                    requiredAttributes = mapOf("洞察力" to 12),
                    potentialConsequences = listOf("可能遇到龙门商会的追兵", "有机会学习数据修复技术"),
                    estimatedTimeCost = 45
                ),
                StoryOption(
                    optionId = "seek_yunmeng_help",
                    text = "寻找网络游侠云梦的帮助",
                    description = "云梦精通数据流穿梭，或许能帮你解读玉简中的秘密。",
                    requiredAttributes = mapOf("说服力" to 10),
                    potentialConsequences = listOf("可能卷入云梦的个人恩怨", "获得强大的盟友"),
                    estimatedTimeCost = 30
                ),
                StoryOption(
                    optionId = "visit_old_taoist",
                    text = "拜访老道长寻求古老智慧",
                    description = "老道长作为传统修仙者，可能理解玉简中的古老符文。",
                    requiredAttributes = mapOf("魄力" to 8),
                    potentialConsequences = listOf("可能被传统修仙者排斥", "获得珍贵的修仙知识"),
                    estimatedTimeCost = 60
                )
            ),
            confidenceScore = 0.92f
        )
    }
    
    /**
     * 生成法典迷城的模拟响应
     */
    private fun generateLawMazeResponse(context: NarrativeContext): NarrativeResponse {
        return NarrativeResponse(
            storySegment = """
                法庭的钟声响起，法律条文在空中浮现，化作金色的锁链。你作为一名律法侠盗，
                感受到法律的力量在体内涌动。
                
                法官大人正用威严的目光注视着你，手中的法槌闪烁着危险的光芒。你需要运用
                法律的智慧来破解眼前的困境。
            """.trimIndent(),
            generatedOptions = listOf(
                StoryOption(
                    optionId = "use_legal_loophole",
                    text = "寻找法律漏洞进行反击",
                    description = "利用你对法律的深刻理解，找到法官无法反驳的法律依据。",
                    requiredAttributes = mapOf("洞察力" to 15, "说服力" to 12),
                    potentialConsequences = listOf("可能被指控藐视法庭", "展现卓越的法律才能"),
                    estimatedTimeCost = 20
                ),
                StoryOption(
                    optionId = "negotiate_with_judge",
                    text = "与法官进行法律谈判",
                    description = "通过合法的谈判程序，寻求和解的可能性。",
                    requiredAttributes = mapOf("说服力" to 18),
                    potentialConsequences = listOf("可能达成有利协议", "暴露更多个人信息"),
                    estimatedTimeCost = 35
                ),
                StoryOption(
                    optionId = "escape_court",
                    text = "利用法律技巧逃离法庭",
                    description = "运用程序性法律权利，合法地离开当前困境。",
                    requiredAttributes = mapOf("魄力" to 14),
                    potentialConsequences = listOf("可能被通缉", "获得行动自由"),
                    estimatedTimeCost = 15
                )
            ),
            confidenceScore = 0.89f
        )
    }
    
    /**
     * 生成衰败王座的模拟响应
     */
    private fun generateDecayingThroneResponse(context: NarrativeContext): NarrativeResponse {
        return NarrativeResponse(
            storySegment = """
                古老的神庙在风中低语，斑驳的壁画上，神明的目光似乎在追随你。你感受到
                一股微弱但熟悉的信仰之力在召唤。
                
                祭司袍在你身上轻轻飘动，仿佛在回应着神庙中的神秘力量。大主教的追兵
                正在逼近，你需要做出选择。
            """.trimIndent(),
            generatedOptions = listOf(
                StoryOption(
                    optionId = "perform_ritual",
                    text = "进行古老的祭祀仪式",
                    description = "通过神圣的仪式唤醒沉睡的神力，对抗追兵。",
                    requiredAttributes = mapOf("魄力" to 16, "信仰" to 10),
                    potentialConsequences = listOf("可能唤醒强大的神力", "消耗大量体力"),
                    estimatedTimeCost = 50
                ),
                StoryOption(
                    optionId = "seek_poet_help",
                    text = "寻找流浪诗人的帮助",
                    description = "流浪诗人知晓古老的秘密，或许能提供庇护。",
                    requiredAttributes = mapOf("说服力" to 12),
                    potentialConsequences = listOf("可能获得重要情报", "卷入诗人的麻烦"),
                    estimatedTimeCost = 25
                ),
                StoryOption(
                    optionId = "hide_in_ruins",
                    text = "在神庙废墟中隐藏",
                    description = "利用对神庙地形的熟悉，躲避追兵的搜捕。",
                    requiredAttributes = mapOf("洞察力" to 14),
                    potentialConsequences = listOf("可能被发现", "争取到宝贵时间"),
                    estimatedTimeCost = 30
                )
            ),
            confidenceScore = 0.91f
        )
    }
    
    /**
     * 生成默认响应
     */
    private fun generateDefaultResponse(context: NarrativeContext): NarrativeResponse {
        return NarrativeResponse(
            storySegment = "欢迎来到无限叙事世界！这是一个充满可能性的奇妙旅程。",
            generatedOptions = listOf(
                StoryOption(
                    optionId = "start_journey",
                    text = "开始你的叙事之旅",
                    description = "选择一个世界开始你的冒险。",
                    requiredAttributes = emptyMap(),
                    potentialConsequences = emptyList(),
                    estimatedTimeCost = null
                )
            ),
            confidenceScore = 0.95f
        )
    }
    
    /**
     * 创建世界状态管理器
     */
    private fun createWorldStateManager(): WorldStateManager {
        return object : WorldStateManager {
            override suspend fun getPlayerProgress(playerId: String): PlayerProgress {
                // 简化的实现，实际项目中需要从数据库读取
                return PlayerProgress(
                    playerId = playerId,
                    playerAttributes = PlayerAttributes(),
                    completedWorlds = listOf(),
                    activeQuests = listOf(),
                    inventoryItems = listOf(),
                    factionRelations = emptyMap(),
                    acquiredItems = listOf(),
                    keyDecisions = listOf()
                )
            }
            
            override suspend fun getAvailableWorlds(playerId: String): List<String> {
                val worlds = listOf("cyber_penglai", "law_maze", "decaying_throne")
                println("DEBUG: Available worlds for $playerId: $worlds")
                return worlds
            }
            
            override suspend fun updateWorldState(
                playerId: String,
                worldId: String,
                response: NarrativeResponse,
                context: NarrativeContext
            ): WorldState {
                // 简化的状态更新
                return context.worldState
            }
            
            override suspend fun unlockWorld(playerId: String, worldId: String) {
                // 实现世界解锁逻辑
            }
            
            override suspend fun recordKeyEvent(playerId: String, eventId: String, eventData: Map<String, Any>) {
                // 实现关键事件记录
            }
        }
    }
    
    /**
     * 创建叙事上下文管理器
     */
    private fun createNarrativeContextManager(): NarrativeContextManager {
        return object : NarrativeContextManager {
            override suspend fun buildNarrativeContext(
                playerId: String,
                worldId: String,
                playerChoice: String?
            ): NarrativeContext {
                // 根据世界ID返回相应的初始上下文
                return WorldSeedData
                    .getWorldConfig(worldId)?.startingContext
                    ?: NarrativeContext(
                        systemInstruction = "欢迎来到无限叙事世界",
                        worldState = WorldState(
                            currentWorld = worldId,
                            keyCharacters = emptyMap(),
                            unlockedLocations = emptyList(),
                            corePuzzles = emptyMap(),
                            storyThreads = emptyList()
                        ),
                        recentStory = "故事刚刚开始",
                        playerAttributes = PlayerAttributes(),
                        availableWorlds = listOf(worldId),
                        activeQuests = listOf(),
                        inventoryItems = listOf(),
                        factionRelations = emptyMap()
                    )
            }
            
            override suspend fun saveContextSnapshot(
                playerId: String,
                worldId: String,
                context: NarrativeContext,
                response: NarrativeResponse
            ) {
                // 保存上下文快照到数据库
            }
            
            override fun getRelevantContexts(
                playerId: String,
                worldId: String,
                contextType: String
            ): kotlinx.coroutines.flow.Flow<List<NarrativeContext>> {
                return kotlinx.coroutines.flow.flow { emit(emptyList()) }
            }
            
            override suspend fun cleanupExpiredContexts(playerId: String) {
                // 清理过期上下文
            }
        }
    }
}
