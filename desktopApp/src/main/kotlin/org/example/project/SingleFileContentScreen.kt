package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SingleFileContentScreen(
    private val files: List<XlsxFileInfo>,
    private val initialIndex: Int = 0,
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var currentIndex by remember {
            mutableIntStateOf(initialIndex.coerceIn(0, files.lastIndex.coerceAtLeast(0)))
        }
        var isLoading by remember { mutableStateOf(false) }
        var preview by remember { mutableStateOf(SingleFileContentPreview(content = "", isTruncated = false)) }
        val currentFile = files.getOrNull(currentIndex)

        LaunchedEffect(currentFile?.absolutePath) {
            if (currentFile == null) {
                preview = SingleFileContentPreview(
                    content = "",
                    isTruncated = false,
                    errorMessage = "当前没有可展示的文件内容。",
                )
                return@LaunchedEffect
            }

            isLoading = true
            preview = withContext(Dispatchers.IO) {
                SingleFileContentReader.read(currentFile)
            }
            isLoading = false
        }

        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val pagePadding = (maxWidth * 0.02f).coerceIn(12.dp, 28.dp)
                    val sectionSpacing = (maxHeight * 0.02f).coerceIn(12.dp, 20.dp)
                    val sideWidth = (maxWidth * 0.13f).coerceIn(92.dp, 140.dp)
                    val cardPadding = (maxWidth * 0.02f).coerceIn(12.dp, 20.dp)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(pagePadding),
                        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "单文件内容",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "延续首页卡片式阅读风格，按文件列表顺序浏览 Excel 内容预览。",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Button(onClick = { navigator.pop() }) {
                                Text("返回")
                            }
                        }

                        if (currentFile == null) {
                            EmptyContentState(message = "当前没有文件列表，无法展示单文件内容。")
                        } else {
                            FileSummaryCard(
                                fileInfo = currentFile,
                                currentIndex = currentIndex,
                                totalCount = files.size,
                                isTruncated = preview.isTruncated,
                            )

                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                NavigationCard(
                                    modifier = Modifier
                                        .width(sideWidth)
                                        .fillMaxHeight(),
                                    title = "上一份",
                                    hint = if (currentIndex == 0) "已到第一份文件" else "查看前一个文件",
                                    enabled = currentIndex > 0 && !isLoading,
                                    onClick = { currentIndex -= 1 },
                                )
                                ContentPanel(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    isLoading = isLoading,
                                    preview = preview,
                                    contentPadding = cardPadding,
                                )
                                NavigationCard(
                                    modifier = Modifier
                                        .width(sideWidth)
                                        .fillMaxHeight(),
                                    title = "下一份",
                                    hint = if (currentIndex == files.lastIndex) "已到最后一份" else "查看后一个文件",
                                    enabled = currentIndex < files.lastIndex && !isLoading,
                                    onClick = { currentIndex += 1 },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileSummaryCard(
    fileInfo: XlsxFileInfo,
    currentIndex: Int,
    totalCount: Int,
    isTruncated: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "当前文件 ${currentIndex + 1} / $totalCount",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = fileInfo.fileName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "路径: ${fileInfo.absolutePath}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("修改时间: ${fileInfo.lastModified.formatForDisplay()}")
                Text("文件大小: ${fileInfo.sizeBytes.formatFileSize()}")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Sheet 数: ${fileInfo.sheetCount}")
                Text("总行数: ${fileInfo.totalRows}")
                Text("最大列数: ${fileInfo.maxColumns}")
            }
            if (isTruncated) {
                Text(
                    text = "提示: 当前内容较长，页面已自动截断部分行列。",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun NavigationCard(
    modifier: Modifier,
    title: String,
    hint: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.width(1.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onClick,
                enabled = enabled,
            ) {
                Text(title)
            }
        }
    }
}

@Composable
private fun ContentPanel(
    modifier: Modifier,
    isLoading: Boolean,
    preview: SingleFileContentPreview,
    contentPadding: Dp,
) {
    Surface(
        modifier = modifier,
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "内容预览",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "支持横向和纵向滚动，可直接复制文本内容。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider()
            ContentBody(
                isLoading = isLoading,
                preview = preview,
                contentPadding = contentPadding,
            )
        }
    }
}

@Composable
private fun ContentBody(
    isLoading: Boolean,
    preview: SingleFileContentPreview,
    contentPadding: Dp,
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(strokeWidth = 2.dp)
                Text("正在读取文件内容...")
            }
        }
        return
    }

    if (preview.errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = preview.errorMessage,
                color = MaterialTheme.colorScheme.error,
            )
        }
        return
    }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    SelectionContainer {
        Text(
            text = preview.content.ifBlank { "当前文件没有可展示的内容。" },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(contentPadding)
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EmptyContentState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
