import { create } from 'zustand'
import { persist, subscribeWithSelector } from 'zustand/middleware'
import { immer } from 'zustand/middleware/immer'
import { 
  GameState, 
  PlayerAttributes, 
  WorldState, 
  NarrativeResponse, 
  StoryOption,
  WorldOption
} from '@/types'

interface GameStore {
  // 游戏状态
  gameState: GameState
  isLoading: boolean
  error: string | null

  // 操作方法
  setPlayerId: (playerId: string) => void
  updatePlayerAttributes: (attributes: Partial<PlayerAttributes>) => void
  setCurrentWorld: (worldId: string) => void
  updateWorldState: (worldState: WorldState) => void
  addStoryToHistory: (response: NarrativeResponse) => void
  selectOption: (option: StoryOption) => void
  unlockWorld: (worldId: string) => void
  addInventoryItem: (item: string) => void

  // 世界选择相关
  availableWorlds: WorldOption[]
  selectedWorld: WorldOption | null
  setAvailableWorlds: (worlds: WorldOption[]) => void
  setSelectedWorld: (world: WorldOption | null) => void

  // UI状态
  isTyping: boolean
  setIsTyping: (isTyping: boolean) => void

  // 重置和清理
  resetGame: () => void
  clearError: () => void
}

const useGameStore = create<GameStore>()(
  persist(
    immer((set, get) => ({
      // 初始状态
      gameState: {
        playerId: '',
        playerAttributes: { insight: 10, persuasion: 10, 魄力: 10 },
        storyHistory: [],
        inventory: [],
        unlockedWorlds: [],
        gameProgress: 0,
        isInitialized: false
      },
      isLoading: false,
      error: null,
      availableWorlds: [],
      selectedWorld: null,
      isTyping: false,

      // 操作方法实现
      setPlayerId: (playerId: string) => {
        set((state) => {
          state.gameState.playerId = playerId
          state.gameState.isInitialized = true
        })
      },

      updatePlayerAttributes: (attributes: Partial<PlayerAttributes>) => {
        set((state) => {
          Object.assign(state.gameState.playerAttributes, attributes)
        })
      },

      setCurrentWorld: (worldId: string) => {
        set((state) => {
          state.gameState.currentWorldId = worldId
        })
      },

      updateWorldState: (worldState: WorldState) => {
        set((state) => {
          state.gameState.worldState = worldState
        })
      },

      addStoryToHistory: (response: NarrativeResponse) => {
        set((state) => {
          state.gameState.storyHistory.push(response)
        })
      },

      selectOption: (option: StoryOption) => {
        set((state) => {
          // 可以在这里添加选项选择的逻辑
          // 比如检查属性要求、更新进度等
        })
      },

      unlockWorld: (worldId: string) => {
        set((state) => {
          if (!state.gameState.unlockedWorlds.includes(worldId)) {
            state.gameState.unlockedWorlds.push(worldId)
          }
        })
      },

      addInventoryItem: (item: string) => {
        set((state) => {
          if (!state.gameState.inventory.includes(item)) {
            state.gameState.inventory.push(item)
          }
        })
      },

      setAvailableWorlds: (worlds: WorldOption[]) => {
        set((state) => {
          state.availableWorlds = worlds
        })
      },

      setSelectedWorld: (world: WorldOption | null) => {
        set((state) => {
          state.selectedWorld = world
        })
      },

      setIsTyping: (isTyping: boolean) => {
        set((state) => {
          state.isTyping = isTyping
        })
      },

      resetGame: () => {
        set((state) => {
          state.gameState = {
            playerId: '',
            playerAttributes: { insight: 10, persuasion: 10, 魄力: 10 },
            storyHistory: [],
            inventory: [],
            unlockedWorlds: [],
            gameProgress: 0,
            isInitialized: false
          }
          state.isLoading = false
          state.error = null
          state.availableWorlds = []
          state.selectedWorld = null
          state.isTyping = false
        })
      },

      clearError: () => {
        set((state) => {
          state.error = null
        })
      }
    })),
    {
      name: 'infinite-narrative-game',
      partialize: (state) => ({
        gameState: {
          playerId: state.gameState.playerId,
          playerAttributes: state.gameState.playerAttributes,
          currentWorldId: state.gameState.currentWorldId,
          storyHistory: state.gameState.storyHistory,
          inventory: state.gameState.inventory,
          unlockedWorlds: state.gameState.unlockedWorlds,
          gameProgress: state.gameState.gameProgress
        },
        availableWorlds: state.availableWorlds
      })
    }
  )
)

// 订阅状态变化，用于调试和分析
subscribeWithSelector(useGameStore, (state) => state.gameState.storyHistory, (history) => {
  console.log('故事历史更新:', history.length, '个片段')
})

subscribeWithSelector(useGameStore, (state) => state.isLoading, (loading) => {
  if (loading) {
    console.log('游戏正在加载...')
  } else {
    console.log('游戏加载完成')
  }
})

export default useGameStore
export { useGameStore }