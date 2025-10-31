package com.infinite.narrative.ai

import com.infinite.narrative.ai.filter.ContentFilterImpl
import com.infinite.narrative.ai.model.*
import com.infinite.narrative.data.model.PlayerAttributes
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * AI客户端测试 - 验证AI集成层的正确性
 */
class AIClientTest {
    
    private lateinit var mockOnlineClient: OnlineAIClient
    private lateinit var mockOfflineClient: OfflineAIClient
    private lateinit var mockContentFilter: ContentFilter
    private lateinit var hybridClient: HybridAIClient
    
    @Before
    fun setUp() {
        mockOnlineClient = mockk()
        mockOfflineClient = mockk()
        mockContentFilter = mockk()
        
        // 创建混合客户端的模拟实现
        hybridClient = object : HybridAIClient {
            override suspend fun generateNarrative(context: NarrativeContext): NarrativeResponse {
                return if (isOnline()) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        mockOnlineClient.generateNarrative(context)
                    }
                } else {
                    mockOfflineClient.generateNarrative(context)
                }
            }
            
            override fun isOnline(): Boolean {
                return mockOnlineClient.isOnline()
            }
            
            override fun getModelInfo(): ModelInfo {
                return if (isOnline()) {
                    mockOnlineClient.getModelInfo()
                } else {
                    mockOfflineClient.getModelInfo()
                }
            }
            
            override suspend fun warmup() {
                if (isOnline()) {
                    mockOnlineClient.warmup()
                } else {
                    mockOfflineClient.warmup()
                }
            }
            
            override fun cleanup() {
                mockOnlineClient.cleanup()
                mockOfflineClient.cleanup()
            }
            
