package com.infinite.narrative.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infinite.narrative.ai.model.StoryOption
import com.infinite.narrative.engine.NarrativeEngine
import kotlinx.coroutines.delay

/**
 * 故事阅读界面组件
 */
@Composable
fun StoryReader(
    storyText: String,
    onTextFinished: () -> Unit = {},
    onProgressChanged: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var displayText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(true) }

    LaunchedEffect(storyText) {
        displayText = ""
        isTyping = true

        for (char in storyText) {
            displayText += char
            delay(50) // 打字机效果速度
            // 计算并更新进度
            val progress = displayText.length.toFloat() / storyText.length.toFloat()
            onProgressChanged?.invoke(progress)
        }

        isTyping = false
        onProgressChanged?.invoke(1f) // 完成时设置为100%
        onTextFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // 为长篇故事添加滚动功能
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayText,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // 添加滚动
                    .padding(16.dp)
            )

            // 跳过按钮
            if (isTyping) {
                SkipButton(
                    onClick = {
                        displayText = storyText
                        isTyping = false
                        onTextFinished()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}

/**
 * 跳过按钮
 */
@Composable
fun SkipButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text("跳过")
    }
}

/**
 * 选项选择界面组件
 */
@Composable
fun OptionSelector(
    options: List<StoryOption>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "请选择你的行动：",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // 添加垂直滚动以处理超长内容
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // 占据剩余空间
                .verticalScroll(rememberScrollState()), // 添加滚动
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEach { option ->
                OptionCard(
                    option = option,
                    onSelected = { onOptionSelected(option.optionId) }
                )
            }
        }
    }
}

/**
 * 选项卡片组件
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OptionCard(
    option: StoryOption,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelected() }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = option.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "选择",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = option.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // 显示潜在后果
                    option.potentialConsequences?.let { consequences ->
                        if (consequences.isNotEmpty()) {
                            Text(
                                text = "可能后果：",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Column(
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            ) {
                                consequences.forEach { consequence ->
                                    Text(
                                        text = "• $consequence",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }

                    // 显示预估时间成本
                    option.estimatedTimeCost?.let { timeCost ->
                        Text(
                            text = "预估耗时：${timeCost}分钟",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    option.requiredAttributes?.let { attributes ->
                        if (attributes.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                attributes.forEach { (attr, value) ->
                                    AttributeChip(attr, value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 点击展开/收起
    LaunchedEffect(option.optionId) {
        delay(500) // 短暂延迟后自动展开
        isExpanded = true
    }
}

/**
 * 属性需求芯片
 */
@Composable
fun AttributeChip(
    attributeName: String,
    requiredValue: Int,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = {},
        modifier = modifier,
        label = {
            Text(
                text = "$attributeName $requiredValue",
                fontSize = 12.sp
            )
        })
}

/**
 * 世界选择界面
 */
@Composable
fun WorldSelector(
    worlds: List<NarrativeEngine.WorldOption>,
    onWorldSelected: (String) -> Unit,
    recommendation: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        recommendation.takeIf { it.isNotEmpty() }?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text(
            text = "选择你的下一个世界：",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        if (worlds.isEmpty()) {
            Text(
                text = "正在加载世界选项...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        } else {
            // 为世界卡片添加滚动功能
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                worlds.forEach { world ->
                    WorldCard(
                        world = world,
                        onSelected = { onWorldSelected(world.worldId) },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun WorldCardPreview() {
    WorldCard(
        NarrativeEngine.WorldOption(
            worldId = "decaying_throne",
            name = "开始你的叙事之旅",
            description = "选择一个世界开始你的冒险。",
            requiredAttributes = emptyMap()
        ),
        {},
    )
}

/**
 * 世界卡片
 */
@Composable
fun WorldCard(
    world: NarrativeEngine.WorldOption,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (world.worldId) {
                "cyber_penglai" -> Color(0xFF1E3A8A) // 赛博蓬莱 - 深蓝色
                "law_maze" -> Color(0xFF7C2D12)     // 法典迷城 - 深橙色
                "decaying_throne" -> Color(0xFF4B5563) // 衰败王座 - 灰色
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onSelected() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = world.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = world.description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                world.requiredAttributes.takeIf { it.isNotEmpty() }?.let { attributes ->
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        attributes.forEach { (attr, value) ->
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = "$attr $value",
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

}
