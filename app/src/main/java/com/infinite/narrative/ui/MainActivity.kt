package com.infinite.narrative.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infinite.narrative.MVPApplication
import com.infinite.narrative.ui.component.LoadingIndicator
import com.infinite.narrative.ui.component.OptionSelector
import com.infinite.narrative.ui.component.StoryReader
import com.infinite.narrative.ui.component.WorldSelector
import com.infinite.narrative.ui.viewmodel.MainUiState
import com.infinite.narrative.ui.viewmodel.MainViewModel

/**
 * 主Activity - MVP版本的用户界面
 */
class MainActivity : ComponentActivity() {

    private lateinit var app: MVPApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = application as MVPApplication

        setContent {
            MaterialTheme {
                // 使用ViewModel管理UI状态
                val mainViewModel: MainViewModel = viewModel {
                    MainViewModel(
                        narrativeEngine = app.narrativeEngine,
                        playerId = "player_001" // 简化实现，实际项目中应该有用户系统
                    )
                }

                InfiniteNarrativeApp(
                    viewModel = mainViewModel,
                    onRetry = { mainViewModel.retry() }
                )
            }
        }
    }
}

/**
 * 主应用界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfiniteNarrativeApp(
    viewModel: MainViewModel,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState: MainUiState by viewModel.uiState

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("无限叙事 - 万象物语")
                },
                actions = {
                    IconButton(
                        onClick = { /* 设置菜单 */ }
                    ) {
                        androidx.compose.material.icons.Icons.Default.Settings
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is MainUiState.Loading -> {
                    LoadingIndicator(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is MainUiState.WorldSelection -> {
                    WorldSelectionScreen(
                        state = state,
                        onWorldSelected = { viewModel.selectWorld(it) },
                        onRetry = onRetry,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize()
                    )
                }

                is MainUiState.StoryReading -> {
                    StoryReadingScreen(
                        state = state,
                        onContinue = { viewModel.continueStory() },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }

                is MainUiState.OptionSelection -> {
                    OptionSelectionScreen(
                        state = state,
                        onOptionSelected = { viewModel.selectOption(it) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }

                is MainUiState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

/**
 * 世界选择界面
 */
@Composable
fun WorldSelectionScreen(
    state: MainUiState.WorldSelection,
    onWorldSelected: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 为WorldSelector添加权重，确保它获得足够的高度
        WorldSelector(
            worlds = state.worldOptions,
            onWorldSelected = onWorldSelected,
            recommendation = state.recommendation,
            modifier = Modifier.weight(1f) // 分配剩余空间
        )

        Button(
            onClick = { onRetry() },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Text("重新生成推荐")
        }
    }
}

/**
 * 故事阅读界面
 */
@Composable
fun StoryReadingScreen(
    state: MainUiState.StoryReading,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentProgress = remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StoryReader(
            storyText = state.storyText,
            onTextFinished = onContinue,
            onProgressChanged = { progress ->
                currentProgress.floatValue = progress
            },
            modifier = Modifier.weight(1f)
        )

        LinearProgressIndicator(
            progress = { currentProgress.floatValue },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

/**
 * 选项选择界面
 */
@Composable
fun OptionSelectionScreen(
    state: MainUiState.OptionSelection,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OptionSelector(
        options = state.options,
        onOptionSelected = onOptionSelected,
        modifier = modifier
    )
}

/**
 * 错误界面
 */
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "发生错误",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onRetry
        ) {
            Text("重试")
        }
    }
}
