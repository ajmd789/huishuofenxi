package org.example.project

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.Composable
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

@Composable
fun TxtFinderDialog(
    initialPath: String,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var directoryPath by remember { mutableStateOf(initialPath) }
    var isLoading by remember { mutableStateOf(false) }
    var scanResult by remember {
        mutableStateOf(
            TxtScanResult(
                rootPath = directoryPath,
                files = emptyList(),
                totalFileCount = 0,
                message = "请输入目录并点击扫描。",
            ),
        )
    }

    fun launchScan(path: String) {
        val normalizedPath = path.trim()
        coroutineScope.launch {
            isLoading = true
            scanResult = withContext(Dispatchers.IO) {
                TxtScanner.scan(normalizedPath)
            }
            isLoading = false
        }
    }

    LatestDataDialog(
        title = "TXT 文件查找",
        onDismissRequest = onDismissRequest,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val rowSpacing = (maxWidth * 0.012f).coerceIn(8.dp, 14.dp)
            val blockSpacing = (maxWidth * 0.02f).coerceIn(8.dp, 14.dp)
            val listItemSpacing = (maxWidth * 0.015f).coerceIn(6.dp, 10.dp)
            val cardPadding = (maxWidth * 0.02f).coerceIn(8.dp, 14.dp)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(blockSpacing),
            ) {
            Text(
                text = "递归查找目录下的 .txt 文件",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = directoryPath,
                    onValueChange = { directoryPath = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("扫描目录") },
                    singleLine = true,
                    enabled = !isLoading,
                )
                Button(
                    onClick = { launchScan(directoryPath) },
                    enabled = !isLoading,
                ) {
                    Text("扫描")
                }
            }

            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                    Text("扫描中...")
                }
            }

            Text("当前目录: ${scanResult.rootPath}")
            Text("状态: ${scanResult.message}")
            Text("文件数: ${scanResult.totalFileCount}")
            HorizontalDivider()

            if (scanResult.files.isEmpty() && !isLoading) {
                Text(
                    text = "暂无 .txt 文件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                verticalArrangement = Arrangement.spacedBy(listItemSpacing),
            ) {
                items(
                    items = scanResult.files,
                    key = { fileInfo -> fileInfo.absolutePath },
                ) { fileInfo ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 1.dp,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Column(
                            modifier = Modifier.padding(cardPadding),
                            verticalArrangement = Arrangement.spacedBy(blockSpacing),
                        ) {
                            Text(
                                text = fileInfo.fileName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            ScrollableLineText(
                                text = fileInfo.absolutePath,
                                modifier = Modifier.fillMaxWidth(),
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
private fun ScrollableLineText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Text(
        text = text,
        modifier = modifier.horizontalScroll(scrollState),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
}
