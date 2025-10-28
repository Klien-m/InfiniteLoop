import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { 
  Card, 
  Button, 
  Typography, 
  Row, 
  Col, 
  Space, 
  Divider, 
  Tag,
  Spin,
  message 
} from 'antd'
import { ArrowRightOutlined, StarOutlined } from '@ant-design/icons'
import useGameStore from '../store'
import NarrativeEngine, { createNarrativeEngine } from '../services/narrativeEngine'
import { WorldOption } from '../types'
import { AnimationUtils } from '../utils'

const { Title, Paragraph, Text } = Typography

const WorldSelectionPage: React.FC = () => {
  const navigate = useNavigate()
  const [isLoading, setIsLoading] = useState(false)
  const [isGenerating, setIsGenerating] = useState(false)
  
  const gameState = useGameStore((state) => state.gameState)
  const availableWorlds = useGameStore((state) => state.availableWorlds)
  const selectedWorld = useGameStore((state) => state.selectedWorld)
  const setAvailableWorlds = useGameStore((state) => state.setAvailableWorlds)
  const setSelectedWorld = useGameStore((state) => state.setSelectedWorld)
  const setCurrentWorld = useGameStore((state) => state.setCurrentWorld)
  const unlockWorld = useGameStore((state) => state.unlockWorld)

  useEffect(() => {
    // 如果已经有可用世界，直接使用
    if (availableWorlds.length > 0) {
      return
    }

    generateWorldRecommendations()
  }, [])

  const generateWorldRecommendations = async () => {
    setIsGenerating(true)
    try {
      const engine = createNarrativeEngine()
      const result = await engine.generateWorldSelection(gameState.playerId)
      
      setAvailableWorlds(result.worldOptions)
      message.success('世界推荐生成完成！')
    } catch (error) {
      console.error('生成世界推荐失败:', error)
      message.error('生成世界推荐失败，请稍后重试')
      
      // 使用默认世界选项
      const defaultWorlds: WorldOption[] = [
        {
          worldId: '赛博蓬莱',
          name: '赛博蓬莱',
          description: '在未来都市，你的意识可以上传至仙境网络，在代码与灵气的冲突中寻求长生。',
          requiredAttributes: { insight: 8, persuasion: 6, 魄力: 10 }
        },
        {
          worldId: '法典迷城',
          name: '法典迷城',
          description: '在一个法律条文能直接改写物理规则的世界，你是一名"律法侠盗"，利用法律的漏洞施展超能力。',
          requiredAttributes: { insight: 12, persuasion: 10, 魄力: 8 }
        },
        {
          worldId: '衰败王座',
          name: '衰败王座',
          description: '你是某个被遗忘之神的最后一位祭司，在一个无神的世界里，收集信仰，重现神迹。',
          requiredAttributes: { insight: 6, persuasion: 8, 魄力: 12 }
        }
      ]
      
      setAvailableWorlds(defaultWorlds)
    } finally {
      setIsGenerating(false)
    }
  }

  const handleWorldSelect = (world: WorldOption) => {
    setSelectedWorld(world)
    
    // 检查属性要求
    const playerAttrs = gameState.playerAttributes
    const requiredAttrs = world.requiredAttributes
    
    let canEnter = true
    const missingAttrs: string[] = []
    
    for (const [attr, required] of Object.entries(requiredAttrs)) {
      if (playerAttrs[attr as keyof typeof playerAttrs] < required) {
        canEnter = false
        missingAttrs.push(`${attr}需要${required}`)
      }
    }
    
    if (!canEnter) {
      message.warning(`属性不足：${missingAttrs.join('， ')}`)
      return
    }
    
    // 解锁并进入世界
    unlockWorld(world.worldId)
    setCurrentWorld(world.worldId)
    
    message.success(`欢迎来到${world.name}！`)
    navigate(`/game/${world.worldId}`)
  }

  const handleCustomWorld = () => {
    // 进入自定义世界创建页面
    navigate('/custom-world')
  }

  const renderWorldCard = (world: WorldOption) => {
    const playerAttrs = gameState.playerAttributes
    const requiredAttrs = world.requiredAttributes
    const isSelected = selectedWorld?.worldId === world.worldId
    
    const attrChecks = Object.entries(requiredAttrs).map(([attr, required]) => {
      const playerValue = playerAttrs[attr as keyof typeof playerAttrs]
      const isSufficient = playerValue >= required
      return {
        attr,
        playerValue,
        required,
        isSufficient
      }
    })

    return (
      <Card
        key={world.worldId}
        className={`world-card ${isSelected ? 'world-card-selected' : ''}`}
        hoverable
        style={{ 
          marginBottom: 16,
          border: isSelected ? '2px solid #1890ff' : undefined,
          transform: isSelected ? 'scale(1.02)' : undefined,
          transition: 'all 0.3s ease'
        }}
        cover={
          <div className="world-card-cover">
            <div className="world-card-gradient" />
            <div className="world-card-content">
              <Title level={3} style={{ color: '#fff', margin: 0 }}>
                {world.name}
              </Title>
              <Paragraph style={{ color: '#fff', opacity: 0.9 }}>
                {world.description}
              </Paragraph>
            </div>
          </div>
        }
        actions={[
          <Button
            type="primary"
            icon={<ArrowRightOutlined />}
            size="large"
            onClick={() => handleWorldSelect(world)}
            disabled={!attrChecks.every(check => check.isSufficient)}
          >
            进入世界
          </Button>
        ]}
      >
        <div className="world-attributes">
          <Title level={5}>属性要求</Title>
          <Space direction="vertical" size="small" style={{ width: '100%' }}>
            {attrChecks.map(({ attr, playerValue, required, isSufficient }) => (
              <div key={attr} className="attr-row">
                <Text strong>{attr}</Text>
                <Text 
                  className={`attr-value ${isSufficient ? 'sufficient' : 'insufficient'}`}
                >
                  {playerValue}/{required}
                </Text>
              </div>
            ))}
          </Space>
        </div>
        
        <Divider />
        
        <div className="world-tags">
          <Tag color="blue" icon={<StarOutlined />}>
            推荐指数：高
          </Tag>
          <Tag color="green">
            难度：中等
          </Tag>
          <Tag color="orange">
            风格：{world.name.includes('赛博') ? '科幻' : 
                   world.name.includes('法典') ? '奇幻' : '史诗'}
          </Tag>
        </div>
      </Card>
    )
  }

  if (isLoading) {
    return (
      <div className="loading-container">
        <Spin size="large" />
        <Text>正在加载...</Text>
      </div>
    )
  }

  return (
    <div className="world-selection-page">
      <div className="page-header">
        <Title level={1} className="page-title">
          万象之根 - 世界选择
        </Title>
        <Paragraph className="page-subtitle">
          选择你的下一个故事世界，每个世界都有独特的风格和挑战
        </Paragraph>
      </div>

      <div className="player-stats">
        <Card title="你的属性" size="small">
          <Space direction="horizontal" size="large">
            <div className="stat-item">
              <Text type="secondary">洞察力</Text>
              <Title level={4} style={{ margin: 0, color: '#1890ff' }}>
                {gameState.playerAttributes.insight}
              </Title>
            </div>
            <div className="stat-item">
              <Text type="secondary">说服力</Text>
              <Title level={4} style={{ margin: 0, color: '#52c41a' }}>
                {gameState.playerAttributes.persuasion}
              </Title>
            </div>
            <div className="stat-item">
              <Text type="secondary">魄力</Text>
              <Title level={4} style={{ margin: 0, color: '#faad14' }}>
                {gameState.playerAttributes.魄力}
              </Title>
            </div>
          </Space>
        </Card>
      </div>

      <div className="worlds-grid">
        <Row gutter={[24, 24]}>
          {isGenerating ? (
            <Col span={24} style={{ textAlign: 'center', padding: 48 }}>
              <Spin size="large" />
              <Title level={4} style={{ marginTop: 16 }}>
                正在生成世界推荐...
              </Title>
              <Paragraph>
                AI正在根据你的经历和能力推荐最适合的世界
              </Paragraph>
            </Col>
          ) : (
            availableWorlds.map(renderWorldCard)
          )}
        </Row>
      </div>

      <div className="page-actions">
        <Space>
          <Button 
            size="large" 
            onClick={generateWorldRecommendations}
            loading={isGenerating}
            disabled={isGenerating}
          >
            重新生成推荐
          </Button>
          <Button 
            size="large" 
            onClick={handleCustomWorld}
            disabled={isGenerating}
          >
            自定义世界
          </Button>
        </Space>
      </div>

      <style jsx>{`
        .world-selection-page {
          min-height: 100vh;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          padding: 24px;
        }
        
        .page-header {
          text-align: center;
          margin-bottom: 32px;
          color: white;
        }
        
        .page-title {
          color: white;
          margin-bottom: 8px;
        }
        
        .page-subtitle {
          color: rgba(255, 255, 255, 0.8);
        }
        
        .player-stats {
          margin-bottom: 32px;
        }
        
        .stat-item {
          text-align: center;
        }
        
        .world-card-cover {
          position: relative;
          height: 120px;
          overflow: hidden;
        }
        
        .world-card-gradient {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: linear-gradient(45deg, rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0.3));
        }
        
        .world-card-content {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          padding: 16px;
          display: flex;
          flex-direction: column;
          justify-content: center;
        }
        
        .world-attributes {
          padding: 16px 0;
        }
        
        .attr-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        
        .attr-value.sufficient {
          color: #52c41a;
          font-weight: bold;
        }
        
        .attr-value.insufficient {
          color: #ff4d4f;
          font-weight: bold;
        }
        
        .world-tags {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
        }
        
        .loading-container {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 200px;
        }
        
        .page-actions {
          text-align: center;
          margin-top: 32px;
        }
      `}</style>
    </div>
  )
}

export default WorldSelectionPage