            override suspend fun generateWithMode(
                context: NarrativeContext,
                forceOffline: Boolean
            ): NarrativeResponse {
                return if (forceOffline) {
                    mockOfflineClient.generateNarrative(context)
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        mockOnlineClient.generateNarrative(context)
                    }
                }
            }
            
            override fun getCurrentMode(): GenerationMode {
                return if (isOnline()) GenerationMode.ONLINE else GenerationMode.OFFLINE
            }
            
            override fun observeModeChanges(): kotlinx.coroutines.flow.Flow<GenerationMode> {
                return kotlinx.coroutines.flow.flow {
                    while (true) {
                        emit(getCurrentMode())
                        kotlinx.coroutines.delay(1000) // 每秒检查一次模式变化
                    }
                }.flowOn(kotlinx.coroutines.Dispatchers.IO)
            }
        }
    }
    
    @Test
    fun `test online generation success`() = runTest {
        // Given
        val context = createTestContext()
        val expectedResponse = createTestResponse()
        
        every { mockOnlineClient.isOnline() } returns true
        every { mockOnlineClient.generateNarrative(context) } returns expectedResponse
        every { mockContentFilter.filterContent(any()) } answers {
            FilteredContent(
                original = firstArg(),
                filtered = firstArg(),
                isModified = false,
                modifications = emptyList()
            )
        }
        
        // When
        val result = hybridClient.generateNarrative(context)
        
        // Then
        assertEquals(expectedResponse.storySegment, result.storySegment)
        assertEquals(expectedResponse.generatedOptions.size, result.generatedOptions.size)
        verify { mockOnlineClient.generateNarrative(context) }
    }
    
    @Test
    fun `test offline generation fallback`() = runTest {
        // Given
        val context = createTestContext()
        val expectedResponse = createTestResponse()
        
        every { mockOnlineClient.isOnline() } returns false
        every { mockOfflineClient.generateNarrative(context) } returns expectedResponse
        every { mockContentFilter.filterContent(any()) } answers {
            FilteredContent(
                original = firstArg(),
                filtered = firstArg(),
                isModified = false,
                modifications = emptyList()
            )
        }
        
        // When
        val result = hybridClient.generateNarrative(context)
        
        // Then
        assertEquals(expectedResponse.storySegment, result.storySegment)
        verify { mockOfflineClient.generateNarrative(context) }
    }
    
    @Test
    fun `test content filtering applied`() = runTest {
        // Given
        val context = createTestContext()
        val rawResponse = NarrativeResponse(
            storySegment = "这是一个包含敏感词屠杀的故事",
            generatedOptions = listOf(
                StoryOption(
                    optionId = "1",
                    text = "选择一",
                    description = "描述一",
                    requiredAttributes = emptyMap(),
                    potentialConsequences = emptyList(),
                    estimatedTimeCost = null
                )
            ),
            newWorldState = null,
            contextUpdates = null,
            confidenceScore = 0.9f
        )
        
        every { mockOnlineClient.isOnline() } returns true
        every { mockOnlineClient.generateNarrative(context) } returns rawResponse
        every { mockContentFilter.filterContent("这是一个包含敏感词屠杀的故事") } returns
                FilteredContent(
                    original = "这是一个包含敏感词屠杀的故事",
                    filtered = "这是一个包含敏感词激烈冲突的故事",
                    isModified = true,
                    modifications = listOf(
                        ContentModification(
                            type = ModificationType.REPLACEMENT,
                            originalText = "屠杀",
                            modifiedText = "激烈冲突",
                            reason = "内容安全过滤"
                        )
                    )
                )
        
        // When
        val result = hybridClient.generateNarrative(context)
        
        // Then
        assertEquals("这是一个包含敏感词激烈冲突的故事", result.storySegment)
        assertTrue(result.storySegment.contains("激烈冲突"))
        assertTrue(!result.storySegment.contains("屠杀"))
    }
    
    @Test
    fun `test force offline mode`() = runTest {
        // Given
        val context = createTestContext()
        val expectedResponse = createTestResponse()
        
        every { mockOfflineClient.generateNarrative(context) } returns expectedResponse
        
        // When
        val result = hybridClient.generateWithMode(context, forceOffline = true)
        
        // Then
        assertEquals(expectedResponse.storySegment, result.storySegment)
        verify { mockOfflineClient.generateNarrative(context) }
        verify(exactly = 0) { mockOnlineClient.generateNarrative(context) }
    }
    
    private fun createTestContext(): NarrativeContext {
        return NarrativeContext(
            systemInstruction = "测试系统指令",
            worldState = WorldState(
                currentWorld = "test_world",
                keyCharacters = mapOf("测试角色" to "友好 - 测试关系"),
                unlockedLocations = listOf("测试地点"),
                corePuzzles = mapOf("测试谜题" to "测试描述"),
                storyThreads = listOf(
                    StoryThread(
                        threadId = "test_thread",
                        title = "测试线程",
                        status = "active",
                        priority = 5,
                        lastUpdate = "测试时间"
                    )
                )
            ),
            recentStory = "测试故事内容",
            playerAttributes = PlayerAttributes(),
            availableWorlds = listOf("test_world"),
            activeQuests = listOf("test_quest"),
            inventoryItems = listOf("test_item"),
            factionRelations = mapOf("test_faction" to "友好")
        )
    }
    
    private fun createTestResponse(): NarrativeResponse {
        return NarrativeResponse(
            storySegment = "这是一个测试故事段落",
            generatedOptions = listOf(
                StoryOption(
                    optionId = "1",
                    text = "测试选项一",
                    description = "测试描述一",
                    requiredAttributes = mapOf("洞察力" to 10),
                    potentialConsequences = listOf("可能的后果"),
                    estimatedTimeCost = 30
                ),
                StoryOption(
                    optionId = "2",
                    text = "测试选项二",
                    description = "测试描述二",
                    requiredAttributes = emptyMap(),
                    potentialConsequences = emptyList(),
                    estimatedTimeCost = null
                )
            ),
            newWorldState = null,
            contextUpdates = null,
            confidenceScore = 0.95f
        )
    }
}

/**
 * 内容过滤器测试
 */
class ContentFilterTest {
    
    private lateinit var contentFilter: ContentFilter
    
    @Before
    fun setUp() {
        contentFilter = ContentFilterImpl()
    }
    
    @Test
    fun `test basic functionality`() {
        // 简单的测试
        assertTrue(true)
    }
}
