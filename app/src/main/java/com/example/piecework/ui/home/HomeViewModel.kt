package com.example.piecework.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.piecework.data.AppDatabase
import com.example.piecework.data.DailyWorkSummary
import com.example.piecework.data.ProductEntity
import com.example.piecework.data.WorkRecordItem
import com.example.piecework.data.WorkRepository
import com.example.piecework.data.WorkSummary
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WorkRepository

    private val mode = MutableStateFlow(HomeViewMode.Calendar)
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val currentMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<HomeUiState>

    init {
        val database = AppDatabase.getInstance(application)
        repository = WorkRepository(
            productDao = database.productDao(),
            workRecordDao = database.workRecordDao()
        )

        viewModelScope.launch {
            repository.ensureDefaultProduct()
        }

        uiState = combine(mode, selectedDate, currentMonth) { mode, date, month ->
            HomeSelection(mode, date, month)
        }.flatMapLatest { selection ->
            val baseFlow = combine(
                repository.observeProducts(),
                repository.observeDayRecords(selection.selectedDate),
                repository.observeDaySummary(selection.selectedDate),
                repository.observeMonthSummary(selection.currentMonth),
                repository.observeMonthDailySummary(selection.currentMonth)
            ) { products, dayRecords, daySummary, monthSummary, monthDailySummary ->
                HomeBaseData(
                    products = products,
                    dayRecords = dayRecords,
                    daySummary = daySummary,
                    monthSummary = monthSummary,
                    monthDailySummary = monthDailySummary
                )
            }

            baseFlow.combine(repository.observeMonthRecords(selection.currentMonth)) { base, monthRecords ->
                HomeUiState(
                    mode = selection.mode,
                    selectedDate = selection.selectedDate,
                    currentMonth = selection.currentMonth,
                    products = base.products,
                    dayRecords = base.dayRecords,
                    monthRecords = monthRecords,
                    monthDailySummary = base.monthDailySummary,
                    daySummary = base.daySummary,
                    monthSummary = base.monthSummary
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )
    }

    fun switchMode(newMode: HomeViewMode) {
        mode.value = newMode
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
        currentMonth.value = YearMonth.from(date)
    }

    fun goToPreviousMonth() {
        val month = currentMonth.value.minusMonths(1)
        currentMonth.value = month
        selectedDate.value = month.atDay(1)
    }

    fun goToNextMonth() {
        val month = currentMonth.value.plusMonths(1)
        currentMonth.value = month
        selectedDate.value = month.atDay(1)
    }

    fun addPieceRecord(productId: Long, quantity: Int, remark: String) {
        if (quantity <= 0) return

        viewModelScope.launch {
            repository.addPieceRecord(
                productId = productId,
                quantity = quantity,
                date = selectedDate.value,
                remark = remark
            )
        }
    }

    fun updatePieceRecord(recordId: Long, productId: Long, quantity: Int, remark: String) {
        if (recordId <= 0 || productId <= 0 || quantity <= 0) return

        viewModelScope.launch {
            repository.updatePieceRecord(
                recordId = recordId,
                productId = productId,
                quantity = quantity,
                remark = remark
            )
        }
    }

    fun updateProduct(productId: Long, name: String, unitPriceCents: Long) {
        val safeName = name.trim()
        if (productId <= 0 || safeName.isBlank() || unitPriceCents <= 0) return

        viewModelScope.launch {
            repository.updateProduct(
                productId = productId,
                name = safeName,
                unitPriceCents = unitPriceCents
            )
        }
    }

    fun addProduct(name: String, unitPriceCents: Long) {
        val safeName = name.trim()
        if (safeName.isBlank() || unitPriceCents <= 0) return

        viewModelScope.launch {
            repository.addProduct(
                name = safeName,
                unitPriceCents = unitPriceCents
            )
        }
    }

    fun addSubsidy(amountCents: Long, remark: String) {
        if (amountCents <= 0) return

        viewModelScope.launch {
            repository.addSubsidy(
                date = selectedDate.value,
                amountCents = amountCents,
                remark = remark
            )
        }
    }

    fun updateSubsidyRecord(recordId: Long, amountCents: Long, remark: String) {
        if (recordId <= 0 || amountCents <= 0) return

        viewModelScope.launch {
            repository.updateSubsidy(
                recordId = recordId,
                amountCents = amountCents,
                remark = remark
            )
        }
    }

    fun addDeduction(amountCents: Long, remark: String) {
        if (amountCents <= 0) return

        viewModelScope.launch {
            repository.addDeduction(
                date = selectedDate.value,
                amountCents = amountCents,
                remark = remark
            )
        }
    }

    fun updateDeductionRecord(recordId: Long, amountCents: Long, remark: String) {
        if (recordId <= 0 || amountCents <= 0) return

        viewModelScope.launch {
            repository.updateDeduction(
                recordId = recordId,
                amountCents = amountCents,
                remark = remark
            )
        }
    }

    fun deleteRecord(recordId: Long) {
        if (recordId <= 0) return

        viewModelScope.launch {
            repository.deleteRecord(recordId)
        }
    }
}

private data class HomeSelection(
    val mode: HomeViewMode,
    val selectedDate: LocalDate,
    val currentMonth: YearMonth
)

private data class HomeBaseData(
    val products: List<ProductEntity>,
    val dayRecords: List<WorkRecordItem>,
    val daySummary: WorkSummary,
    val monthSummary: WorkSummary,
    val monthDailySummary: List<DailyWorkSummary>
)
