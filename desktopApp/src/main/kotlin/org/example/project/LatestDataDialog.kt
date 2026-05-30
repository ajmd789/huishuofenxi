package org.example.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * “最新数据”弹窗组件。
 *
 * 这里单独拆文件，方便后续继续扩展：
 * 1. 增加更多字段
 * 2. 加入复制路径、打开目录等动作
 * 3. 保持主页面文件长度和职责清晰
 */
@Composable
fun LatestDataDialog(
    fileInfo: XlsxFileInfo,
    onDismissRequest: () -> Unit,
) {
    LatestDataDialog(
        title = "最新数据",
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = "最新数据文件",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text("文件名: ${fileInfo.fileName}")
        Text("完整路径: ${fileInfo.absolutePath}")
        Text("修改时间: ${fileInfo.lastModified.formatForDisplay()}")
        Text("文件大小: ${fileInfo.sizeBytes.formatFileSize()}")
        Text("Sheet 数: ${fileInfo.sheetCount}")
        Text("首个 Sheet: ${fileInfo.firstSheetName ?: "-"}")
        Text("总行数: ${fileInfo.totalRows}")
        Text("最大列数: ${fileInfo.maxColumns}")
        Text("读取状态: ${fileInfo.status}")

        if (fileInfo.errorMessage != null) {
            Text(
                text = "错误信息: ${fileInfo.errorMessage}",
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
fun LatestDataDialog(
    title: String,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onCloseRequest = onDismissRequest,
        title = title,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                content()
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("关闭")
                }
            }
        }
    }
}
