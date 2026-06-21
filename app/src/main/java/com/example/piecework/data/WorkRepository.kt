package com.example.piecework.data

import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

class WorkRepository(
    private val productDao: ProductDao,
    private val workRecordDao: WorkRecordDao
) {
    fun observeProducts(): Flow<List<ProductEntity>> {
        return productDao.observeProducts()
    }

    fun observeDayRecords(date: LocalDate): Flow<List<WorkRecordItem>> {
        return workRecordDao.observeRecordsByDate(date.toEpochDay())
    }

    fun observeMonthRecords(month: YearMonth): Flow<List<WorkRecordItem>> {
        val start = month.atDay(1).toEpochDay()
        val end = month.plusMonths(1).atDay(1).toEpochDay()
        return workRecordDao.observeRecordsBetween(start, end)
    }

    fun observeDaySummary(date: LocalDate): Flow<WorkSummary> {
        val start = date.toEpochDay()
        return workRecordDao.observeSummaryBetween(start, start + 1)
    }

    fun observeMonthSummary(month: YearMonth): Flow<WorkSummary> {
        val start = month.atDay(1).toEpochDay()
        val end = month.plusMonths(1).atDay(1).toEpochDay()
        return workRecordDao.observeSummaryBetween(start, end)
    }

    fun observeMonthDailySummary(month: YearMonth): Flow<List<DailyWorkSummary>> {
        val start = month.atDay(1).toEpochDay()
        val end = month.plusMonths(1).atDay(1).toEpochDay()
        return workRecordDao.observeDailySummaryBetween(start, end)
    }

    suspend fun ensureDefaultProduct() {
        if (productDao.countProducts() == 0) {
            productDao.upsert(
                ProductEntity(
                    name = "产品1",
                    unitPriceCents = 14_000
                )
            )
        }
    }

    suspend fun updateProduct(
        productId: Long,
        name: String,
        unitPriceCents: Long
    ) {
        productDao.updateProduct(
            productId = productId,
            name = name,
            unitPriceCents = unitPriceCents
        )
    }

    suspend fun addProduct(
        name: String,
        unitPriceCents: Long
    ): Long {
        return productDao.upsert(
            ProductEntity(
                name = name,
                unitPriceCents = unitPriceCents
            )
        )
    }

    suspend fun addPieceRecord(
        productId: Long,
        quantity: Int,
        date: LocalDate,
        remark: String = ""
    ) {
        workRecordDao.insert(
            WorkRecordEntity(
                productId = productId,
                quantity = quantity,
                dateEpochDay = date.toEpochDay(),
                remark = remark,
                recordType = RecordTypes.Piece
            )
        )
    }

    suspend fun updatePieceRecord(
        recordId: Long,
        productId: Long,
        quantity: Int,
        remark: String
    ) {
        workRecordDao.updatePieceRecord(
            recordId = recordId,
            productId = productId,
            quantity = quantity,
            remark = remark
        )
    }

    suspend fun addSubsidy(
        date: LocalDate,
        amountCents: Long,
        remark: String = ""
    ) {
        workRecordDao.insert(
            WorkRecordEntity(
                productId = null,
                quantity = 0,
                dateEpochDay = date.toEpochDay(),
                subsidyCents = amountCents,
                remark = remark,
                recordType = RecordTypes.Subsidy
            )
        )
    }

    suspend fun updateSubsidy(
        recordId: Long,
        amountCents: Long,
        remark: String
    ) {
        workRecordDao.updateAdjustmentRecord(
            recordId = recordId,
            subsidyCents = amountCents,
            deductionCents = 0,
            remark = remark,
            recordType = RecordTypes.Subsidy
        )
    }

    suspend fun addDeduction(
        date: LocalDate,
        amountCents: Long,
        remark: String = ""
    ) {
        workRecordDao.insert(
            WorkRecordEntity(
                productId = null,
                quantity = 0,
                dateEpochDay = date.toEpochDay(),
                deductionCents = amountCents,
                remark = remark,
                recordType = RecordTypes.Deduction
            )
        )
    }

    suspend fun updateDeduction(
        recordId: Long,
        amountCents: Long,
        remark: String
    ) {
        workRecordDao.updateAdjustmentRecord(
            recordId = recordId,
            subsidyCents = 0,
            deductionCents = amountCents,
            remark = remark,
            recordType = RecordTypes.Deduction
        )
    }

    suspend fun deleteRecord(recordId: Long) {
        workRecordDao.deleteById(recordId)
    }
}
