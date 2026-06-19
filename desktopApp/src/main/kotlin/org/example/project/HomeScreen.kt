package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

// HomeScreen 继承自 Screen
// 构造函数接收 windowState，为了能控制全屏
class HomeScreen(private val windowState: WindowState) : Screen {

    @Composable
    override fun Content() {
        // 获取路由导航器 (相当于前端的 useRouter() 或者 Android 的 NavController)
        val navigator = LocalNavigator.currentOrThrow

        Column {
            // 1. 保留你原来的老界面代码！直接在这里调用！
            DesktopFileAnalyzerApp(
                onNavigateToHot = {
                    // 当内部的“热度”按钮被点击时，这里会被触发：

                    // 1. 让窗口最大化/全屏
                    windowState.placement = WindowPlacement.Fullscreen

                    // 2. 跳转到 GetHotScreen
                    navigator.push(GetHotScreen(windowState))
                }
            )

            // 2. 在老界面下面（或者你代码的任意按钮里），加一个跳转动作
            Button(onClick = {
                // 【跳转动作 1】让当前的窗口全屏
                windowState.placement = WindowPlacement.Maximized

                // 【跳转动作 2】页面跳转到 获取股票热度页
                navigator.push(GetHotScreen(windowState))
            }) {
                Text("跳转到 股票热度页 (全屏)")
            }
        }
    }
}