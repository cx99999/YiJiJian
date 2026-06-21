package com.example.piecework.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object RecordTypes {
    const val Piece = "PIECE"
    const val Subsidy = "SUBSIDY"
    const val Deduction = "DEDUCTION"
}

@Entity(
    tableName = "work_records",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("productId"),
        Index("dateEpochDay"),
        Index(value = ["dateEpochDay", "productId"])
    ]
)
data class WorkRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long?,
    val quantity: Int = 0,
    val dateEpochDay: Long,
    val remark: String = "",
    val subsidyCents: Long = 0,
    val deductionCents: Long = 0,
    val recordType: String = RecordTypes.Piece,
    val createdAtMillis: Long = System.currentTimeMillis()
)
