package com.infinite.narrative.data.seed

import com.infinite.narrative.ai.model.NarrativeContext
import com.infinite.narrative.ai.model.StoryThread
import com.infinite.narrative.ai.model.WorldConfig
import com.infinite.narrative.ai.model.WorldState
import com.infinite.narrative.data.model.PlayerAttributes
import com.infinite.narrative.data.model.Character
import com.infinite.narrative.data.model.Item
import com.infinite.narrative.data.model.ItemMetadata
import com.infinite.narrative.data.model.ItemType
import com.infinite.narrative.data.model.Rarity

/**
 * 世界观种子数据 - 对应deepseek_markdown.md中的三个叙事种子
 */
object WorldSeedData {

    /**
     * 赛博蓬莱世界配置
     */
    val CYBER_PENGLAI = WorldConfig(
        worldId = "cyber_penglai",
        name = "赛博蓬莱",
        description = "在未来都市，你的意识可以上传至仙境网络，在代码与灵气的冲突中寻求长生。",
        stylePrompt = "融合赛博朋克的冰冷与东方古典的优雅，使用科技感词汇与古典诗词意境相结合的文风。",
        startingContext = NarrativeContext(
            systemInstruction = "你将是《赛博蓬莱》世界的叙述者。文风需融合赛博朋克的冰冷与东方古典的优雅。",
            worldState = WorldState(
                currentWorld = "cyber_penglai",
                keyCharacters = mapOf(
                    "龙门商会" to "敌对 - 主要商业竞争对手",
                    "云梦" to "中立 - 神秘的网络游侠",
                    "老道长" to "友好 - 传统修仙者代表"
                ),
                unlockedLocations = listOf("霓虹街市", "数据庙宇"),
                corePuzzles = mapOf(
                    "云端仙境入口" to "寻找传说中的虚拟修仙空间入口"
                ),
                storyThreads = listOf(
                    StoryThread(
                        threadId = "main_quest",
                        title = "寻找云端仙境",
                        status = "active",
                        priority = 10,
                        lastUpdate = "故事开始"
                    ),
                    StoryThread(
                        threadId = "character_personal",
                        title = "云梦的身世之谜",
                        status = "dormant",
                        priority = 6,
                        lastUpdate = "初次相遇"
                    )
                )
            ),
            recentStory = "你是一名初入赛博都市的叙事行者，刚刚从龙门商会的追捕中逃脱。",
            playerAttributes = PlayerAttributes(),
            availableWorlds = listOf("cyber_penglai"),
            activeQuests = listOf("寻找云端仙境入口"),
            inventoryItems = listOf("破损的玉简"),
            factionRelations = mapOf("龙门商会" to "敌对")
        ),
        availableCharacters = listOf("龙门商会", "云梦", "老道长"),
        initialItems = listOf("破损的玉简"),
        storyTemplates = listOf(
            "在霓虹闪烁的街道上，你看到...",
            "数据流中浮现出古老的符箓...",
            "突然，一个身影从暗处走出..."
        )
    )

