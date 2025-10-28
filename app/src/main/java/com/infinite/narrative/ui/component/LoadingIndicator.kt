package com.infinite.narrative.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 加载指示器组件
 * 用于显示应用加载状态，支持自定义加载文本和动画效果
 */
@Composable
fun LoadingIndicator(
    text: String = "正在生成故事...",
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    // 延迟显示以避免闪烁
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // 主要的圆形进度指示器
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            
            // 加载文本
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
            
            // 可选的子文本，提供更多上下文
            AnimatedVisibility(
                visible = text.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "请稍候...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * 简化的加载指示器，仅包含进度条
 */
@Composable
fun SimpleLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Int = 24
) {
    CircularProgressIndicator(
        modifier = modifier.size(size.dp),
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 2.dp
    )
}

/**
 * 带背景遮罩的全屏加载指示器
 */
@Composable
fun FullScreenLoadingIndicator(
    text: String = "加载中...",
    onDismiss: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 半透明背景遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
        )
        
        // 加载内容
        LoadingIndicator(
            text = text,
            modifier = Modifier
                .wrapContentSize()
                .padding(24.dp)
        )
        
        // 如果提供了关闭回调，可以添加点击区域
        if (onDismiss != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onDismiss)
            )
        }
    }
}
