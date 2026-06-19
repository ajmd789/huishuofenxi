package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

// 你构思的新页面：获取股票热度页
class GetHotScreen(private val windowState: WindowState) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Box(
            modifier = Modifier.fillMaxSize().background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text("📈 这里是全屏的股票热度页面！", color = Color.White)

            // 返回按钮
            Button(
                onClick = {
                    // 1. 退出全屏，恢复浮动小窗口
                    windowState.placement = WindowPlacement.Floating
                    // 2. 路由出栈，返回上一页
                    navigator.pop()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Text("退出全屏并返回")
            }
        }
    }
}