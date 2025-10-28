import React, { useEffect, useState } from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { ConfigProvider, theme } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { LoadingOutlined } from '@ant-design/icons'
import WorldSelectionPage from './pages/WorldSelectionPage'
import GamePage from './pages/GamePage'
import useGameStore from './store'
import { generatePlayerId, StorageManager } from './utils'
import './styles/global.css'

const { defaultAlgorithm, darkAlgorithm } = theme

const App: React.FC = () => {
  const [isInitialized, setIsInitialized] = useState(false)
  const gameState = useGameStore((state) => state.gameState)
  const setPlayerId = useGameStore((state) => state.setPlayerId)

  useEffect(() => {
    // 初始化游戏
    const initializeGame = () => {
      // 检查是否有保存的玩家ID
      const savedPlayerId = StorageManager.getItem<string>('player_id')
      
      if (savedPlayerId) {
        setPlayerId(savedPlayerId)
      } else {
        // 生成新的玩家ID
        const newPlayerId = generatePlayerId()
        setPlayerId(newPlayerId)
        StorageManager.setItem('player_id', newPlayerId)
      }
      
      setIsInitialized(true)
    }

    initializeGame()
  }, [setPlayerId])

  // 应用主题配置
  const themeConfig = {
    algorithm: gameState.playerAttributes.魄力 > 15 ? darkAlgorithm : defaultAlgorithm,
    token: {
      colorPrimary: '#1890ff',
      colorSuccess: '#52c41a',
      colorWarning: '#faad14',
      colorError: '#ff4d4f',
      borderRadius: 8,
      wireframe: false,
    },
    components: {
      Button: {
        borderRadius: 6,
        controlHeight: 32,
      },
      Card: {
        borderRadius: 8,
      },
      Modal: {
        borderRadius: 8,
      }
    }
  }

  if (!isInitialized) {
    return (
      <div className="app-loading">
        <LoadingOutlined style={{ fontSize: 48, color: '#1890ff' }} spin />
        <p>正在初始化游戏...</p>
      </div>
    )
  }

  return (
    <ConfigProvider 
      locale={zhCN} 
      theme={themeConfig}
      renderEmpty={() => (
        <div className="empty-state">
          <p>暂无内容</p>
        </div>
      )}
    >
      <Router>
        <div className="app">
          <Routes>
            <Route 
              path="/" 
              element={
                gameState.currentWorldId ? (
                  <Navigate to={`/game/${gameState.currentWorldId}`} replace />
                ) : (
                  <WorldSelectionPage />
                )
              } 
            />
            <Route path="/world-selection" element={<WorldSelectionPage />} />
            <Route path="/game/:worldId" element={<GamePage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </Router>
    </ConfigProvider>
  )
}

export default App