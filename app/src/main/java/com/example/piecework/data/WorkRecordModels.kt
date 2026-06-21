package com.example.piecework.data

data class WorkRecordItem(
    val recordId: Long,
    val productId: Long?,
    val productName: String?,
    val unitPriceCents: Long?,
    val quantity: Int,
    val dateEpochDay: Long,
    val remark: String,
    val subsidyCents: Long,
    val deductionCents: Long,
    val pieceIncomeCents: Long,
    val finalIncomeCents: Long
)

data class WorkSummary(
    val totalQuantity: Long,
    val pieceIncomeCents: Long,
    val subsidyCents: Long,
    val deductionCents: Long,
    val finalIncomeCents: Long
) {
    companion object {
        val Empty = WorkSummary(
            totalQuantity = 0,
            pieceIncomeCents = 0,
            subsidyCents = 0,
            deductionCents = 0,
            finalIncomeCents = 0
        )
    }
}

data class DailyWorkSummary(
    val dateEpochDay: Long,
    val totalQuantity: Long,
    val pieceIncomeCents: Long,
    val subsidyCents: Long,
    val deductionCents: Long,
    val finalIncomeCents: Long,
    val recordCount: Long
)
