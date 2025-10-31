package com.infinite.narrative.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 带有百分比文本的进度条组件
 * 在 LinearProgressIndicator 中间显示进度百分比
 */
@Composable
fun LinearProgressIndicatorWithText(
    progress: Float,
    modifier: Modifier = Modifier,
    text: String = "${(progress * 100).toInt()}%"
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 背景进度条
        LinearProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
        
        // 前景进度条
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
        
        // 中间显示百分比文本
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}