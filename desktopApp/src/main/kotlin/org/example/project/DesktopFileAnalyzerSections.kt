package org.example.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun SummarySection(result: XlsxScanResult, isLoading: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        SelectionContainer {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "扫描摘要",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Text("扫描中...")
                        }
                    }
                }

                Text("当前目录: ${result.rootPath}")
                Text("状态: ${result.message}")
                Text("文件数: ${result.totalFileCount}")
                Text("Sheet 总数: ${result.totalSheetCount}")
                Text("总行数: ${result.totalRowCount}")
                Text("失败文件数: ${result.failedFileCount}")
                if (result.failedFileCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        result.files.filter { it.errorMessage != null }.forEach { failedFile ->
                            Text(
                                text = "失败文件: ${failedFile.absolutePath}，原因: ${failedFile.errorMessage}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun XlsxFileCard(fileInfo: XlsxFileInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = fileInfo.fileName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text("路径: ${fileInfo.absolutePath}")
            Text("大小: ${fileInfo.sizeBytes.formatFileSize()}")
            Text("修改时间: ${fileInfo.lastModified.formatForDisplay()}")
            Text("Sheet 数: ${fileInfo.sheetCount}")
            Text("首个 Sheet: ${fileInfo.firstSheetName ?: "-"}")
            Text("总行数: ${fileInfo.totalRows}")
            Text("最大列数: ${fileInfo.maxColumns}")
            Text("状态: ${fileInfo.status}")
            if (fileInfo.errorMessage != null) {
                Text(
                    text = "错误信息: ${fileInfo.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
internal fun EmptyState(message: String, height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "暂无可展示的 Excel 文件",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun ActionButtonsContent(
    isLoading: Boolean,
    showLatestData: Boolean,
    latestFileAvailable: Boolean,
    onScanClick: () -> Unit,
    onResetClick: () -> Unit,
    onLatestClick: () -> Unit,
    onFindTxtClick: () -> Unit,
) {
    Button(
        onClick = onScanClick,
        enabled = !isLoading,
    ) {
        Text("开始扫描")
    }
    TextButton(
        onClick = onResetClick,
        enabled = !isLoading,
    ) {
        Text("恢复默认")
    }
    if (showLatestData) {
        Button(
            onClick = onLatestClick,
            enabled = !isLoading && latestFileAvailable,
        ) {
            Text("最新数据")
        }
    }
    Button(
        onClick = onFindTxtClick,
        enabled = !isLoading,
    ) {
        Text("查找TXT")
    }
}
