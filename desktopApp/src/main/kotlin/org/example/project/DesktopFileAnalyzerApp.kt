package org.example.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * macOS 桌面首页。
 *
 * 需求目标：
 * 1. 应用启动后默认带入一个扫描目录
 * 2. 支持用户手动修改路径后重新扫描
 * 3. 递归读取目录下所有 .xlsx 文件
 * 4. 把文件详情以列表形式展示出来
 */
@Composable
fun DesktopFileAnalyzerApp() {
    val coroutineScope = rememberCoroutineScope()

    // 顶层状态都放在界面入口，便于后续逐步替换成 ViewModel 或状态容器。
    var directoryPath by remember { mutableStateOf(XlsxScanner.defaultRootPath()) }
    var isLoading by remember { mutableStateOf(false) }
    var showLatestDataDialog by remember { mutableStateOf(false) }
    var showTxtFinderDialog by remember { mutableStateOf(false) }
    var scanResult by remember {
        mutableStateOf(
            XlsxScanResult(
                rootPath = directoryPath,
                files = emptyList(),
                totalFileCount = 0,
                totalSheetCount = 0,
                totalRowCount = 0,
                failedFileCount = 0,
                message = "准备扫描目录...",
            ),
        )
    }
    val latestFileInfo = scanResult.files.firstOrNull()

    fun launchScan(path: String) {
        val normalizedPath = path.trim()
        coroutineScope.launch {
            isLoading = true
            scanResult = withContext(Dispatchers.IO) {
                XlsxScanner.scan(normalizedPath)
            }
            isLoading = false
        }
    }

    // 首次打开桌面应用时，自动扫描默认目录，减少第一次操作步骤。
    LaunchedEffect(Unit) {
        launchScan(directoryPath)
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val pagePadding = (maxWidth * 0.02f).coerceIn(12.dp, 28.dp)
                val sectionSpacing = (maxHeight * 0.02f).coerceIn(12.dp, 20.dp)
                val rowSpacing = (maxWidth * 0.012f).coerceIn(8.dp, 14.dp)
                val listSpacing = (maxHeight * 0.015f).coerceIn(10.dp, 16.dp)
                val emptyStateHeight = (maxHeight * 0.25f).coerceIn(180.dp, 320.dp)
                val isCompact = maxWidth < 900.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(pagePadding),
                    verticalArrangement = Arrangement.spacedBy(sectionSpacing),
                ) {
                    Text(
                        text = "Excel 文件分析器",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "默认扫描目录下的所有 .xlsx 文件，并展示基础元数据与工作簿统计信息。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (isCompact) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(rowSpacing),
                        ) {
                            OutlinedTextField(
                                value = directoryPath,
                                onValueChange = { directoryPath = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("扫描目录") },
                                singleLine = true,
                                enabled = !isLoading,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ActionButtonsContent(
                                    isLoading = isLoading,
                                    showLatestData = scanResult.totalFileCount != 0,
                                    latestFileAvailable = latestFileInfo != null,
                                    onScanClick = { launchScan(directoryPath) },
                                    onResetClick = {
                                        directoryPath = XlsxScanner.defaultRootPath()
                                        launchScan(directoryPath)
                                    },
                                    onLatestClick = { showLatestDataDialog = true },
                                    onFindTxtClick = { showTxtFinderDialog = true },
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = directoryPath,
                                onValueChange = { directoryPath = it },
                                modifier = Modifier.weight(0.65f),
                                label = { Text("扫描目录") },
                                singleLine = true,
                                enabled = !isLoading,
                            )
                            Row(
                                modifier = Modifier.weight(0.35f),
                                horizontalArrangement = Arrangement.spacedBy(rowSpacing, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ActionButtonsContent(
                                    isLoading = isLoading,
                                    showLatestData = scanResult.totalFileCount != 0,
                                    latestFileAvailable = latestFileInfo != null,
                                    onScanClick = { launchScan(directoryPath) },
                                    onResetClick = {
                                        directoryPath = XlsxScanner.defaultRootPath()
                                        launchScan(directoryPath)
                                    },
                                    onLatestClick = { showLatestDataDialog = true },
                                    onFindTxtClick = { showTxtFinderDialog = true },
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    SummarySection(
                        result = scanResult,
                        isLoading = isLoading,
                    )

                    Text(
                        text = "文件列表",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    if (scanResult.files.isEmpty() && !isLoading) {
                        EmptyState(message = scanResult.message, height = emptyStateHeight)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(listSpacing),
                        ) {
                            items(
                                items = scanResult.files,
                                key = { fileInfo -> fileInfo.absolutePath },
                            ) { fileInfo ->
                                XlsxFileCard(fileInfo = fileInfo)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLatestDataDialog && latestFileInfo != null) {
        org.example.project.LatestDataDialog(
            fileInfo = latestFileInfo,
            onDismissRequest = { showLatestDataDialog = false },
        )
    }

    if (showTxtFinderDialog) {
        TxtFinderDialog(
            initialPath = directoryPath,
            onDismissRequest = { showTxtFinderDialog = false },
        )
    }
}

@Composable
private fun SummarySection(result: XlsxScanResult, isLoading: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
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
        }
    }
}

@Composable
private fun XlsxFileCard(fileInfo: XlsxFileInfo) {
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
private fun EmptyState(message: String, height: Dp) {
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
private fun ActionButtonsContent(
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
