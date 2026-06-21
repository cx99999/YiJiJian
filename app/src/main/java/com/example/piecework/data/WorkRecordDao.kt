package com.example.piecework.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkRecordDao {
    @Insert
    suspend fun insert(record: WorkRecordEntity): Long

    @Update
    suspend fun update(record: WorkRecordEntity)

    @Delete
    suspend fun delete(record: WorkRecordEntity)

    @Query("""
        UPDATE work_records
        SET productId = :productId,
            quantity = :quantity,
            remark = :remark,
            subsidyCents = 0,
            deductionCents = 0,
            recordType = :recordType
        WHERE id = :recordId
    """)
    suspend fun updatePieceRecord(
        recordId: Long,
        productId: Long,
        quantity: Int,
        remark: String,
        recordType: String = RecordTypes.Piece
    )

    @Query("""
        UPDATE work_records
        SET quantity = 0,
            remark = :remark,
            subsidyCents = :subsidyCents,
            deductionCents = :deductionCents,
            recordType = :recordType
        WHERE id = :recordId
    """)
    suspend fun updateAdjustmentRecord(
        recordId: Long,
        subsidyCents: Long,
        deductionCents: Long,
        remark: String,
        recordType: String
    )

    @Query("DELETE FROM work_records WHERE id = :recordId")
    suspend fun deleteById(recordId: Long)

    @Query("""
        SELECT
            r.id AS recordId,
            r.productId AS productId,
            p.name AS productName,
            p.unitPriceCents AS unitPriceCents,
            r.quantity AS quantity,
            r.dateEpochDay AS dateEpochDay,
            r.remark AS remark,
            r.subsidyCents AS subsidyCents,
            r.deductionCents AS deductionCents,
            COALESCE(r.quantity * p.unitPriceCents, 0) AS pieceIncomeCents,
            COALESCE(r.quantity * p.unitPriceCents, 0) + r.subsidyCents - r.deductionCents AS finalIncomeCents
        FROM work_records r
        LEFT JOIN products p ON p.id = r.productId
        WHERE r.dateEpochDay = :dateEpochDay
        ORDER BY r.createdAtMillis DESC
    """)
    fun observeRecordsByDate(dateEpochDay: Long): Flow<List<WorkRecordItem>>

    @Query("""
        SELECT
            r.id AS recordId,
            r.productId AS productId,
            p.name AS productName,
            p.unitPriceCents AS unitPriceCents,
            r.quantity AS quantity,
            r.dateEpochDay AS dateEpochDay,
            r.remark AS remark,
            r.subsidyCents AS subsidyCents,
            r.deductionCents AS deductionCents,
            COALESCE(r.quantity * p.unitPriceCents, 0) AS pieceIncomeCents,
            COALESCE(r.quantity * p.unitPriceCents, 0) + r.subsidyCents - r.deductionCents AS finalIncomeCents
        FROM work_records r
        LEFT JOIN products p ON p.id = r.productId
        WHERE r.dateEpochDay >= :startEpochDay
          AND r.dateEpochDay < :endEpochDay
        ORDER BY r.dateEpochDay DESC, r.createdAtMillis DESC
    """)
    fun observeRecordsBetween(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<WorkRecordItem>>

    @Query("""
        SELECT
            COALESCE(SUM(r.quantity), 0) AS totalQuantity,
            COALESCE(SUM(r.quantity * p.unitPriceCents), 0) AS pieceIncomeCents,
            COALESCE(SUM(r.subsidyCents), 0) AS subsidyCents,
            COALESCE(SUM(r.deductionCents), 0) AS deductionCents,
            COALESCE(SUM(r.quantity * p.unitPriceCents), 0)
                + COALESCE(SUM(r.subsidyCents), 0)
                - COALESCE(SUM(r.deductionCents), 0) AS finalIncomeCents
        FROM work_records r
        LEFT JOIN products p ON p.id = r.productId
        WHERE r.dateEpochDay >= :startEpochDay
          AND r.dateEpochDay < :endEpochDay
    """)
    fun observeSummaryBetween(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<WorkSummary>

    @Query("""
        SELECT
            r.dateEpochDay AS dateEpochDay,
            COALESCE(SUM(r.quantity), 0) AS totalQuantity,
            COALESCE(SUM(r.quantity * p.unitPriceCents), 0) AS pieceIncomeCents,
            COALESCE(SUM(r.subsidyCents), 0) AS subsidyCents,
            COALESCE(SUM(r.deductionCents), 0) AS deductionCents,
            COALESCE(SUM(r.quantity * p.unitPriceCents), 0)
                + COALESCE(SUM(r.subsidyCents), 0)
                - COALESCE(SUM(r.deductionCents), 0) AS finalIncomeCents,
            COUNT(r.id) AS recordCount
        FROM work_records r
        LEFT JOIN products p ON p.id = r.productId
        WHERE r.dateEpochDay >= :startEpochDay
          AND r.dateEpochDay < :endEpochDay
        GROUP BY r.dateEpochDay
        ORDER BY r.dateEpochDay
    """)
    fun observeDailySummaryBetween(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<DailyWorkSummary>>
}
