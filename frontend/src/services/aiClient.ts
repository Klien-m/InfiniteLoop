import { NarrativeContext, NarrativeResponse, AIClientConfig, ApiResponse } from '@/types'
import ApiService from './api'
import ContentFilter from './contentFilter'

class AIClient {
  private apiService: ApiService
  private contentFilter: ContentFilter
  private config: AIClientConfig

  constructor(config: AIClientConfig) {
    this.config = config
    this.apiService = new ApiService(config)
    this.contentFilter = new ContentFilter()
  }

  /**
   * 生成叙事内容
   */
  async generateNarrative(context: NarrativeContext): Promise<NarrativeResponse> {
    try {
      // 1. 检查API健康状态
      const isHealthy = await this.apiService.healthCheck()
      if (!isHealthy) {
        throw new Error('AI服务暂时不可用')
      }

      // 2. 生成原始响应
      const rawResponse = await this.apiService.generateNarrative(context)

      // 3. 内容安全过滤
      const filteredResponse = this.filterResponse(rawResponse)

      return filteredResponse
    } catch (error) {
      console.error('AI生成失败:', error)
      throw error
    }
  }

  /**
   * 过滤AI响应内容
   */
  private filterResponse(response: NarrativeResponse): NarrativeResponse {
    // 过滤故事段落
    const filteredStory = this.contentFilter.filterContent(response.storySegment)

    // 过滤选项文本
    const filteredOptions = response.generatedOptions.map(option => {
      const filteredText = this.contentFilter.filterContent(option.text)
      const filteredDescription = this.contentFilter.filterContent(option.description)

      return {
        ...option,
        text: filteredText.filtered,
        description: filteredDescription.filtered
      }
    })

    return {
      ...response,
      storySegment: filteredStory.filtered,
      generatedOptions: filteredOptions
    }
  }

  /**
   * 检查是否在线
   */
  async isOnline(): Promise<boolean> {
    return this.apiService.healthCheck()
  }

  /**
   * 获取模型信息
   */
  getModelInfo(): { name: string; version: string; provider: string } {
    return {
      name: 'KAT-Coder-Pro',
      version: 'V1',
      provider: 'DeepSeek'
    }
  }

  /**
   * 更新配置
   */
  updateConfig(config: Partial<AIClientConfig>) {
    this.config = { ...this.config, ...config }
    this.apiService.updateConfig(this.config)
  }

  /**
   * 获取当前配置
   */
  getConfig(): AIClientConfig {
    return { ...this.config }
  }
}

// 创建默认实例
let defaultClient: AIClient | null = null

export function createAIClient(config: AIClientConfig): AIClient {
  return new AIClient(config)
}

export function getDefaultAIClient(): AIClient {
  if (!defaultClient) {
    const config: AIClientConfig = {
      apiKey: process.env.VITE_API_KEY || '',
      baseUrl: process.env.VITE_API_BASE_URL || 'https://api.katcoder.pro/v1',
      timeoutSeconds: 30,
      maxRetries: 3,
      modelQuantization: 'q4_0'
    }
    defaultClient = new AIClient(config)
  }
  return defaultClient
}

export default AIClient
export { createAIClient, getDefaultAIClient }