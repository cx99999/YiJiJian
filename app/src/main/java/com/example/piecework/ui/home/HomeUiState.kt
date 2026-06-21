package com.example.piecework.ui.home

import com.example.piecework.data.DailyWorkSummary
import com.example.piecework.data.ProductEntity
import com.example.piecework.data.WorkRecordItem
import com.example.piecework.data.WorkSummary
import java.time.LocalDate
import java.time.YearMonth

enum class HomeViewMode {
    Calendar,
    List,
    Overview
}

data class HomeUiState(
    val mode: HomeViewMode = HomeViewMode.Calendar,
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val products: List<ProductEntity> = emptyList(),
    val dayRecords: List<WorkRecordItem> = emptyList(),
    val monthRecords: List<WorkRecordItem> = emptyList(),
    val monthDailySummary: List<DailyWorkSummary> = emptyList(),
    val daySummary: WorkSummary = WorkSummary.Empty,
    val monthSummary: WorkSummary = WorkSummary.Empty
)
