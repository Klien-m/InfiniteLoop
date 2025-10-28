import { FilteredContent, SafetyResult, RiskLevel, RiskCategory, ContentModification } from '@/types'

class ContentFilter {
  private readonly SAFE_THRESHOLD = 0.8
  private readonly RISK_KEYWORDS: Record<RiskCategory, string[]> = {
    VIOLENCE: ['杀', '死', '血', '暴力', '残忍', '血腥', '谋杀', '屠杀'],
    HATE: ['歧视', '仇恨', '种族', '偏见', '侮辱', '攻击'],
    SELF_HARM: ['自杀', '自残', '伤害', '结束生命'],
    SEXUAL: ['色情', '性', '裸露', '淫秽'],
    HARASSMENT: ['骚扰', '欺负', '霸凌', '威胁'],
    MISINFORMATION: ['假', '错误', '误导', '谣言']
  }

  /**
   * 过滤生成的内容
   */
  filterContent(content: string): FilteredContent {
    const safetyResult = this.checkSafety(content)
    
    if (safetyResult.isSafe) {
      return {
        original: content,
        filtered: content,
        isModified: false,
        modifications: []
      }
    }

    const filteredContent = this.applyContentFiltering(content, safetyResult)
    const modifications = this.generateModificationLog(content, filteredContent, safetyResult)

    return {
      original: content,
      filtered: filteredContent,
      isModified: content !== filteredContent,
      modifications
    }
  }

  /**
   * 检查内容安全性
   */
  checkSafety(content: string): SafetyResult {
    const riskCategories: RiskCategory[] = []
    let maxRiskScore = 0

    // 检查各类风险关键词
    for (const [category, keywords] of Object.entries(this.RISK_KEYWORDS)) {
      const riskCategory = category as RiskCategory
      const riskScore = this.calculateRiskScore(content, keywords)
      
      if (riskScore > 0.3) {
        riskCategories.push(riskCategory)
        maxRiskScore = Math.max(maxRiskScore, riskScore)
      }
    }

    // 检查敏感话题
    const sensitiveTopicsScore = this.checkSensitiveTopics(content)
    maxRiskScore = Math.max(maxRiskScore, sensitiveTopicsScore)

    const riskLevel = this.determineRiskLevel(maxRiskScore)
    const isSafe = riskLevel === 'SAFE' || riskLevel === 'LOW_RISK'
    const confidence = 1 - maxRiskScore

    return {
      isSafe,
      riskLevel,
      riskCategories,
      confidence
    }
  }

  /**
   * 计算风险分数
   */
  private calculateRiskScore(content: string, keywords: string[]): number {
    let score = 0
    const contentLower = content.toLowerCase()
    
    for (const keyword of keywords) {
      const keywordLower = keyword.toLowerCase()
      const occurrences = (contentLower.match(new RegExp(keywordLower, 'g')) || []).length
      score += occurrences * 0.1
    }
    
    return Math.min(score, 1.0)
  }

  /**
   * 检查敏感话题
   */
  private checkSensitiveTopics(content: string): number {
    const sensitivePatterns = [
      /政治.*敏感/i,
      /宗教.*极端/i,
      /民族.*歧视/i,
      /国家.*机密/i,
      /未成年人.*保护/i
    ]

    let score = 0
    for (const pattern of sensitivePatterns) {
      if (pattern.test(content)) {
        score += 0.3
      }
    }

    return Math.min(score, 1.0)
  }

  /**
   * 确定风险等级
   */
  private determineRiskLevel(riskScore: number): RiskLevel {
    if (riskScore < 0.2) return 'SAFE'
    if (riskScore < 0.4) return 'LOW_RISK'
    if (riskScore < 0.7) return 'MEDIUM_RISK'
    return 'HIGH_RISK'
  }

  /**
   * 应用内容过滤
   */
  private applyContentFiltering(content: string, safetyResult: SafetyResult): string {
    let filteredContent = content

    // 根据风险等级应用不同的过滤策略
    switch (safetyResult.riskLevel) {
      case 'HIGH_RISK':
        filteredContent = this.replaceHighRiskContent(content, safetyResult.riskCategories)
        break
      case 'MEDIUM_RISK':
        filteredContent = this.censorMediumRiskContent(content, safetyResult.riskCategories)
        break
      case 'LOW_RISK':
        filteredContent = this.moderateLowRiskContent(content, safetyResult.riskCategories)
        break
    }

    return filteredContent
  }

  /**
   * 替换高风险内容
   */
  private replaceHighRiskContent(content: string, riskCategories: RiskCategory[]): string {
    let filtered = content
    
    // 替换暴力内容
    if (riskCategories.includes('VIOLENCE')) {
      filtered = filtered.replace(/(杀|死|血|暴力)/g, '***')
    }
    
    // 替换仇恨内容
    if (riskCategories.includes('HATE')) {
      filtered = filtered.replace(/(歧视|仇恨|偏见)/g, '***')
    }
    
    // 替换自残内容
    if (riskCategories.includes('SELF_HARM')) {
      filtered = filtered.replace(/(自杀|自残|伤害)/g, '***')
    }

    return filtered
  }

  /**
   * 屏蔽中等风险内容
   */
  private censorMediumRiskContent(content: string, riskCategories: RiskCategory[]): string {
    let filtered = content
    
    // 屏蔽敏感词汇
    if (riskCategories.includes('SEXUAL')) {
      filtered = filtered.replace(/(色情|性)/g, '***')
    }
    
    // 屏蔽骚扰内容
    if (riskCategories.includes('HARASSMENT')) {
      filtered = filtered.replace(/(骚扰|欺负|威胁)/g, '***')
    }

    return filtered
  }

  /**
   * 调整低风险内容
   */
  private moderateLowRiskContent(content: string, riskCategories: RiskCategory[]): string {
    // 对于低风险，通常只需要轻微调整语气
    return content
  }

  /**
   * 生成修改日志
   */
  private generateModificationLog(original: string, filtered: string, safetyResult: SafetyResult): ContentModification[] {
    if (original === filtered) {
      return []
    }

    return [{
      type: 'CENSORING',
      originalText: original,
      modifiedText: filtered,
      reason: `检测到${safetyResult.riskCategories.join(', ')}风险`
    }]
  }
}

export default ContentFilter