    /**
     * 法典迷城世界配置
     */
    val LAW_MAZE = WorldConfig(
        worldId = "law_maze",
        name = "法典迷城",
        description = "在一个法律条文能直接改写物理规则的世界，你是一名律法侠盗，利用法律的漏洞施展超能力。",
        stylePrompt = "营造法律与超能力结合的奇幻氛围，强调逻辑推理与规则破解的智斗感。",
        startingContext = NarrativeContext(
            systemInstruction = "你将是《法典迷城》世界的叙述者。需要展现法律条文与超能力的奇妙结合。",
            worldState = WorldState(
                currentWorld = "law_maze",
                keyCharacters = mapOf(
                    "法官大人" to "敌对 - 法律秩序的维护者",
                    "小律师" to "友好 - 初出茅庐的法律新人",
                    "法典之灵" to "神秘 - 法律条文的具象化存在"
                ),
                unlockedLocations = listOf("法庭大厅", "法律图书馆"),
                corePuzzles = mapOf(
                    "法理悖论" to "寻找能够颠覆现有法律体系的终极悖论"
                ),
                storyThreads = listOf(
                    StoryThread(
                        threadId = "main_quest",
                        title = "破解法理悖论",
                        status = "active",
                        priority = 10,
                        lastUpdate = "故事开始"
                    ),
                    StoryThread(
                        threadId = "character_personal",
                        title = "小律师的成长之路",
                        status = "dormant",
                        priority = 5,
                        lastUpdate = "初次相遇"
                    )
                )
            ),
            recentStory = "你作为一名律法侠盗，刚刚在法庭上利用法律漏洞逃脱了法官的追捕。",
            playerAttributes = PlayerAttributes(),
            availableWorlds = listOf("law_maze"),
            activeQuests = listOf("寻找法理悖论"),
            inventoryItems = listOf(),
            factionRelations = mapOf("法官大人" to "敌对")
        ),
        availableCharacters = listOf("法官大人", "小律师", "法典之灵"),
        initialItems = listOf(),
        storyTemplates = listOf(
            "法庭的钟声响起，新的审判即将开始...",
            "法律条文在空中浮现，化作实体的锁链...",
            "你发现了一个前所未见的法律漏洞..."
        )
    )

    /**
     * 衰败王座世界配置
     */
    val DECAYING_THRONE = WorldConfig(
        worldId = "decaying_throne",
        name = "衰败王座",
        description = "你是某个被遗忘之神的最后一位祭司，在一个无神的世界里，收集信仰，重现神迹。",
        stylePrompt = "营造古老、神秘、略带悲伤的氛围，强调信仰与神迹的庄严感。",
        startingContext = NarrativeContext(
            systemInstruction = "你将是《衰败王座》世界的叙述者。需要展现古老神庙的庄严与信仰的重量。",
            worldState = WorldState(
                currentWorld = "decaying_throne",
                keyCharacters = mapOf(
                    "大主教" to "敌对 - 现有宗教的领袖",
                    "流浪诗人" to "友好 - 信仰的传播者",
                    "古老神像" to "神秘 - 被遗忘之神的象征"
                ),
                unlockedLocations = listOf("废弃神庙", "信仰集市"),
                corePuzzles = mapOf(
                    "神之低语" to "寻找被遗忘之神的最后启示"
                ),
                storyThreads = listOf(
                    StoryThread(
                        threadId = "main_quest",
                        title = "重现神迹",
                        status = "active",
                        priority = 10,
                        lastUpdate = "故事开始"
                    ),
                    StoryThread(
                        threadId = "character_personal",
                        title = "流浪诗人的秘密",
                        status = "dormant",
                        priority = 4,
                        lastUpdate = "初次相遇"
                    )
                )
            ),
            recentStory = "你作为最后一位祭司，在废弃的神庙中醒来，感受到信仰的微弱呼唤。",
            playerAttributes = PlayerAttributes(),
            availableWorlds = listOf("decaying_throne"),
            activeQuests = listOf("寻找神之低语"),
            inventoryItems = listOf("古老的祭司袍"),
            factionRelations = mapOf("大主教" to "敌对")
        ),
        availableCharacters = listOf("大主教", "流浪诗人", "古老神像"),
        initialItems = listOf("古老的祭司袍"),
        storyTemplates = listOf(
            "神庙的钟声在风中回荡，仿佛在呼唤...",
            "古老的壁画上，神明的目光似乎在追随你...",
            "你感受到一股微弱但熟悉的信仰之力..."
        )
    )

    /**
     * 获取所有世界观配置
     */
    fun getAllWorldConfigs(): List<WorldConfig> = listOf(
        CYBER_PENGLAI,
        LAW_MAZE,
        DECAYING_THRONE
    )

    /**
     * 根据世界ID获取配置
     */
    fun getWorldConfig(worldId: String): WorldConfig? = when (worldId) {
        "cyber_penglai" -> CYBER_PENGLAI
        "law_maze" -> LAW_MAZE
        "decaying_throne" -> DECAYING_THRONE
        else -> null
    }

