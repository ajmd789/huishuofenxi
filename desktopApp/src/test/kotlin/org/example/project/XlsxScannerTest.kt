package org.example.project

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

class XlsxScannerTest {
    @Test
    fun `scan should discover xlsx files recursively and extract workbook details`() {
        val root = Files.createTempDirectory("xlsx-scanner-test")
        val nestedDirectory = root.resolve("nested").createDirectories()
        val excelFile = nestedDirectory.resolve("sample.xlsx")

        createWorkbook(excelFile)

        val result = XlsxScanner.scan(root.toString())

        assertEquals(1, result.totalFileCount)
        assertEquals(1, result.totalSheetCount)
        assertEquals(2, result.totalRowCount)
        assertEquals(0, result.failedFileCount)

        val file = result.files.single()
        assertEquals("sample.xlsx", file.fileName)
        assertEquals(1, file.sheetCount)
        assertEquals("Data", file.firstSheetName)
        assertEquals(2, file.totalRows)
        assertEquals(2, file.maxColumns)
        assertNull(file.errorMessage)
    }

    @Test
    fun `scan should return readable message for invalid directory`() {
        val result = XlsxScanner.scan("/path/that/does/not/exist")

        assertEquals(0, result.totalFileCount)
        assertTrue(result.message.contains("目录不存在"))
    }

    private fun createWorkbook(file: Path) {
        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet("Data")
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("name")
            header.createCell(1).setCellValue("value")

            val row = sheet.createRow(1)
            row.createCell(0).setCellValue("demo")
            row.createCell(1).setCellValue(42.0)

            file.outputStream().use { outputStream ->
                workbook.write(outputStream)
            }
        }
    }
}
