package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState // 引入窗口状态
// 引入 Voyager 相关
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition

fun main() = application {
    // 1. 记住窗口的状态，这样才能在后面的代码里控制它全屏
    val windowState = rememberWindowState()

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState, // 2. 把状态绑定给 Window
        title = "Excel 文件分析器",
    ) {
        // 3. 原来的 DesktopFileAnalyzerApp() 删掉
        // 换成 Navigator (也就是路由的根容器)
        // 并把你的首页 (HomeScreen) 设为默认启动的页面
        Navigator(HomeScreen(windowState)) { navigator ->
            // 可选：添加一个丝滑的左右滑动页面切换动画
            SlideTransition(navigator)
        }
    }
}