    /**
     * 初始角色数据
     */
    val INITIAL_CHARACTERS = listOf(
        // 赛博蓬莱角色
        Character(
            id = "cyber_penglai_lmh商会",
            name = "龙门商会",
            description = "赛博都市中最大的商业集团，掌握着数据和资源的命脉。",
            faction = "商业集团",
            imageUrl = "cyber_penglai/lmh商会.png"
        ),
        Character(
            id = "cyber_penglai_yunmeng",
            name = "云梦",
            description = "神秘的网络游侠，能在数据流中自由穿梭。",
            faction = "自由战士",
            imageUrl = "cyber_penglai/yunmeng.png"
        ),
        Character(
            id = "cyber_penglai_laodaozhang",
            name = "老道长",
            description = "坚持传统修仙之道的最后传人。",
            faction = "修仙者",
            imageUrl = "cyber_penglai/laodaozhang.png"
        ),

        // 法典迷城角色
        Character(
            id = "law_maze_judge",
            name = "法官大人",
            description = "法律秩序的坚定维护者，能用法槌改变现实。",
            faction = "司法系统",
            imageUrl = "law_maze/judge.png"
        ),
        Character(
            id = "law_maze_lawyer",
            name = "小律师",
            description = "初出茅庐的法律新人，渴望改变现状。",
            faction = "法律新人",
            imageUrl = "law_maze/lawyer.png"
        ),
        Character(
            id = "law_maze_fadianling",
            name = "法典之灵",
            description = "法律条文的具象化存在，知晓所有法律秘密。",
            faction = "神秘存在",
            imageUrl = "law_maze/fadianling.png"
        ),

        // 衰败王座角色
        Character(
            id = "decaying_throne_archbishop",
            name = "大主教",
            description = "现有宗教的领袖，视你为异端。",
            faction = "主流宗教",
            imageUrl = "decaying_throne/archbishop.png"
        ),
        Character(
            id = "decaying_throne_poet",
            name = "流浪诗人",
            description = "游走各地传播信仰的诗人，知晓古老秘密。",
            faction = "流浪者",
            imageUrl = "decaying_throne/poet.png"
        ),
        Character(
            id = "decaying_throne_god_statue",
            name = "古老神像",
            description = "被遗忘之神的象征，蕴含着神力。",
            faction = "神明",
            imageUrl = "decaying_throne/god_statue.png"
        )
    )

    /**
     * 初始物品数据
     */
    val INITIAL_ITEMS = listOf(
        // 赛博蓬莱物品
        Item(
            id = "cyber_penglai_broken_jade",
            name = "破损的玉简",
            description = "记载着云端仙境线索的古老玉简，虽然破损但仍蕴含信息。",
            itemType = ItemType.CONCEPTUAL,
            rarity = Rarity.UNCOMMON,
            value = 100,
            metadata = ItemMetadata.ConceptualMetadata(
                conditions = listOf("需要在数据庙宇中修复")
            )
        ),

        // 法典迷城物品
        Item(
            id = "law_maze_legal_code",
            name = "基础法律典籍",
            description = "包含基本法律条文的典籍，是律法侠盗的入门读物。",
            itemType = ItemType.KNOWLEDGE,
            rarity = Rarity.COMMON,
            value = 50,
            metadata = ItemMetadata.KnowledgeMetadata(
                skillLevel = 1,
                learningTime = 30
            )
        ),

        // 衰败王座物品
        Item(
            id = "decaying_throne_priest_robe",
            name = "古老的祭司袍",
            description = "传承自上古祭司的法袍，蕴含着微弱的神力。",
            itemType = ItemType.SPIRITUAL,
            rarity = Rarity.RARE,
            value = 200,
            metadata = ItemMetadata.SpiritualMetadata(
                power = 2,
                source = "被遗忘之神",
                sideEffects = listOf("会吸引神敌的注意")
            )
        )
    )
}
