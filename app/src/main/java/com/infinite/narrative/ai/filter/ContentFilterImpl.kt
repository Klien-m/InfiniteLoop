package com.infinite.narrative.ai.filter

import com.infinite.narrative.ai.ContentFilter
import com.infinite.narrative.ai.ContentModification
import com.infinite.narrative.ai.FilteredContent
import com.infinite.narrative.ai.ModificationType
import com.infinite.narrative.ai.RiskCategory
import com.infinite.narrative.ai.RiskLevel
import com.infinite.narrative.ai.SafetyResult

/**
 * 内容过滤器实现 - 确保AI生成内容的安全性
 */
class ContentFilterImpl : ContentFilter {

    // 敏感词库
    private val sensitiveWords = setOf(
        // 暴力相关
        "屠杀", "谋杀", "残杀", "虐杀", "血腥", "暴力", "残忍", "酷刑",
        // 仇恨相关
        "歧视", "偏见", "仇恨", "排外", "种族主义", "性别歧视",
        // 自残相关
        "自杀", "自残", "轻生", "结束生命", "跳楼", "割腕",
        // 性相关内容
        "色情", "情色", "裸露", "性行为", "性暗示", "淫秽",
        // 骚扰相关
        "骚扰", "跟踪", "威胁", "恐吓", "辱骂", "人身攻击",
        // 虚假信息
        "谣言", "假新闻", "虚假信息", "误导", "欺骗"
    )

    // 需要替换的敏感词映射
    private val wordReplacements = mapOf(
        "屠杀" to "激烈冲突",
        "谋杀" to "意外事件",
        "血腥" to "紧张场面",
        "色情" to "艺术表现",
        "自杀" to "心理困扰"
    )

    override fun filterContent(content: String): FilteredContent {
        var filteredContent = content
        val modifications = mutableListOf<ContentModification>()

        // 检查并替换敏感词
        for (word in sensitiveWords) {
            if (content.contains(word)) {
                val replacement = wordReplacements[word] ?: "[已过滤]"
                val originalText = word
                val modifiedText = replacement

                filteredContent = filteredContent.replace(word, replacement)

                modifications.add(
                    ContentModification(
                        type = ModificationType.REPLACEMENT,
                        originalText = originalText,
                        modifiedText = modifiedText,
                        reason = "内容安全过滤"
                    )
                )
            }
        }

        return FilteredContent(
            original = content,
            filtered = filteredContent,
            isModified = modifications.isNotEmpty(),
            modifications = modifications
        )
    }

    override fun checkSafety(content: String): SafetyResult {
        var riskLevel = RiskLevel.SAFE
        val riskCategories = mutableListOf<RiskCategory>()
        var confidence = 1.0f

        // 检查各类风险
        checkViolenceRisk(content, riskCategories)
        checkHateRisk(content, riskCategories)
        checkSelfHarmRisk(content, riskCategories)
        checkSexualRisk(content, riskCategories)
        checkHarassmentRisk(content, riskCategories)
        checkMisinformationRisk(content, riskCategories)

        // 确定风险等级
        when {
            riskCategories.isEmpty() -> {
                riskLevel = RiskLevel.SAFE
                confidence = 0.95f
            }

            riskCategories.size == 1 && riskCategories[0] == RiskCategory.HARASSMENT -> {
                riskLevel = RiskLevel.LOW_RISK
                confidence = 0.85f
            }

            riskCategories.size <= 2 -> {
                riskLevel = RiskLevel.MEDIUM_RISK
                confidence = 0.7f
            }

            else -> {
                riskLevel = RiskLevel.HIGH_RISK
                confidence = 0.5f
            }
        }

        return SafetyResult(
            isSafe = riskLevel == RiskLevel.SAFE,
            riskLevel = riskLevel,
            riskCategories = riskCategories,
            confidence = confidence
        )
    }

    private fun checkViolenceRisk(content: String, riskCategories: MutableList<RiskCategory>) {
        val violenceKeywords = listOf("屠杀", "谋杀", "暴力", "血腥", "残忍", "酷刑")
        if (violenceKeywords.any { content.contains(it) }) {
            riskCategories.add(RiskCategory.VIOLENCE)
        }
    }

    private fun checkHateRisk(content: String, riskCategories: MutableList<RiskCategory>) {
        val hateKeywords = listOf("歧视", "偏见", "仇恨", "种族主义", "性别歧视")
        if (hateKeywords.any { content.contains(it) }) {
            riskCategories.add(RiskCategory.HATE)
        }
    }

    private fun checkSelfHarmRisk(content: String, riskCategories: MutableList<RiskCategory>) {
        val selfHarmKeywords = listOf("自杀", "自残", "轻生", "结束生命")
        if (selfHarmKeywords.any { content.contains(it) }) {
            riskCategories.add(RiskCategory.SELF_HARM)
        }
    }

    private fun checkSexualRisk(content: String, riskCategories: MutableList<RiskCategory>) {
        val sexualKeywords = listOf("色情", "情色", "裸露", "性行为")
        if (sexualKeywords.any { content.contains(it) }) {
            riskCategories.add(RiskCategory.SEXUAL)
        }
    }

    private fun checkHarassmentRisk(content: String, riskCategories: MutableList<RiskCategory>) {
        val harassmentKeywords = listOf("骚扰", "跟踪", "威胁", "恐吓", "辱骂")
        if (harassmentKeywords.any { content.contains(it) }) {
            riskCategories.add(RiskCategory.HARASSMENT)
        }
    }

    private fun checkMisinformationRisk(
        content: String,
        riskCategories: MutableList<RiskCategory>
    ) {
        val misinformationKeywords = listOf("谣言", "假新闻", "虚假信息", "误导")
        if (misinformationKeywords.any { content.contains(it) }) {
            riskCategories.add(RiskCategory.MISINFORMATION)
        }
    }
}
