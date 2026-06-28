package org.example.project

import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.inputStream

private const val MAX_PREVIEW_ROWS = 400
private const val MAX_PREVIEW_COLUMNS = 24
private const val MAX_CELL_TEXT_LENGTH = 120

data class SingleFileContentPreview(
    val content: String,
    val isTruncated: Boolean,
    val errorMessage: String? = null,
)

object SingleFileContentReader {
    fun read(fileInfo: XlsxFileInfo): SingleFileContentPreview {
        val filePath = Path.of(fileInfo.absolutePath)
        if (!Files.exists(filePath)) {
            return SingleFileContentPreview(
                content = "",
                isTruncated = false,
                errorMessage = "文件不存在：${fileInfo.absolutePath}",
            )
        }

        return runCatching {
            filePath.inputStream().use { inputStream ->
                WorkbookFactory.create(inputStream).use { workbook ->
                    val formatter = DataFormatter()
                    val builder = StringBuilder()
                    var renderedRows = 0
                    var isTruncated = false

                    builder.appendLine("文件名: ${fileInfo.fileName}")
                    builder.appendLine("路径: ${fileInfo.absolutePath}")
                    builder.appendLine("Sheet 数: ${workbook.numberOfSheets}")
                    builder.appendLine()

                    outer@ for (sheetIndex in 0 until workbook.numberOfSheets) {
                        val sheet = workbook.getSheetAt(sheetIndex)
                        builder.appendLine("========== Sheet ${sheetIndex + 1}: ${sheet.sheetName} ==========")

                        if (sheet.physicalNumberOfRows == 0) {
                            builder.appendLine("(空 Sheet)")
                            builder.appendLine()
                            continue
                        }

                        for (row in sheet) {
                            if (renderedRows >= MAX_PREVIEW_ROWS) {
                                isTruncated = true
                                break@outer
                            }

                            val columnCount = row.lastCellNum.toInt().coerceAtLeast(0)
                            val renderedColumns = columnCount.coerceAtMost(MAX_PREVIEW_COLUMNS)
                            val rowValues = buildList {
                                if (renderedColumns == 0) {
                                    add("(空行)")
                                } else {
                                    for (columnIndex in 0 until renderedColumns) {
                                        val rawText = formatter.formatCellValue(row.getCell(columnIndex))
                                        add(rawText.normalizeCellText())
                                    }
                                }
                            }

                            builder.append("第${row.rowNum + 1}行 | ")
                            builder.append(rowValues.joinToString(" | "))
                            if (columnCount > MAX_PREVIEW_COLUMNS) {
                                builder.append(" | ...(已省略后续列)")
                            }
                            builder.appendLine()
                            renderedRows += 1
                        }

                        builder.appendLine()
                    }

                    if (isTruncated) {
                        builder.appendLine("----------")
                        builder.appendLine("内容较长，当前仅展示前 $MAX_PREVIEW_ROWS 行。")
                    }

                    SingleFileContentPreview(
                        content = builder.toString().trimEnd(),
                        isTruncated = isTruncated,
                    )
                }
            }
        }.getOrElse { throwable ->
            SingleFileContentPreview(
                content = "",
                isTruncated = false,
                errorMessage = throwable.message ?: throwable::class.simpleName ?: "读取失败",
            )
        }
    }
}

private fun String.normalizeCellText(): String {
    val normalized = replace("\r\n", " ").replace('\n', ' ').trim()
    if (normalized.isEmpty()) {
        return "(空)"
    }
    if (normalized.length <= MAX_CELL_TEXT_LENGTH) {
        return normalized
    }
    return normalized.take(MAX_CELL_TEXT_LENGTH) + "..."
}
