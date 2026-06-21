package org.example.project

import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.pathString

private const val DEFAULT_SCAN_ROOT = "/Users/tom/Desktop/codes/java-desktop-data-analysis"

/**
 * 单个 Excel 文件的展示模型。
 *
 * 这里把 UI 需要的字段都提前整理好，界面层只负责展示，
 * 不再关心文件系统和 POI 的细节。
 */
data class XlsxFileInfo(
    val fileName: String,
    val absolutePath: String,
    val sizeBytes: Long,
    val lastModified: FileTime,
    val sheetCount: Int,
    val firstSheetName: String?,
    val totalRows: Int,
    val maxColumns: Int,
    val status: String,
    val errorMessage: String? = null,
)

/**
 * 扫描结果除了文件列表，还带上汇总信息和错误提示，
 * 这样界面顶部可以统一展示统计摘要。
 */
data class XlsxScanResult(
    val rootPath: String,
    val files: List<XlsxFileInfo>,
    val totalFileCount: Int,
    val totalSheetCount: Int,
    val totalRowCount: Int,
    val failedFileCount: Int,
    val message: String,
)

/**
 * JVM 桌面专用的 Excel 扫描器。
 *
 * 这个实现会递归遍历目录下所有 .xlsx 文件，并用 Apache POI 提取：
 * 1. 文件基础信息
 * 2. Sheet 数量与首个 Sheet 名称
 * 3. 每个工作簿的总行数和最大列数
 *
 * 之所以放在桌面模块，是因为 Apache POI 只适合 JVM 端使用，
 * 不应该影响 Android/iOS/Web 的编译。
 */
object XlsxScanner {
    fun defaultRootPath(): String = DEFAULT_SCAN_ROOT

    fun scan(rootPath: String): XlsxScanResult {
        val normalizedPath = rootPath.trim()
        if (normalizedPath.isEmpty()) {
            return XlsxScanResult(
                rootPath = normalizedPath,
                files = emptyList(),
                totalFileCount = 0,
                totalSheetCount = 0,
                totalRowCount = 0,
                failedFileCount = 0,
                message = "请输入要扫描的目录路径。",
            )
        }

        val root = Path.of(normalizedPath)
        if (!root.isDirectory()) {
            return XlsxScanResult(
                rootPath = normalizedPath,
                files = emptyList(),
                totalFileCount = 0,
                totalSheetCount = 0,
                totalRowCount = 0,
                failedFileCount = 0,
                message = "目录不存在，或当前路径不是文件夹：$normalizedPath",
            )
        }

        val files = Files.walk(root).use { pathStream ->
            pathStream
                .filter { path -> path.isRegularFile() }
                .filter { path -> isSupportedXlsxFile(path) }
                .map { path -> readXlsxFile(path) }
                .toList()
                .sortedWith(
                    compareByDescending<XlsxFileInfo> { it.lastModified.toMillis() }
                        .thenBy { it.fileName.lowercase() },
                )
        }

        val totalSheetCount = files.sumOf { it.sheetCount }
        val totalRowCount = files.sumOf { it.totalRows }
        val failedFileCount = files.count { it.errorMessage != null }
        val message = when {
            files.isEmpty() -> "未找到任何 .xlsx 文件。"
            failedFileCount == 0 -> "共扫描到 ${files.size} 个 Excel 文件。"
            else -> "共扫描到 ${files.size} 个 Excel 文件，其中 ${failedFileCount} 个读取失败。"
        }

        return XlsxScanResult(
            rootPath = normalizedPath,
            files = files,
            totalFileCount = files.size,
            totalSheetCount = totalSheetCount,
            totalRowCount = totalRowCount,
            failedFileCount = failedFileCount,
            message = message,
        )
    }

    private fun isSupportedXlsxFile(path: Path): Boolean {
//        val fileName = path.name
//        return path.extension.equals("xlsx", ignoreCase = true) && !fileName.startsWith("~$")
        //第一步： 只关心 .xlsx拓展名的普通文件
        if(!path.extension.equals("xlsx", ignoreCase = true)) return false
        val fileName = path.name
        //第二步：过滤各类临时文件与隐藏文件
        //windows office 临时文件以"~$"开头
        if(fileName.startsWith("~$")) return false
        //Macos  以“.~”开头
        if(fileName.startsWith(".~")) return false
        if(fileName.startsWith(".")) return false
        return true
    }

    private fun readXlsxFile(path: Path): XlsxFileInfo {
        val sizeBytes = runCatching { path.fileSize() }.getOrDefault(0L)
        val lastModified = runCatching { path.getLastModifiedTime() }.getOrDefault(FileTime.fromMillis(0L))

        return runCatching {
            path.inputStream().use { inputStream ->
                createFileInfo(path = path, sizeBytes = sizeBytes, lastModified = lastModified, inputStream = inputStream)
            }
        }.getOrElse { throwable ->
            XlsxFileInfo(
                fileName = path.name,
                absolutePath = path.pathString,
                sizeBytes = sizeBytes,
                lastModified = lastModified,
                sheetCount = 0,
                firstSheetName = null,
                totalRows = 0,
                maxColumns = 0,
                status = "读取失败",
                errorMessage = throwable.message ?: throwable::class.simpleName ?: "未知错误",
            )
        }
    }

    /**
     * 真正读取工作簿的逻辑单独拆出来，便于测试，也方便后续扩展更多统计指标。
     */
    private fun createFileInfo(path: Path, sizeBytes: Long, lastModified: FileTime, inputStream: InputStream): XlsxFileInfo {
        WorkbookFactory.create(inputStream).use { workbook ->
            var totalRows = 0
            var maxColumns = 0

            for (sheetIndex in 0 until workbook.numberOfSheets) {
                val sheet = workbook.getSheetAt(sheetIndex)
                totalRows += sheet.physicalNumberOfRows

                for (row in sheet) {
                    val columnCount = row.lastCellNum.toInt().coerceAtLeast(0)
                    if (columnCount > maxColumns) {
                        maxColumns = columnCount
                    }
                }
            }

            return XlsxFileInfo(
                fileName = path.name,
                absolutePath = path.pathString,
                sizeBytes = sizeBytes,
                lastModified = lastModified,
                sheetCount = workbook.numberOfSheets,
                firstSheetName = workbook.getSheetAt(0)?.sheetName,
                totalRows = totalRows,
                maxColumns = maxColumns,
                status = "读取成功",
                errorMessage = null,
            )
        }
    }
}

private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun FileTime.formatForDisplay(): String =
    toInstant()
        .atZone(ZoneId.systemDefault())
        .format(dateTimeFormatter)

fun Long.formatFileSize(): String {
    val kiloByte = 1024.0
    val megaByte = kiloByte * 1024
    return when {
        this >= megaByte -> String.format("%.2f MB", this / megaByte)
        this >= kiloByte -> String.format("%.2f KB", this / kiloByte)
        else -> "$this B"
    }
}
