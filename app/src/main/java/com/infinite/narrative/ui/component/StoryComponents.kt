package com.infinite.narrative.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        }

        isTyping = false
        onTextFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Justify,
            modifier = Modifier
                .fillMaxWidth()
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
            .fillMaxSize()
            .padding(16.dp),
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

        options.forEach { option ->
            OptionCard(
                option = option,
                onSelected = { onOptionSelected(option.optionId) }
            )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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

        worlds.forEach { world ->
            WorldCard(
                world = world,
                onSelected = { onWorldSelected(world.worldId) }
            )
        }
    }
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
                    color = Color.White
                )

                Text(
                    text = world.description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
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
