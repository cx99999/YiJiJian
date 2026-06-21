package com.example.piecework.ui.home

import com.example.piecework.data.WorkRecordItem
import com.example.piecework.data.WorkSummary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val ExportDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

data class ProductStatItem(
    val productId: Long,
    val productName: String,
    val unitPriceCents: Long,
    val totalQuantity: Long,
    val pieceIncomeCents: Long
)

fun buildProductStats(records: List<WorkRecordItem>): List<ProductStatItem> {
    return records
        .filter { it.productId != null }
        .groupBy { it.productId ?: 0L }
        .mapNotNull { (productId, items) ->
            val first = items.firstOrNull() ?: return@mapNotNull null
            ProductStatItem(
                productId = productId,
                productName = first.productName ?: "产品",
                unitPriceCents = first.unitPriceCents ?: 0L,
                totalQuantity = items.sumOf { it.quantity.toLong() },
                pieceIncomeCents = items.sumOf { it.pieceIncomeCents }
            )
        }
        .sortedByDescending { it.pieceIncomeCents }
}

fun buildExportCsv(
    month: YearMonth,
    records: List<WorkRecordItem>,
    summary: WorkSummary,
    productStats: List<ProductStatItem>
): String {
    return buildString {
        appendLine("\uFEFF易计件数据导出")
        appendCsvRow(listOf("统计范围", month.formatMonthRange()))
        appendCsvRow(listOf("月总收入(元)", summary.finalIncomeCents.toYuanText()))
        appendCsvRow(listOf("计件收入(元)", summary.pieceIncomeCents.toYuanText()))
        appendCsvRow(listOf("补贴扣款(元)", (summary.subsidyCents - summary.deductionCents).toYuanText()))
        appendCsvRow(listOf("计件数量(件)", summary.totalQuantity.toString()))
        appendLine()

        appendLine("产品统计")
        appendCsvRow(listOf("产品", "单价(元)", "数量", "计件收入(元)"))
        productStats.forEach { item ->
            appendCsvRow(
                listOf(
                    item.productName,
                    item.unitPriceCents.toYuanText(),
                    item.totalQuantity.toString(),
                    item.pieceIncomeCents.toYuanText()
                )
            )
        }
        appendLine()

        appendLine("明细记录")
        appendCsvRow(listOf("日期", "类型", "产品/项目", "单价(元)", "数量", "计件收入(元)", "补贴(元)", "扣款(元)", "最终收入(元)", "备注"))
        records.sortedWith(compareBy<WorkRecordItem> { it.dateEpochDay }.thenBy { it.recordId }).forEach { record ->
            val date = LocalDate.ofEpochDay(record.dateEpochDay).format(ExportDateFormatter)
            val type = when {
                record.productId != null -> "计件"
                record.subsidyCents > 0 -> "日补贴"
                record.deductionCents > 0 -> "日扣款"
                else -> "记录"
            }
            appendCsvRow(
                listOf(
                    date,
                    type,
                    record.productName ?: type,
                    record.unitPriceCents?.toYuanText().orEmpty(),
                    if (record.productId != null) record.quantity.toString() else "",
                    record.pieceIncomeCents.toYuanText(),
                    record.subsidyCents.toYuanText(),
                    record.deductionCents.toYuanText(),
                    record.finalIncomeCents.toYuanText(),
                    record.remark
                )
            )
        }
    }
}

private fun StringBuilder.appendCsvRow(values: List<String>) {
    appendLine(values.joinToString(",") { it.toCsvCell() })
}

private fun String.toCsvCell(): String {
    return "\"${replace("\"", "\"\"")}\""
}
