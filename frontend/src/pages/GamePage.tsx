import React, { useEffect, useRef, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { 
  Layout, 
  Card, 
  Button, 
  Typography, 
  Space, 
  Divider, 
  Tag, 
  Spin, 
  message,
  FloatButton,
  Drawer
} from 'antd'
import { ArrowLeftOutlined, ReloadOutlined, UserOutlined } from '@ant-design/icons'
import StoryDisplay from '../components/StoryDisplay'
import OptionSelector from '../components/OptionSelector'
import useGameStore from '../store'
import { createNarrativeEngine } from '../services/narrativeEngine'
import { ErrorHandler, AnimationUtils } from '../utils'
import '../styles/game.css'

const { Header, Content, Sider } = Layout
const { Title, Text, Paragraph } = Typography

const GamePage: React.FC = () => {
  const { worldId } = useParams<{ worldId: string }>()
  const navigate = useNavigate()
  const [isLoading, setIsLoading] = useState(false)
  const [isTyping, setIsTyping] = useState(false)
  const [drawerVisible, setDrawerVisible] = useState(false)
  
  const contentRef = useRef<HTMLDivElement>(null)
  
  const gameState = useGameStore((state) => state.gameState)
  const storyHistory = useGameStore((state) => state.gameState.storyHistory)
  const isTypingState = useGameStore((state) => state.isTyping)
  const setIsTypingState = useGameStore((state) => state.setIsTyping)
  const addStoryToHistory = useGameStore((state) => state.addStoryToHistory)
  const setCurrentWorld = useGameStore((state) => state.setCurrentWorld)

  useEffect(() => {
    if (!worldId) {
      navigate('/')
      return
    }

    setCurrentWorld(worldId)
    
    // 如果没有故事历史，生成初始故事
    if (storyHistory.length === 0) {
      generateInitialStory().catch(console.error)
    }
  }, [worldId])

  useEffect(() => {
    setIsTypingState(isTyping)
  }, [isTyping, setIsTypingState])

  const generateInitialStory = async () => {
    if (!worldId) return

    setIsLoading(true)
    try {
      const engine = createNarrativeEngine()
      const result = await engine.generateStory(gameState.playerId, worldId)
      
      addStoryToHistory({
        storySegment: result.storySegment,
        generatedOptions: result.options,
        newWorldState: result.worldState,
        confidenceScore: result.confidenceScore
      })

      message.success('故事生成成功！')
    } catch (error) {
      ErrorHandler.handle(error, '生成初始故事')
    } finally {
      setIsLoading(false)
    }
  }

  const handleOptionSelect = async (option: any) => {
    if (!worldId) return

    setIsLoading(true)
    setIsTyping(true)
    
    try {
      const engine = createNarrativeEngine()
      const result = await engine.generateStory(gameState.playerId, worldId, option.optionId)
      
      addStoryToHistory({
        storySegment: result.storySegment,
        generatedOptions: result.options,
        newWorldState: result.worldState,
        confidenceScore: result.confidenceScore
      })

      // 滚动到底部
      setTimeout(() => {
        contentRef.current?.scrollIntoView({ behavior: 'smooth' })
      }, 100)

      message.success('故事继续中...')
    } catch (error) {
      ErrorHandler.handle(error, '生成故事')
    } finally {
      setIsLoading(false)
      setIsTyping(false)
    }
  }

  const handleBackToWorldSelection = () => {
    navigate('/')
  }

  const handleReload = () => {
    generateInitialStory()
  }

  const scrollToBottom = () => {
    contentRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  const currentStory = storyHistory[storyHistory.length - 1]
  const currentOptions = currentStory?.generatedOptions || []

  return (
    <Layout className="game-layout">
      <Header className="game-header">
        <div className="header-content">
          <Space>
            <Button 
              type="text" 
              icon={<ArrowLeftOutlined />} 
              onClick={handleBackToWorldSelection}
            >
              返回世界选择
            </Button>
            <Button 
              type="text" 
              icon={<ReloadOutlined />} 
              onClick={handleReload}
              loading={isLoading}
            >
              重新开始
            </Button>
          </Space>
          
          <div className="world-info">
            <Title level={4} style={{ color: 'white', margin: 0 }}>
              {worldId}
            </Title>
            <Text style={{ color: 'rgba(255, 255, 255, 0.8)' }}>
              进度: {storyHistory.length} 个故事片段
            </Text>
          </div>

          <Space>
            <Button 
              type="text" 
              icon={<UserOutlined />} 
              onClick={() => setDrawerVisible(true)}
            >
              角色信息
            </Button>
          </Space>
        </div>
      </Header>

      <Layout>
        <Content className="game-content">
          <div className="story-container">
            {isLoading && (
              <div className="loading-overlay">
                <Spin size="large" />
                <Text>AI正在编织故事...</Text>
              </div>
            )}

            {storyHistory.map((story, index) => (
              <StoryDisplay
                key={index}
                story={story}
                isLast={index === storyHistory.length - 1}
                onTypingStart={() => setIsTyping(true)}
                onTypingEnd={() => setIsTyping(false)}
              />
            ))}

            {currentOptions.length > 0 && !isLoading && (
              <OptionSelector
                options={currentOptions}
                onOptionSelect={handleOptionSelect}
                disabled={isTyping}
              />
            )}

            <div ref={contentRef} />
          </div>
        </Content>

        <Sider 
          width={300} 
          className="game-sidebar"
          collapsedWidth={0}
          trigger={null}
        >
          <div className="sidebar-content">
            <Title level={5}>世界状态</Title>
            <Divider />
            
            {gameState.worldState ? (
              <div className="world-state-info">
                <Text>已解锁地点: {gameState.worldState.unlockedLocations.join(', ')}</Text>
                <Text>核心谜题: {Object.keys(gameState.worldState.corePuzzles).join(', ')}</Text>
                <Text>关键人物: {Object.keys(gameState.worldState.keyCharacters).join(', ')}</Text>
              </div>
            ) : (
              <Text type="secondary">暂无世界状态信息</Text>
            )}
            
            <Divider />
            
            <Title level={5}>玩家属性</Title>
            <div className="player-attributes">
              <div className="attr-item">
                <Text type="secondary">洞察力</Text>
                <Text strong>{gameState.playerAttributes.insight}</Text>
              </div>
              <div className="attr-item">
                <Text type="secondary">说服力</Text>
                <Text strong>{gameState.playerAttributes.persuasion}</Text>
              </div>
              <div className="attr-item">
                <Text type="secondary">魄力</Text>
                <Text strong>{gameState.playerAttributes.魄力}</Text>
              </div>
            </div>

            <Divider />
            
            <Title level={5}>背包</Title>
            <div className="inventory">
              {gameState.inventory.length > 0 ? (
                gameState.inventory.map((item, index) => (
                  <Tag key={index} color="blue">{item}</Tag>
                ))
              ) : (
                <Text type="secondary">暂无物品</Text>
              )}
            </div>
          </div>
        </Sider>
      </Layout>

      <FloatButton
        icon={<ArrowLeftOutlined />}
        tooltip="返回顶部"
        onClick={scrollToBottom}
        style={{ right: 24, bottom: 24 }}
      />

      <Drawer
        title="角色信息"
        placement="right"
        onClose={() => setDrawerVisible(false)}
        open={drawerVisible}
        width={400}
      >
        <div className="character-sheet">
          <Title level={4}>角色属性</Title>
          <div className="attribute-grid">
            <div className="attr-card">
              <Text type="secondary">洞察力</Text>
              <Title level={2} style={{ margin: 0, color: '#1890ff' }}>
                {gameState.playerAttributes.insight}
              </Title>
              <Text type="secondary">发现隐藏选项和细节</Text>
            </div>
            <div className="attr-card">
              <Text type="secondary">说服力</Text>
              <Title level={2} style={{ margin: 0, color: '#52c41a' }}>
                {gameState.playerAttributes.persuasion}
              </Title>
              <Text type="secondary">语言说服能力</Text>
            </div>
            <div className="attr-card">
              <Text type="secondary">魄力</Text>
              <Title level={2} style={{ margin: 0, color: '#faad14' }}>
                {gameState.playerAttributes.魄力}
              </Title>
              <Text type="secondary">物理对抗能力</Text>
            </div>
          </div>

          <Divider />

          <Title level={4}>游戏进度</Title>
          <div className="progress-info">
            <Text>当前世界: {worldId}</Text>
            <Text>故事片段: {storyHistory.length} 个</Text>
            <Text>已解锁世界: {gameState.unlockedWorlds.join(', ')}</Text>
          </div>

          <Divider />

          <Title level={4}>背包物品</Title>
          <div className="inventory-list">
            {gameState.inventory.length > 0 ? (
              gameState.inventory.map((item, index) => (
                <Tag key={index} color="processing" style={{ margin: 4 }}>
                  {item}
                </Tag>
              ))
            ) : (
              <Text type="secondary">暂无物品</Text>
            )}
          </div>
        </div>
      </Drawer>
    </Layout>
  )
}

export default GamePage