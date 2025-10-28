// 核心数据模型 - 基于Android项目移植

// 玩家属性
export interface PlayerAttributes {
  insight: number    // 洞察力
  persuasion: number // 说服力
 魄力: number       // 魄力
  [key: string]: number
}

// 世界状态
export interface WorldState {
  currentWorld: string
  keyCharacters: Record<string, string>
  unlockedLocations: string[]
  corePuzzles: Record<string, string>
  storyThreads: StoryThread[]
  timeProgression?: string
}

// 故事线
export interface StoryThread {
  threadId: string
  title: string
  status: 'active' | 'dormant' | 'completed'
  priority: number // 1-10
  lastUpdate: string
}

// 叙事上下文
export interface NarrativeContext {
  systemInstruction: string
  worldState: WorldState
  recentStory: string
  playerAttributes: PlayerAttributes
  availableWorlds: string[]
  activeQuests: string[]
  inventoryItems: string[]
  factionRelations: Record<string, string>
  narrativeAnchor?: string
}

// 故事选项
export interface StoryOption {
  optionId: string
  text: string
  description: string
  requiredAttributes?: Record<string, number>
  potentialConsequences?: string[]
  estimatedTimeCost?: number // 分钟
}

// AI生成响应
export interface NarrativeResponse {
  storySegment: string
  generatedOptions: StoryOption[]
  newWorldState?: WorldState
  contextUpdates?: ContextUpdate[]
  confidenceScore: number
}

// 上下文更新
export interface ContextUpdate {
  updateType: string // "character_relation", "item_acquired", "quest_progress"等
  targetId: string
  changeData: Record<string, any>
  timestamp: number
}

// AI客户端配置
export interface AIClientConfig {
  apiKey: string
  baseUrl: string
  timeoutSeconds: number
  maxRetries: number
  offlineModelPath?: string
  modelQuantization: string
}

// 世界配置
export interface WorldConfig {
  worldId: string
  name: string
  description: string
  stylePrompt: string
  startingContext: NarrativeContext
  availableCharacters: string[]
  initialItems: string[]
  storyTemplates: string[]
}

// 玩家进度
export interface PlayerProgress {
  playerId: string
  playerAttributes: PlayerAttributes
  completedWorlds: string[]
  activeQuests: string[]
  inventoryItems: string[]
  factionRelations: Record<string, string>
  acquiredItems: string[]
  keyDecisions: string[]
  currentWorld?: string
}

// 世界选择结果
export interface WorldSelectionResult {
  recommendation: string
  worldOptions: WorldOption[]
  reasoning: string
}

// 世界选项
export interface WorldOption {
  worldId: string
  name: string
  description: string
  requiredAttributes: Record<string, number>
}

// 故事生成结果
export interface StoryGenerationResult {
  storySegment: string
  options: StoryOption[]
  worldState: WorldState
  confidenceScore: number
}

// 内容过滤相关
export interface FilteredContent {
  original: string
  filtered: string
  isModified: boolean
  modifications: ContentModification[]
}

export interface SafetyResult {
  isSafe: boolean
  riskLevel: RiskLevel
  riskCategories: RiskCategory[]
  confidence: number
}

export interface ContentModification {
  type: ModificationType
  originalText: string
  modifiedText: string
  reason: string
}

export type RiskLevel = 'SAFE' | 'LOW_RISK' | 'MEDIUM_RISK' | 'HIGH_RISK'
export type RiskCategory = 'VIOLENCE' | 'HATE' | 'SELF_HARM' | 'SEXUAL' | 'HARASSMENT' | 'MISINFORMATION'
export type ModificationType = 'REPLACEMENT' | 'DELETION' | 'CENSORING'

// API响应
export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: string
  code?: string
}

// 游戏状态
export interface GameState {
  playerId: string
  currentWorldId?: string
  playerAttributes: PlayerAttributes
  worldState?: WorldState
  storyHistory: NarrativeResponse[]
  inventory: string[]
  unlockedWorlds: string[]
  gameProgress: number
  isInitialized: boolean
}