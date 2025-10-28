import { 
  NarrativeContext, 
  NarrativeResponse, 
  StoryGenerationResult, 
  WorldSelectionResult, 
  WorldOption,
  PlayerProgress,
  WorldState,
  PlayerAttributes,
  StoryOption
} from '@/types'
import AIClient, { getDefaultAIClient } from './aiClient'

interface WorldStateManager {
  getPlayerProgress(playerId: string): Promise<PlayerProgress>
  getAvailableWorlds(playerId: string): Promise<string[]>
  updateWorldState(
    playerId: string,
    worldId: string,
    response: NarrativeResponse,
    context: NarrativeContext
  ): Promise<WorldState>
  unlockWorld(playerId: string, worldId: string): Promise<void>
  recordKeyEvent(playerId: string, eventId: string, eventData: Record<string, any>): Promise<void>
}

interface NarrativeContextManager {
  buildNarrativeContext(
    playerId: string,
    worldId: string,
    playerChoice?: string
  ): Promise<NarrativeContext>
  saveContextSnapshot(
    playerId: string,
    worldId: string,
    context: NarrativeContext,
    response: NarrativeResponse
  ): Promise<void>
  cleanupExpiredContexts(playerId: string): Promise<void>
}

class NarrativeEngine {
  private aiClient: AIClient
  private worldStateManager: WorldStateManager
  private contextManager: NarrativeContextManager

  constructor(
    aiClient: AIClient,
    worldStateManager: WorldStateManager,
    contextManager: NarrativeContextManager
  ) {
    this.aiClient = aiClient
    this.worldStateManager = worldStateManager
    this.contextManager = contextManager
  }

  /**
   * 生成新的故事片段
   */
  async generateStory(
    playerId: string,
    worldId: string,
    playerChoice?: string
  ): Promise<StoryGenerationResult> {
    try {
      // 1. 构建叙事上下文
      const context = await this.contextManager.buildNarrativeContext(
        playerId,
        worldId,
        playerChoice
      )

      // 2. 生成AI响应
      const rawResponse = await this.aiClient.generateNarrative(context)

      // 3. 更新世界状态
      const updatedState = await this.worldStateManager.updateWorldState(
        playerId,
        worldId,
        rawResponse,
        context
      )

      // 4. 保存上下文快照
      await this.contextManager.saveContextSnapshot(
        playerId,
        worldId,
        context,
        rawResponse
      )

      return {
        storySegment: rawResponse.storySegment,
        options: rawResponse.generatedOptions,
        worldState: updatedState,
        confidenceScore: rawResponse.confidenceScore
      }
    } catch (error) {
      console.error('故事生成失败:', error)
      throw new Error('故事生成失败，请稍后重试')
    }
  }

  /**
   * 构建世界选择上下文
   */
  async generateWorldSelection(playerId: string): Promise<WorldSelectionResult> {
    try {
      const playerProgress = await this.worldStateManager.getPlayerProgress(playerId)
      const availableWorlds = await this.worldStateManager.getAvailableWorlds(playerId)

      // 构建世界选择上下文
      const context: NarrativeContext = {
        systemInstruction: '你是一位世界引导者，需要根据玩家的成长和经历，推荐最适合的下一个故事世界。每个世界都有独特的风格和挑战。',
        worldState: {
          currentWorld: 'nexus', // 叙事行者的大本营
          keyCharacters: {},
          unlockedLocations: ['万象之根', '叙事长廊'],
          corePuzzles: {},
          storyThreads: []
        },
        recentStory: this.buildPlayerHistorySummary(playerProgress),
        playerAttributes: playerProgress.playerAttributes,
        availableWorlds,
        activeQuests: playerProgress.activeQuests,
        inventoryItems: playerProgress.inventoryItems,
        factionRelations: playerProgress.factionRelations
      }

      const response = await this.aiClient.generateNarrative(context)
      const filteredResponse = this.filterResponse(response)

      return {
        recommendation: filteredResponse.storySegment,
        worldOptions: this.extractWorldOptions(filteredResponse.generatedOptions),
        reasoning: this.extractWorldReasoning(filteredResponse.storySegment)
      }
    } catch (error) {
      console.error('世界选择生成失败:', error)
      throw new Error('世界选择生成失败，请稍后重试')
    }
  }

  /**
   * 过滤AI响应
   */
  private filterResponse(response: NarrativeResponse): NarrativeResponse {
    // 这里可以添加额外的业务逻辑过滤
    return response
  }

