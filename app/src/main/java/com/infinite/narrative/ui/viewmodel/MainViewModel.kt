package com.infinite.narrative.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infinite.narrative.ai.model.StoryOption
import com.infinite.narrative.engine.NarrativeEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 主ViewModel - 管理MVP应用的UI状态
 */
class MainViewModel(
    private val narrativeEngine: NarrativeEngine,
    private val playerId: String
) : ViewModel() {
    
    // UI状态流
    private val _uiState = mutableStateOf<MainUiState>(MainUiState.Loading("初始化中..."))
    val uiState: State<MainUiState> = _uiState
    
    // 当前游戏状态
    private var currentWorld: String? = null
    private var currentStoryProgress: String = ""
    
    init {
        loadInitialData()
    }
    
    /**
     * 加载初始数据
     */
    private fun loadInitialData() {
        _uiState.value = MainUiState.Loading("正在加载游戏数据...")
        
        viewModelScope.launch {
            try {
                // 生成世界选择推荐
                val worldSelection = narrativeEngine.generateWorldSelection(playerId)
                _uiState.value = MainUiState.WorldSelection(
                    recommendation = worldSelection.recommendation,
                    worldOptions = worldSelection.worldOptions
                )
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error("加载失败: ${e.message}")
            }
        }
    }
    
    /**
     * 选择世界
     */
    fun selectWorld(worldId: String) {
        currentWorld = worldId
        _uiState.value = MainUiState.Loading("进入${worldId}世界...")
        
        viewModelScope.launch {
            try {
                // 生成第一个故事片段
                val result = narrativeEngine.generateStory(
                    playerId = playerId,
                    worldId = worldId
                )
                
                currentStoryProgress = result.storySegment
                _uiState.value = MainUiState.StoryReading(
                    storyText = result.storySegment,
                    readProgress = 0f
                )
                
                // 自动继续到选项选择
                viewModelScope.launch {
                    delay(3000) // 等待故事阅读
                    _uiState.value = MainUiState.OptionSelection(result.options)
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error("故事生成失败: ${e.message}")
            }
        }
    }
    
    /**
     * 继续故事阅读
     */
    fun continueStory() {
        if (currentWorld != null) {
            _uiState.value = MainUiState.Loading("正在生成后续故事...")
            
            viewModelScope.launch {
                try {
                    val result = narrativeEngine.generateStory(
                        playerId = playerId,
                        worldId = currentWorld!!,
                        playerChoice = null // 无选择时继续当前故事线
                    )
                    
                    currentStoryProgress += "\n\n" + result.storySegment
                    _uiState.value = MainUiState.StoryReading(
                        storyText = result.storySegment,
                        readProgress = 0f
                    )
                    
                    // 自动继续到选项选择
                    viewModelScope.launch {
                        delay(3000)
                        _uiState.value = MainUiState.OptionSelection(result.options)
                    }
                } catch (e: Exception) {
                    _uiState.value = MainUiState.Error("故事继续失败: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 选择选项
     */
    fun selectOption(optionId: String) {
        if (currentWorld != null) {
            _uiState.value = MainUiState.Loading("正在生成故事发展...")
            
            viewModelScope.launch {
                try {
                    val result = narrativeEngine.generateStory(
                        playerId = playerId,
                        worldId = currentWorld!!,
                        playerChoice = optionId
                    )
                    
                    currentStoryProgress += "\n\n" + result.storySegment
                    _uiState.value = MainUiState.StoryReading(
                        storyText = result.storySegment,
                        readProgress = 0f
                    )
                    
                    // 自动继续到选项选择
                    viewModelScope.launch {
                        delay(3000)
                        _uiState.value = MainUiState.OptionSelection(result.options)
                    }
                } catch (e: Exception) {
                    _uiState.value = MainUiState.Error("选项处理失败: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 重试
     */
    fun retry() {
        loadInitialData()
    }
    
    /**
     * 获取当前故事进度
     */
    fun getCurrentStoryProgress(): String = currentStoryProgress
}

/**
 * 主UI状态密封类
 */
sealed class MainUiState {
    
    /**
     * 加载状态
     */
    data class Loading(val message: String) : MainUiState()
    
    /**
     * 世界选择状态
     */
    data class WorldSelection(
        val recommendation: String,
        val worldOptions: List<NarrativeEngine.WorldOption>
    ) : MainUiState()
    
    /**
     * 故事阅读状态
     */
    data class StoryReading(
        val storyText: String,
        val readProgress: Float
    ) : MainUiState()
    
    /**
     * 选项选择状态
     */
    data class OptionSelection(
        val options: List<StoryOption>
    ) : MainUiState()
    
    /**
     * 错误状态
     */
    data class Error(val message: String) : MainUiState()
}

/**
 * 世界选择状态
 */
data class WorldSelectionState(
    val isLoading: Boolean = false,
    val recommendation: String = "",
    val worldOptions: List<NarrativeEngine.WorldOption> = emptyList(),
    val error: String? = null
)

/**
 * 故事阅读状态
 */
data class StoryReadingState(
    val isLoading: Boolean = false,
    val storyText: String = "",
    val readProgress: Float = 0f,
    val error: String? = null
)

/**
 * 选项选择状态
 */
data class OptionSelectionState(
    val isLoading: Boolean = false,
    val options: List<StoryOption> = emptyList(),
    val error: String? = null
)
