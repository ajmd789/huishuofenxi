package org.example.project

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.pathString

data class TxtFileInfo(
    val fileName: String,
    val absolutePath: String,
    val sizeBytes: Long,
    val lastModified: FileTime,
)

data class TxtScanResult(
    val rootPath: String,
    val files: List<TxtFileInfo>,
    val totalFileCount: Int,
    val message: String,
)

object TxtScanner {
    fun scan(rootPath: String): TxtScanResult {
        val normalizedPath = rootPath.trim()
        if (normalizedPath.isEmpty()) {
            return TxtScanResult(
                rootPath = normalizedPath,
                files = emptyList(),
                totalFileCount = 0,
                message = "请输入要扫描的目录路径。",
            )
        }

        val root = Path.of(normalizedPath)
        if (!root.isDirectory()) {
            return TxtScanResult(
                rootPath = normalizedPath,
                files = emptyList(),
                totalFileCount = 0,
                message = "目录不存在，或当前路径不是文件夹：$normalizedPath",
            )
        }

        val files = Files.walk(root).use { pathStream ->
            pathStream
                .filter { path -> path.isRegularFile() }
                .filter { path -> isSupportedTxtFile(path) }
                .map { path -> toTxtFileInfo(path) }
                .toList()
                .sortedWith(
                    compareByDescending<TxtFileInfo> { it.lastModified.toMillis() }
                        .thenBy { it.fileName.lowercase() },
                )
        }

        val message = if (files.isEmpty()) {
            "未找到任何 .txt 文件。"
        } else {
            "共扫描到 ${files.size} 个 TXT 文件。"
        }

        return TxtScanResult(
            rootPath = normalizedPath,
            files = files,
            totalFileCount = files.size,
            message = message,
        )
    }

    private fun isSupportedTxtFile(path: Path): Boolean =
        path.extension.equals("txt", ignoreCase = true)

    private fun toTxtFileInfo(path: Path): TxtFileInfo {
        val sizeBytes = runCatching { path.fileSize() }.getOrDefault(0L)
        val lastModified = runCatching { path.getLastModifiedTime() }.getOrDefault(FileTime.fromMillis(0L))
        return TxtFileInfo(
            fileName = path.name,
            absolutePath = path.pathString,
            sizeBytes = sizeBytes,
            lastModified = lastModified,
        )
    }
}
