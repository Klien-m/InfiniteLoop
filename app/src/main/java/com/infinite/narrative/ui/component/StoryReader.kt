package com.infinite.narrative.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
