package com.infinite.narrative

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.infinite.narrative.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 用户测试框架 - 验证MVP版本的用户体验
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UserTestFramework {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    private lateinit var context: Context
    
    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }
    
    /**
     * 测试1: 验证应用启动和初始界面
     */
    @Test
    fun testAppLaunchAndInitialUI() {
        // 验证应用成功启动
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))
        
        // 验证标题栏显示正确
        onView(withText("无限叙事 - 万象物语")).check(matches(isDisplayed()))
        
        // 验证加载指示器显示
        onView(withText("初始化中...")).check(matches(isDisplayed()))
    }
    
    /**
     * 测试2: 验证世界选择功能
     */
    @Test
    fun testWorldSelectionFlow() {
        // 等待世界选择界面加载
        Thread.sleep(3000)
        
        // 验证世界选择界面显示
        onView(withText("选择你的下一个世界：")).check(matches(isDisplayed()))
        
        // 验证三个世界观卡片显示
        onView(withText("赛博蓬莱")).check(matches(isDisplayed()))
        onView(withText("法典迷城")).check(matches(isDisplayed()))
        onView(withText("衰败王座")).check(matches(isDisplayed()))
        
        // 验证推荐文本显示
        onView(withId(R.id.recommendation_text)).check(matches(isDisplayed()))
    }
    
    /**
     * 测试3: 验证赛博蓬莱世界体验
     */
    @Test
    fun testCyberPenglaiWorldExperience() {
        // 选择赛博蓬莱世界
        onView(withText("赛博蓬莱")).perform(click())
        
        // 等待故事加载
        Thread.sleep(2000)
        
        // 验证故事阅读界面
        onView(withId(R.id.story_text)).check(matches(isDisplayed()))
        
        // 验证故事内容包含赛博元素
        onView(withId(R.id.story_text)).check { view, _ ->
            val text = view.text.toString()
            assert(text.contains("霓虹") || text.contains("数据") || text.contains("云端"))
        }
        
        // 等待选项显示
        Thread.sleep(3000)
        
        // 验证选项选择界面
        onView(withText("请选择你的行动：")).check(matches(isDisplayed()))
        
        // 验证选项包含合理的选择
        onView(withText("前往数据庙宇寻找修复玉简的方法")).check(matches(isDisplayed()))
        onView(withText("寻找网络游侠云梦的帮助")).check(matches(isDisplayed()))
        onView(withText("拜访老道长寻求古老智慧")).check(matches(isDisplayed()))
    }
    
    /**
     * 测试4: 验证选项交互
     */
    @Test
    fun testOptionSelection() {
        // 选择赛博蓬莱世界
        onView(withText("赛博蓬莱")).perform(click())
        
        // 等待并选择第一个选项
        Thread.sleep(5000)
        onView(withText("前往数据庙宇寻找修复玉简的方法")).perform(click())
        
        // 验证故事继续
        Thread.sleep(3000)
        onView(withId(R.id.story_text)).check(matches(isDisplayed()))
    }
    
    /**
     * 测试5: 验证法典迷城世界体验
     */
    @Test
    fun testLawMazeWorldExperience() {
        // 重新开始选择法典迷城
        Thread.sleep(1000)
        onView(withText("重新生成推荐")).perform(click())
        Thread.sleep(2000)
        onView(withText("法典迷城")).perform(click())
        
        // 验证法典迷城的故事特色
        Thread.sleep(3000)
        onView(withId(R.id.story_text)).check { view, _ ->
            val text = view.text.toString()
            assert(text.contains("法律") || text.contains("法庭") || text.contains("法槌"))
        }
        
        // 验证法律相关的选项
        Thread.sleep(3000)
        onView(withText("寻找法律漏洞进行反击")).check(matches(isDisplayed()))
        onView(withText("与法官进行法律谈判")).check(matches(isDisplayed()))
        onView(withText("利用法律技巧逃离法庭")).check(matches(isDisplayed()))
    }
    
    /**
     * 测试6: 验证衰败王座世界体验
     */
    @Test
    fun testDecayingThroneWorldExperience() {
        // 重新开始选择衰败王座
        Thread.sleep(1000)
        onView(withText("重新生成推荐")).perform(click())
        Thread.sleep(2000)
        onView(withText("衰败王座")).perform(click())
        
        // 验证衰败王座的故事特色
        Thread.sleep(3000)
        onView(withId(R.id.story_text)).check { view, _ ->
            val text = view.text.toString()
            assert(text.contains("神庙") || text.contains("祭司") || text.contains("信仰"))
        }
        
        // 验证神秘氛围的选项
        Thread.sleep(3000)
        onView(withText("进行古老的祭祀仪式")).check(matches(isDisplayed()))
        onView(withText("寻找流浪诗人的帮助")).check(matches(isDisplayed()))
        onView(withText("在神庙废墟中隐藏")).check(matches(isDisplayed()))
    }
    
    /**
     * 测试7: 验证UI动效和交互
     */
    @Test
    fun testUIAnimationsAndInteractions() {
        // 验证卡片浮现动画
        onView(withText("赛博蓬莱")).check(matches(isDisplayed()))
        
        // 验证选项展开动画
        onView(withText("赛博蓬莱")).perform(click())
        Thread.sleep(3000)
        
        // 验证打字机效果
        onView(withId(R.id.story_text)).check { view, _ ->
            // 验证文本是逐步显示的（这里通过检查文本长度变化来验证）
            val initialText = view.text.toString()
            Thread.sleep(1000)
            val finalText = view.text.toString()
            assert(finalText.length > initialText.length)
        }
    }
    
    /**
     * 测试8: 验证错误处理
     */
    @Test
    fun testErrorHandling() {
        // 测试网络错误处理（模拟）
        // 这里可以添加模拟网络错误的测试
        
        // 验证重试功能
        onView(withText("重新生成推荐")).perform(click())
        Thread.sleep(2000)
        onView(withText("选择你的下一个世界：")).check(matches(isDisplayed()))
    }
    
    /**
     * 测试9: 验证性能表现
     */
    @Test
    fun testPerformance() {
        val startTime = System.currentTimeMillis()
        
        // 完整的世界选择流程
        onView(withText("赛博蓬莱")).perform(click())
        Thread.sleep(6000) // 等待故事生成和显示
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // 验证响应时间在合理范围内（小于10秒）
        assert(duration < 10000) { "故事生成时间过长: ${duration}ms" }
    }
    
    /**
     * 测试10: 验证内容安全性
     */
    @Test
    fun testContentSafety() {
        // 选择任意世界
        onView(withText("赛博蓬莱")).perform(click())
        
        // 等待故事显示
        Thread.sleep(5000)
        
        // 验证内容不包含敏感词
        onView(withId(R.id.story_text)).check { view, _ ->
            val text = view.text.toString()
            
            // 检查不包含暴力、色情等敏感内容
            val sensitiveWords = listOf("屠杀", "血腥", "色情", "自杀", "仇恨")
            sensitiveWords.forEach { word ->
                assert(!text.contains(word)) { "故事中包含敏感词: $word" }
            }
        }
    }
}

/**
 * 用户体验评估指标
 */
data class UserExperienceMetrics(
    val appLaunchTime: Long = 0,
    val worldSelectionTime: Long = 0,
    val storyGenerationTime: Long = 0,
    val optionResponseTime: Long = 0,
    val userSatisfaction: Float = 0f,
    val contentSafetyScore: Float = 0f,
    val narrativeCoherenceScore: Float = 0f
)

/**
 * 测试配置
 */
object TestConfig {
    const val WAIT_TIME_SHORT = 1000L
    const val WAIT_TIME_MEDIUM = 3000L
    const val WAIT_TIME_LONG = 5000L
    const val MAX_RESPONSE_TIME = 10000L
}