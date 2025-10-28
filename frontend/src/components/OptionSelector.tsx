import React from 'react'
import { Card, Button, Typography, Space, Divider, Tag, Tooltip } from 'antd'
import { ArrowRightOutlined, ThunderboltOutlined, EyeOutlined, HeartOutlined } from '@ant-design/icons'
import { StoryOption } from '../types'

const { Title, Text, Paragraph } = Typography

interface OptionSelectorProps {
  options: StoryOption[]
  onOptionSelect: (option: StoryOption) => void
  disabled?: boolean
}

const OptionSelector: React.FC<OptionSelectorProps> = ({
  options,
  onOptionSelect,
  disabled = false
}) => {
  const getAttributeIcon = (attr: string) => {
    switch (attr) {
      case 'insight':
        return <EyeOutlined />
      case 'persuasion':
        return <HeartOutlined />
      case '魄力':
        return <ThunderboltOutlined />
      default:
        return <ArrowRightOutlined />
    }
  }

  const getAttributeColor = (attr: string) => {
    switch (attr) {
      case 'insight':
        return '#1890ff'
      case 'persuasion':
        return '#52c41a'
      case '魄力':
        return '#faad14'
      default:
        return '#d9d9d9'
    }
  }

  const handleOptionClick = (option: StoryOption) => {
    if (disabled) return
    onOptionSelect(option)
  }

  const renderOptionCard = (option: StoryOption, index: number) => {
    const hasRequirements = option.requiredAttributes && 
      Object.keys(option.requiredAttributes).length > 0
    
    return (
      <Card
        key={option.optionId}
        className={`option-card ${disabled ? 'option-card-disabled' : ''}`}
        hoverable={!disabled}
        style={{
          marginBottom: 16,
          border: `2px solid ${disabled ? '#f5f5f5' : '#e6f7ff'}`,
          cursor: disabled ? 'not-allowed' : 'pointer',
          transition: 'all 0.3s ease',
        }}
        onMouseEnter={(e) => {
          if (!disabled) {
            e.currentTarget.style.transform = 'translateY(-2px)'
            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.1)'
          }
        }}
        onMouseLeave={(e) => {
          if (!disabled) {
            e.currentTarget.style.transform = 'translateY(0)'
            e.currentTarget.style.boxShadow = '0 2px 8px rgba(0, 0, 0, 0.05)'
          }
        }}
      >
        <div className="option-content">
          <div className="option-header">
            <Title level={5} style={{ margin: 0, color: '#1f1f1f' }}>
              {index + 1}. {option.text}
            </Title>
            {option.estimatedTimeCost && (
              <Tag color="default">
                预计耗时: {option.estimatedTimeCost}分钟
              </Tag>
            )}
          </div>

          <Paragraph 
            style={{ 
              margin: '8px 0', 
              color: '#666',
              lineHeight: 1.6
            }}
          >
            {option.description}
          </Paragraph>

          {hasRequirements && (
            <div className="option-requirements">
              <Divider style={{ margin: '12px 0' }} />
              <div className="requirements-header">
                <Text type="secondary" strong>
                  属性要求:
                </Text>
              </div>
              <Space wrap style={{ marginTop: 8 }}>
                {Object.entries(option.requiredAttributes!).map(([attr, required]) => (
                  <Tooltip 
                    key={attr} 
                    title={`需要${attr}达到${required}`}
                  >
                    <Tag 
                      color="processing" 
                      icon={getAttributeIcon(attr)}
                      style={{ 
                        color: getAttributeColor(attr),
                        borderColor: getAttributeColor(attr)
                      }}
                    >
                      {attr}: {required}
                    </Tag>
                  </Tooltip>
                ))}
              </Space>
            </div>
          )}

          {option.potentialConsequences && option.potentialConsequences.length > 0 && (
            <div className="option-consequences">
              <Divider style={{ margin: '12px 0' }} />
              <Text type="secondary" strong>
                可能后果:
              </Text>
              <Space direction="vertical" size="small" style={{ marginTop: 8, width: '100%' }}>
                {option.potentialConsequences.map((consequence, idx) => (
                  <Tag 
                    key={idx} 
                    color="warning" 
                    style={{ 
                      display: 'block',
                      textAlign: 'left',
                      border: '1px solid #ffd591'
                    }}
                  >
                    • {consequence}
                  </Tag>
                ))}
              </Space>
            </div>
          )}
        </div>

        <Button
          type="primary"
          icon={<ArrowRightOutlined />}
          size="large"
          onClick={() => handleOptionClick(option)}
          disabled={disabled}
          style={{
            width: '100%',
            marginTop: 16,
            height: 40,
            fontWeight: 500
          }}
        >
          选择此项
        </Button>
      </Card>
    )
  }

  return (
    <div className="option-selector">
      <div className="options-header">
        <Title level={4} style={{ margin: 0, color: '#1f1f1f' }}>
          请选择下一步行动
        </Title>
        <Text type="secondary">
          每个选择都会影响故事的发展方向
        </Text>
      </div>

      <Divider style={{ margin: '16px 0' }} />

      <div className="options-grid">
        {options.map((option, index) => renderOptionCard(option, index))}
      </div>

      <style>{`
        .option-selector {
          max-width: 800px;
          margin: 0 auto;
          padding: 24px 0;
        }

        .options-header {
          text-align: center;
          margin-bottom: 24px;
        }

        .options-grid {
          display: flex;
          flex-direction: column;
          gap: 16px;
        }

        .option-card {
          position: relative;
          overflow: hidden;
        }

        .option-card-disabled {
          opacity: 0.6;
        }

        .option-content {
          flex: 1;
        }

        .option-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
        }

        .option-requirements {
          background: #f9f9f9;
          padding: 12px;
          border-radius: 6px;
          border-left: 3px solid #1890ff;
        }

        .requirements-header {
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .option-consequences {
          background: #fff7e6;
          padding: 12px;
          border-radius: 6px;
          border-left: 3px solid #faad14;
        }

        @media (max-width: 768px) {
          .option-selector {
            padding: 16px 0;
          }

          .options-grid {
            gap: 12px;
          }

          .option-card {
            border-width: 1px;
          }
        }
      `}</style>
    </div>
  )
}

export default OptionSelector