  /**
   * 构建玩家历史摘要
   */
  private buildPlayerHistorySummary(progress: PlayerProgress): string {
    const sb: string[] = []
    sb.push('玩家叙事行者的历史摘要：\n')

    if (progress.completedWorlds.length > 0) {
      sb.push(`已完成的世界：${progress.completedWorlds.join(', ')}\n`)
    }

    if (progress.acquiredItems.length > 0) {
      sb.push(`获得的独特物品：${progress.acquiredItems.slice(0, 5).join(', ')}\n`)
    }

    if (progress.keyDecisions.length > 0) {
      sb.push(`关键抉择：${progress.keyDecisions.slice(0, 3).join('； ')}\n`)
    }

    sb.push(`当前属性：洞察力${progress.playerAttributes.insight}，说服力${progress.playerAttributes.persuasion}，魄力${progress.playerAttributes.魄力}`)

    return sb.join('')
  }

  /**
   * 从选项中提取世界选择
   */
  private extractWorldOptions(options: StoryOption[]): WorldOption[] {
    return options.map(option => ({
      worldId: option.optionId,
      name: option.text,
      description: option.description,
      requiredAttributes: option.requiredAttributes || {}
    }))
  }

  /**
   * 从故事中提取世界推荐理由
   */
  private extractWorldReasoning(story: string): string {
    // 简单实现：提取包含"因为"、"所以"、"适合"、"推荐"等关键词的句子
    const lines = story.split('\n').filter(line => 
      line.includes('因为') || 
      line.includes('所以') || 
      line.includes('适合') || 
      line.includes('推荐')
    )
    
    return lines.join(' ').trim() || '基于你的经历和能力，这个选择最为合适。'
  }

  /**
   * 获取AI客户端
   */
  getAIClient(): AIClient {
    return this.aiClient
  }

  /**
   * 更新AI客户端配置
   */
  updateAIClientConfig(config: Partial<NarrativeResponse>) {
    // 这里可以添加配置更新逻辑
  }
}

// 默认的世界状态管理器实现
class DefaultWorldStateManager implements WorldStateManager {
  async getPlayerProgress(playerId: string): Promise<PlayerProgress> {
    // 从localStorage获取玩家进度
    const saved = localStorage.getItem(`player_progress_${playerId}`)
    if (saved) {
      return JSON.parse(saved)
    }

    // 返回默认进度
    return {
      playerId,
      playerAttributes: { insight: 10, persuasion: 10, 魄力: 10 },
      completedWorlds: [],
      activeQuests: [],
      inventoryItems: [],
      factionRelations: {},
      acquiredItems: [],
      keyDecisions: [],
      currentWorld: undefined
    }
  }

  async getAvailableWorlds(playerId: string): Promise<string[]> {
    // 基于玩家进度返回可用世界
    const progress = await this.getPlayerProgress(playerId)
    const baseWorlds = ['赛博蓬莱', '法典迷城', '衰败王座']
    
    // 可以根据完成的世界解锁新世界
    return baseWorlds
  }

  async updateWorldState(
    playerId: string,
    worldId: string,
    response: NarrativeResponse,
    context: NarrativeContext
  ): Promise<WorldState> {
    // 更新世界状态逻辑
    const newState = response.newWorldState || context.worldState
    
    // 保存进度
    const progress = await this.getPlayerProgress(playerId)
    progress.currentWorld = worldId
    
    localStorage.setItem(`player_progress_${playerId}`, JSON.stringify(progress))
    localStorage.setItem(`world_state_${playerId}_${worldId}`, JSON.stringify(newState))

    return newState
  }

  async unlockWorld(playerId: string, worldId: string): Promise<void> {
    const progress = await this.getPlayerProgress(playerId)
    if (!progress.completedWorlds.includes(worldId)) {
      progress.completedWorlds.push(worldId)
      localStorage.setItem(`player_progress_${playerId}`, JSON.stringify(progress))
    }
  }

  async recordKeyEvent(playerId: string, eventId: string, eventData: Record<string, any>): Promise<void> {
    const progress = await this.getPlayerProgress(playerId)
    progress.keyDecisions.push(`${eventId}: ${JSON.stringify(eventData)}`)
    localStorage.setItem(`player_progress_${playerId}`, JSON.stringify(progress))
  }
}

