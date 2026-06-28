package org.example.project

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
fun DesktopFileAnalyzerApp(
    onNavigateToHot: () -> Unit,
    onNavigateToSingleFileContent: (List<XlsxFileInfo>) -> Unit,
) {
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
    val topButtons = listOf(
        TopBarButton(text = "导入", onClick = { /* TODO */ }),
        TopBarButton(text = "导出", onClick = { /* TODO */ }),
        TopBarButton(text = "设置", onClick = { /* TODO */ }),
        TopBarButton(text = "热度", onClick = { onNavigateToHot() }),
        TopBarButton(
            text = "单文件内容",
            onClick = { onNavigateToSingleFileContent(scanResult.files) },
            enabled = scanResult.files.isNotEmpty(),
        ),
    )

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

                    TopButtonBar(
                        buttons = topButtons,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                        val listState = rememberLazyListState()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ){
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(listSpacing),
                            ) {
                                items(
                                    items = scanResult.files,
                                    key = { fileInfo -> fileInfo.absolutePath },
                                ) { fileInfo ->
                                    XlsxFileCard(fileInfo = fileInfo)
                                }
                            }
                            VerticalScrollbar(
                                adapter = rememberScrollbarAdapter(listState),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight()
                            )
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
