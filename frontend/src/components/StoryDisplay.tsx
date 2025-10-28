import React, { useEffect, useState } from 'react'
import { Card, Typography, Tag, Divider, Space } from 'antd'
import { MessageOutlined, StarOutlined } from '@ant-design/icons'
import { NarrativeResponse } from '../types'
import { AnimationUtils } from '../utils'

const { Paragraph, Text } = Typography

interface StoryDisplayProps {
  story: NarrativeResponse
  isLast?: boolean
  onTypingStart?: () => void
  onTypingEnd?: () => void
}

const StoryDisplay: React.FC<StoryDisplayProps> = ({
  story,
  isLast = false,
  onTypingStart,
  onTypingEnd
}) => {
  const [displayedText, setDisplayedText] = useState('')
  const [isTyping, setIsTyping] = useState(false)

  useEffect(() => {
    if (isLast) {
      startTypingAnimation()
    } else {
      setDisplayedText(story.storySegment)
    }
  }, [story.storySegment, isLast])

  const startTypingAnimation = async () => {
    if (onTypingStart) onTypingStart()
    setIsTyping(true)
    
    await AnimationUtils.typeWriter(
      story.storySegment,
      (text) => setDisplayedText(text),
      30
    )
    
    setIsTyping(false)
    if (onTypingEnd) onTypingEnd()
  }

  const renderConfidenceTag = () => {
    const confidence = story.confidenceScore
    let color: string
    let text: string

    if (confidence >= 0.8) {
      color = 'green'
      text = '高置信度'
    } else if (confidence >= 0.6) {
      color = 'blue'
      text = '中等置信度'
    } else {
      color = 'orange'
      text = '低置信度'
    }

    return (
      <Tag color={color}>
        <StarOutlined /> {text} ({Math.round(confidence * 100)}%)
      </Tag>
    )
  }

  return (
    <Card 
      className={`story-display ${isLast ? 'story-display-last' : ''}`}
      style={{ 
        marginBottom: 16,
        borderLeft: isLast ? '4px solid #1890ff' : undefined,
        opacity: isTyping ? 0.7 : 1,
        transition: 'opacity 0.3s ease'
      }}
    >
      <div className="story-header">
        <Space>
          <Tag icon={<MessageOutlined />} color="processing">
            故事片段
          </Tag>
          {renderConfidenceTag()}
          {isLast && (
            <Tag color="gold">
              最新
            </Tag>
          )}
        </Space>
      </div>

      <Divider style={{ margin: '12px 0' }} />

      <div className="story-content">
        <Paragraph 
          className="story-text"
          style={{ 
            whiteSpace: 'pre-wrap',
            lineHeight: 1.8,
            fontSize: '16px'
          }}
        >
          {displayedText}
        </Paragraph>
      </div>

      {story.newWorldState && (
        <div className="world-state-update">
          <Divider />
          <div className="state-changes">
            <Text type="secondary" strong>世界状态更新:</Text>
            <Space wrap style={{ marginTop: 8 }}>
              {story.newWorldState.unlockedLocations.length > 0 && (
                <Tag color="blue">
                  解锁地点: {story.newWorldState.unlockedLocations.join(', ')}
                </Tag>
              )}
              {Object.keys(story.newWorldState.corePuzzles).length > 0 && (
                <Tag color="purple">
                  新谜题: {Object.keys(story.newWorldState.corePuzzles).join(', ')}
                </Tag>
              )}
              {Object.keys(story.newWorldState.keyCharacters).length > 0 && (
                <Tag color="cyan">
                  新角色: {Object.keys(story.newWorldState.keyCharacters).join(', ')}
                </Tag>
              )}
            </Space>
          </div>
        </div>
      )}

      <style>{`
        .story-display {
          animation: fadeInUp 0.5s ease-out;
        }

        .story-display-last {
          animation: pulse 0.5s ease-in-out;
        }

        .story-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .story-content {
          margin: 16px 0;
        }

        .story-text {
          font-family: 'Georgia', 'Times New Roman', serif;
          color: #333;
        }

        .world-state-update {
          margin-top: 16px;
          padding-top: 12px;
          border-top: 1px dashed #d9d9d9;
        }

        .state-changes {
          display: flex;
          flex-direction: column;
          gap: 8px;
        }

        @keyframes fadeInUp {
          from {
            opacity: 0;
            transform: translate3d(0, 40px, 0);
          }
          to {
            opacity: 1;
            transform: translate3d(0, 0, 0);
          }
        }

        @keyframes pulse {
          from {
            transform: scale3d(1, 1, 1);
          }
          50% {
            transform: scale3d(1.02, 1.02, 1.02);
          }
          to {
            transform: scale3d(1, 1, 1);
          }
        }
      `}</style>
    </Card>
  )
}

export default StoryDisplay