// 默认的上下文管理器实现
class DefaultNarrativeContextManager implements NarrativeContextManager {
  async buildNarrativeContext(
    playerId: string,
    worldId: string,
    playerChoice?: string
  ): Promise<NarrativeContext> {
    const progress = await this.getPlayerProgress(playerId)
    const savedState = localStorage.getItem(`world_state_${playerId}_${worldId}`)
    const worldState: WorldState = savedState ? JSON.parse(savedState) : {
      currentWorld: worldId,
      keyCharacters: {},
      unlockedLocations: [],
      corePuzzles: {},
      storyThreads: []
    }

    // 构建系统指令
    const systemInstruction = this.buildSystemInstruction(worldId)

    return {
      systemInstruction,
      worldState,
      recentStory: this.buildRecentStory(playerId, worldId),
      playerAttributes: progress.playerAttributes,
      availableWorlds: await this.getAvailableWorlds(playerId),
      activeQuests: progress.activeQuests,
      inventoryItems: progress.inventoryItems,
      factionRelations: progress.factionRelations
    }
  }

  async saveContextSnapshot(
    playerId: string,
    worldId: string,
    context: NarrativeContext,
    response: NarrativeResponse
  ): Promise<void> {
    const snapshot = {
      context,
      response,
      timestamp: Date.now()
    }
    
    // 保存最近的上下文快照
    const snapshots = JSON.parse(localStorage.getItem(`context_snapshots_${playerId}_${worldId}`) || '[]')
    snapshots.push(snapshot)
    
    // 只保留最近的10个快照
    if (snapshots.length > 10) {
      snapshots.shift()
    }
    
    localStorage.setItem(`context_snapshots_${playerId}_${worldId}`, JSON.stringify(snapshots))
  }

  async cleanupExpiredContexts(playerId: string): Promise<void> {
    // 清理过期的上下文快照
    const threeDaysAgo = Date.now() - 3 * 24 * 60 * 60 * 1000
    
    for (const key of Object.keys(localStorage)) {
      if (key.startsWith(`context_snapshots_${playerId}_`)) {
        const snapshots = JSON.parse(localStorage.getItem(key) || '[]')
        const filtered = snapshots.filter((snapshot: any) => snapshot.timestamp > threeDaysAgo)
        
        if (filtered.length === 0) {
          localStorage.removeItem(key)
        } else {
          localStorage.setItem(key, JSON.stringify(filtered))
        }
      }
    }
  }

  private async getPlayerProgress(playerId: string): Promise<any> {
    const saved = localStorage.getItem(`player_progress_${playerId}`)
    return saved ? JSON.parse(saved) : {
      playerId,
      playerAttributes: { insight: 10, persuasion: 10, 魄力: 10 },
      completedWorlds: [],
      activeQuests: [],
      inventoryItems: [],
      factionRelations: {},
      acquiredItems: [],
      keyDecisions: []
    }
  }

  private async getAvailableWorlds(playerId: string): Promise<string[]> {
    return ['赛博蓬莱', '法典迷城', '衰败王座']
  }

  private buildSystemInstruction(worldId: string): string {
    const instructions: Record<string, string> = {
      '赛博蓬莱': '你将是《赛博蓬莱》世界的叙述者。文风需融合赛博朋克的冰冷与东方古典的优雅。',
      '法典迷城': '你将是《法典迷城》世界的叙述者。文风需融合法律条文的严谨与侠盗的豪迈。',
      '衰败王座': '你将是《衰败王座》世界的叙述者。文风需融合神迹的神秘与衰败的苍凉。'
    }
    
    return instructions[worldId] || '你是一个故事世界的叙述者，请根据世界特点调整文风。'
  }

  private buildRecentStory(playerId: string, worldId: string): string {
    const snapshots = JSON.parse(localStorage.getItem(`context_snapshots_${playerId}_${worldId}`) || '[]')
    
    if (snapshots.length === 0) {
      return '故事刚开始，一切皆有可能。'
    }
    
    // 返回最近的3个故事片段
    const recent = snapshots.slice(-3).map((snapshot: any) => snapshot.response.storySegment)
    return recent.join('\n\n')
  }
}

// 创建默认实例
export function createNarrativeEngine(): NarrativeEngine {
  const aiClient = getDefaultAIClient()
  const worldStateManager = new DefaultWorldStateManager()
  const contextManager = new DefaultNarrativeContextManager()
  
  return new NarrativeEngine(aiClient, worldStateManager, contextManager)
}

export default NarrativeEngine
export { createNarrativeEngine }