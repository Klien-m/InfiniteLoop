package com.infinite.narrative.ai

import com.infinite.narrative.ai.model.NarrativeContext
import com.infinite.narrative.ai.model.NarrativeResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * AI客户端接口 - 支持在线API和离线模型的抽象
 */
interface AIClient {

    /**
     * 生成叙事内容
     * @param context 叙事上下文，包含世界状态和玩家属性
     * @return 叙事响应，包含故事片段和选项
     */
    suspend fun generateNarrative(context: NarrativeContext): NarrativeResponse

    /**
     * 检查是否在线
     * @return true表示可以使用在线API，false表示需要使用离线模型
     */
    fun isOnline(): Boolean

    /**
     * 获取模型信息
     * @return 模型名称和版本信息
     */
    fun getModelInfo(): ModelInfo

    /**
     * 预热模型（特别是离线模型）
     */
    suspend fun warmup()

    /**
     * 清理资源
     */
    fun cleanup()
}

/**
 * 在线AI客户端实现
 */
interface OnlineAIClient : AIClient {

    /**
     * 获取Retrofit服务接口
     */
    fun getService(): NarrativeApiService
}

/**
 * 离线AI客户端实现
 */
interface OfflineAIClient : AIClient {

    /**
     * 检查模型是否已加载
     */
    fun isModelLoaded(): Boolean

    /**
     * 加载模型（可能需要较长时间）
     */
    suspend fun loadModel(): Boolean

    /**
     * 卸载模型
     */
    fun unloadModel()
}

/**
 * 混合AI客户端 - 智能切换在线/离线模式
 */
interface HybridAIClient : AIClient {

    /**
     * 强制使用特定模式
     */
    suspend fun generateWithMode(
        context: NarrativeContext,
        forceOffline: Boolean
    ): NarrativeResponse

    /**
     * 获取当前模式信息
     */
    fun getCurrentMode(): GenerationMode

    /**
     * 监听模式变化
     */
    fun observeModeChanges(): Flow<GenerationMode>
}

/**
 * Narrative API服务接口
 */
interface NarrativeApiService {

    @POST("/generate")
    suspend fun generate(@Body request: NarrativeContext): NarrativeResponse

    @POST("/health")
    suspend fun healthCheck(): HealthResponse

    @POST("/models/{modelId}/load")
    suspend fun loadModel(@Path("modelId") modelId: String): ModelLoadResponse
}

/**
 * 健康检查响应
 */
data class HealthResponse(
    val status: String,
    val modelLoaded: Boolean,
    val timestamp: Long
)

/**
 * 模型加载响应
 */
data class ModelLoadResponse(
    val success: Boolean,
    val modelId: String,
    val loadTimeMs: Long
)

/**
 * 模型信息
 */
data class ModelInfo(
    val name: String,
    val version: String,
    val provider: String,
    val capabilities: List<String>
)

/**
 * 生成模式枚举
 */
enum class GenerationMode {
    ONLINE,     // 使用在线API
    OFFLINE,    // 使用离线模型
}

/**
 * AI生成异常
 */
class AIGenerationException(
    message: String,
    cause: Throwable? = null,
    val errorCode: String? = null
) : Exception(message, cause)

/**
 * 内容过滤器接口
 */
interface ContentFilter {

    /**
     * 过滤生成的内容
     * @param content 待过滤的内容
     * @return 过滤后的安全内容
     */
    fun filterContent(content: String): FilteredContent

    /**
     * 检查内容是否安全
     * @param content 待检查的内容
     * @return 安全性评估结果
     */
    fun checkSafety(content: String): SafetyResult
}

/**
 * 过滤后的内容
 */
data class FilteredContent(
    val original: String,
    val filtered: String,
    val isModified: Boolean,
    val modifications: List<ContentModification>
)

/**
 * 安全性评估结果
 */
data class SafetyResult(
    val isSafe: Boolean,
    val riskLevel: RiskLevel,
    val riskCategories: List<RiskCategory>,
    val confidence: Float
)

/**
 * 风险等级
 */
enum class RiskLevel {
    SAFE,           // 安全
    LOW_RISK,       // 低风险
    MEDIUM_RISK,    // 中风险
    HIGH_RISK       // 高风险
}

/**
 * 风险类别
 */
enum class RiskCategory {
    VIOLENCE,       // 暴力
    HATE,           // 仇恨
    SELF_HARM,      // 自残
    SEXUAL,         // 性相关内容
    HARASSMENT,     // 骚扰
    MISINFORMATION  // 虚假信息
}

/**
 * 内容修改记录
 */
data class ContentModification(
    val type: ModificationType,
    val originalText: String,
    val modifiedText: String,
    val reason: String
)

/**
 * 修改类型
 */
enum class ModificationType {
    REPLACEMENT,    // 替换
    DELETION,       // 删除
    CENSORING       // 屏蔽
}
