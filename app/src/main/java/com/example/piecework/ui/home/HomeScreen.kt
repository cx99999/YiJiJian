package com.example.piecework.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.piecework.data.DailyWorkSummary
import com.example.piecework.data.WorkRecordItem
import com.example.piecework.data.WorkSummary
import com.example.piecework.ui.theme.AppBackground
import com.example.piecework.ui.theme.AppCardRadius
import com.example.piecework.ui.theme.AppOrange
import java.time.LocalDate
import java.time.YearMonth

private enum class EntryDialogType {
    Piece,
    Subsidy,
    Deduction
}

private const val CalendarDayCellHeightDp = 50
private const val RecordRowHeightDp = 64
private const val RecordNameColumnWidthDp = 120
private const val RecordQuantityColumnWidthDp = 50
private const val RecordIncomeColumnWidthDp = 92
private const val RecordChevronSizeDp = 18

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onModeChange: (HomeViewMode) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAddPieceRecord: (productId: Long, quantity: Int, remark: String) -> Unit,
    onAddProduct: (name: String, unitPriceCents: Long) -> Unit,
    onUpdateProduct: (productId: Long, name: String, unitPriceCents: Long) -> Unit,
    onUpdatePieceRecord: (recordId: Long, productId: Long, quantity: Int, remark: String) -> Unit,
    onUpdateSubsidyRecord: (recordId: Long, amountCents: Long, remark: String) -> Unit,
    onUpdateDeductionRecord: (recordId: Long, amountCents: Long, remark: String) -> Unit,
    onDeleteRecord: (recordId: Long) -> Unit,
    onAddSubsidy: (amountCents: Long, remark: String) -> Unit,
    onAddDeduction: (amountCents: Long, remark: String) -> Unit,
    onExportData: () -> Unit
) {
    var activeDialog by rememberSaveable { mutableStateOf<EntryDialogType?>(null) }
    var editingRecord by remember { mutableStateOf<WorkRecordItem?>(null) }

    Scaffold(containerColor = AppBackground) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 0.dp)
                    .padding(bottom = if (uiState.mode == HomeViewMode.Calendar) 76.dp else 0.dp)
            ) {
                DashboardHeader(
                    uiState = uiState,
                    onModeChange = onModeChange,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth
                )

                when (uiState.mode) {
                    HomeViewMode.Calendar -> {
                        CalendarMonthGrid(
                            month = uiState.currentMonth,
                            selectedDate = uiState.selectedDate,
                            dailySummary = uiState.monthDailySummary,
                            onDateSelected = { date ->
                                onDateSelected(date)
                            }
                        )

                        RecordListView(
                            selectedDate = uiState.selectedDate,
                            records = uiState.dayRecords,
                            summary = uiState.daySummary,
                            onRecordClick = { editingRecord = it },
                            modifier = Modifier
                        )
                    }

                    HomeViewMode.List -> {
                        MonthRecordListView(
                            monthRecords = uiState.monthRecords,
                            dailySummary = uiState.monthDailySummary,
                            onDateClick = { date ->
                                onDateSelected(date)
                            },
                            onRecordClick = { editingRecord = it }
                        )
                    }

                    HomeViewMode.Overview -> {
                        OverviewPage(
                            uiState = uiState,
                            onPreviousMonth = onPreviousMonth,
                            onNextMonth = onNextMonth,
                            onExportData = onExportData
                        )
                    }
                }
            }

            if (uiState.mode == HomeViewMode.Calendar) {
                ActionBar(
                    onAddSubsidy = { activeDialog = EntryDialogType.Subsidy },
                    onAddDeduction = { activeDialog = EntryDialogType.Deduction },
                    onAddRecord = { activeDialog = EntryDialogType.Piece },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    editingRecord?.let { record ->
        EditRecordDialogHost(
            record = record,
            products = uiState.products,
            onDismiss = { editingRecord = null },
            onAddProduct = onAddProduct,
            onUpdateProduct = onUpdateProduct,
            onUpdatePieceRecord = onUpdatePieceRecord,
            onUpdateSubsidyRecord = onUpdateSubsidyRecord,
            onUpdateDeductionRecord = onUpdateDeductionRecord,
            onDeleteRecord = onDeleteRecord
        )
    }

    when (activeDialog) {
        EntryDialogType.Piece -> PieceRecordDialog(
            products = uiState.products,
            onDismiss = { activeDialog = null },
            onAddProduct = onAddProduct,
            onUpdateProduct = onUpdateProduct,
            onSave = { productId, quantity, remark ->
                onAddPieceRecord(productId, quantity, remark)
                activeDialog = null
            }
        )

        EntryDialogType.Subsidy -> AmountRecordDialog(
            title = "日补贴",
            onDismiss = { activeDialog = null },
            onSave = { amountCents, remark ->
                onAddSubsidy(amountCents, remark)
                activeDialog = null
            }
        )

        EntryDialogType.Deduction -> AmountRecordDialog(
            title = "日扣款",
            onDismiss = { activeDialog = null },
            onSave = { amountCents, remark ->
                onAddDeduction(amountCents, remark)
                activeDialog = null
            }
        )

        null -> Unit
    }
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onModeChange: (HomeViewMode) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAddPieceRecord: (productId: Long, quantity: Int, remark: String) -> Unit,
    onAddProduct: (name: String, unitPriceCents: Long) -> Unit,
    onUpdateProduct: (productId: Long, name: String, unitPriceCents: Long) -> Unit,
    onUpdatePieceRecord: (recordId: Long, productId: Long, quantity: Int, remark: String) -> Unit,
    onUpdateSubsidyRecord: (recordId: Long, amountCents: Long, remark: String) -> Unit,
    onUpdateDeductionRecord: (recordId: Long, amountCents: Long, remark: String) -> Unit,
    onDeleteRecord: (recordId: Long) -> Unit,
    onAddSubsidy: (amountCents: Long, remark: String) -> Unit,
    onAddDeduction: (amountCents: Long, remark: String) -> Unit
) {
    HomeScreen(
        uiState = uiState,
        onModeChange = onModeChange,
        onDateSelected = onDateSelected,
        onPreviousMonth = onPreviousMonth,
        onNextMonth = onNextMonth,
        onAddPieceRecord = onAddPieceRecord,
        onAddProduct = onAddProduct,
        onUpdateProduct = onUpdateProduct,
        onUpdatePieceRecord = onUpdatePieceRecord,
        onUpdateSubsidyRecord = onUpdateSubsidyRecord,
        onUpdateDeductionRecord = onUpdateDeductionRecord,
        onDeleteRecord = onDeleteRecord,
        onAddSubsidy = onAddSubsidy,
        onAddDeduction = onAddDeduction,
        onExportData = {}
    )
}

@Composable
private fun EditRecordDialogHost(
    record: WorkRecordItem,
    products: List<com.example.piecework.data.ProductEntity>,
    onDismiss: () -> Unit,
    onAddProduct: (name: String, unitPriceCents: Long) -> Unit,
    onUpdateProduct: (productId: Long, name: String, unitPriceCents: Long) -> Unit,
    onUpdatePieceRecord: (recordId: Long, productId: Long, quantity: Int, remark: String) -> Unit,
    onUpdateSubsidyRecord: (recordId: Long, amountCents: Long, remark: String) -> Unit,
    onUpdateDeductionRecord: (recordId: Long, amountCents: Long, remark: String) -> Unit,
    onDeleteRecord: (recordId: Long) -> Unit
) {
    when {
        record.productId != null -> EditPieceRecordDialog(
            record = record,
            products = products,
            onDismiss = onDismiss,
            onAddProduct = onAddProduct,
            onUpdateProduct = onUpdateProduct,
            onSave = { productId, quantity, remark ->
                onUpdatePieceRecord(record.recordId, productId, quantity, remark)
                onDismiss()
            },
            onDelete = {
                onDeleteRecord(record.recordId)
                onDismiss()
            }
        )

        record.subsidyCents > 0 -> EditAmountRecordDialog(
            title = "编辑日补贴",
            record = record,
            onDismiss = onDismiss,
            onSave = { amountCents, remark ->
                onUpdateSubsidyRecord(record.recordId, amountCents, remark)
                onDismiss()
            },
            onDelete = {
                onDeleteRecord(record.recordId)
                onDismiss()
            }
        )

        else -> EditAmountRecordDialog(
            title = "编辑日扣款",
            record = record,
            onDismiss = onDismiss,
            onSave = { amountCents, remark ->
                onUpdateDeductionRecord(record.recordId, amountCents, remark)
                onDismiss()
            },
            onDelete = {
                onDeleteRecord(record.recordId)
                onDismiss()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardHeader(
    uiState: HomeUiState,
    onModeChange: (HomeViewMode) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = uiState.mode == HomeViewMode.Calendar,
                    onClick = { onModeChange(HomeViewMode.Calendar) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("日历", fontSize = 13.sp)
                }

                SegmentedButton(
                    selected = uiState.mode == HomeViewMode.List,
                    onClick = { onModeChange(HomeViewMode.List) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("列表", fontSize = 13.sp)
                }

                SegmentedButton(
                    selected = uiState.mode == HomeViewMode.Overview,
                    onClick = { onModeChange(HomeViewMode.Overview) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("总览", fontSize = 13.sp)
                }
            }

            if (uiState.mode != HomeViewMode.Overview) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MonthNavButton(onClick = onPreviousMonth, isPrevious = true)

                    Text(
                        text = uiState.currentMonth.formatMonthRange(),
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .width(104.dp),
                        color = Color(0xFF333333),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    MonthNavButton(onClick = onNextMonth, isPrevious = false)
                }
            }
        }

        if (uiState.mode != HomeViewMode.Overview) {
            SummaryCard(
                totalCount = uiState.monthSummary.totalQuantity,
                pieceIncomeCents = uiState.monthSummary.pieceIncomeCents,
                monthIncomeCents = uiState.monthSummary.finalIncomeCents
            )
        }
    }
}

@Composable
private fun OverviewPage(
    uiState: HomeUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onExportData: () -> Unit
) {
    val productStats = remember(uiState.monthRecords) {
        buildProductStats(uiState.monthRecords)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OverviewRangeRow(
            month = uiState.currentMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onExportData = onExportData
        )

        OverviewSummaryCard(summary = uiState.monthSummary)

        ProductStatsCard(items = productStats)
    }
}

@Composable
private fun OverviewRangeRow(
    month: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onExportData: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Assessment,
                contentDescription = null,
                tint = Color(0xFF333333),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = "统计",
                color = Color(0xFF333333),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            MonthNavButton(onClick = onPreviousMonth, isPrevious = true)
            Text(
                text = month.formatMonthRange(),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(112.dp),
                color = Color(0xFF333333),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            MonthNavButton(onClick = onNextMonth, isPrevious = false)
        }

        Button(
            onClick = onExportData,
            colors = ButtonDefaults.buttonColors(containerColor = AppOrange),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 7.dp)
        ) {
            Text("导出", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OverviewSummaryCard(
    summary: WorkSummary
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(
                text = "总览",
                color = AppOrange,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewMetric(
                    value = summary.finalIncomeCents.toYuanText(),
                    label = "月总收入(元)"
                )
                OverviewMetric(
                    value = (summary.subsidyCents - summary.deductionCents).toYuanText(),
                    label = "补贴扣款(元)",
                    alignEnd = true
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewMetric(
                    value = summary.pieceIncomeCents.toYuanText(),
                    label = "计件收入(元)"
                )
                OverviewMetric(
                    value = summary.totalQuantity.toString(),
                    label = "计件数量(件)",
                    alignEnd = true
                )
            }
        }
    }
}

@Composable
private fun OverviewMetric(
    value: String,
    label: String,
    alignEnd: Boolean = false
) {
    Column(
        modifier = Modifier.width(142.dp),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(
            text = value,
            color = Color(0xFF333333),
            fontSize = 17.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            color = Color(0xFF777777),
            fontSize = 12.sp,
            lineHeight = 15.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun ProductStatsCard(
    items: List<ProductStatItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "产品统计",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                color = AppOrange,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            if (items.isEmpty()) {
                Text(
                    text = "暂无产品数据",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    color = Color(0xFF999999),
                    fontSize = 13.sp
                )
            } else {
                items.forEachIndexed { index, item ->
                    ProductStatRow(item = item)
                    if (index < items.lastIndex) {
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductStatRow(
    item: ProductStatItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.width(142.dp)) {
            Text(
                text = item.productName,
                color = Color(0xFF333333),
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "单价:${item.unitPriceCents.toYuanText()}",
                color = Color(0xFF777777),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 1
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.totalQuantity.toString(),
                color = AppOrange,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "¥${item.pieceIncomeCents.toYuanText()}",
                color = Color(0xFF777777),
                fontSize = 13.sp,
                lineHeight = 16.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MonthNavButton(
    onClick: () -> Unit,
    isPrevious: Boolean
) {
    Box(
        modifier = Modifier
            .requiredSize(24.dp)
            .clip(CircleShape)
            .background(AppOrange)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPrevious) Icons.Default.KeyboardArrowLeft else Icons.Default.KeyboardArrowRight,
            contentDescription = if (isPrevious) "上个月" else "下个月",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SummaryCard(
    totalCount: Long,
    pieceIncomeCents: Long,
    monthIncomeCents: Long
) {
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCardRadius))
            .background(AppOrange)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SummaryItem(value = totalCount.toString(), label = "计件总数")
        SummaryItem(value = pieceIncomeCents.toYuanText(), label = "计件收入")
        SummaryItem(value = monthIncomeCents.toYuanText(), label = "月总收入")
    }
}

@Composable
private fun SummaryItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CalendarMonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    dailySummary: List<DailyWorkSummary>,
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit
) {
    val summaryMap = remember(dailySummary) {
        dailySummary.associateBy { it.dateEpochDay }
    }
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startBlank = firstDay.dayOfWeek.value - 1
    val occupiedCells = startBlank + daysInMonth
    val totalCells = ((occupiedCells + 6) / 7) * 7
    val cells = remember(month) {
        List(startBlank) { null } +
            (1..daysInMonth).map { month.atDay(it) } +
            List(totalCells - occupiedCells) { null }
    }
    val rows = totalCells / 7

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp),
            userScrollEnabled = false
        ) {
            items(listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")) {
                Text(
                    text = it,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF777777),
                    fontSize = 13.sp
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height((rows * CalendarDayCellHeightDp + 8 + (rows - 1) * 4).dp),
            userScrollEnabled = false,
            contentPadding = PaddingValues(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(cells) { date ->
                if (date == null) {
                    Box(Modifier.height(CalendarDayCellHeightDp.dp))
                } else {
                    val summary = summaryMap[date.toEpochDay()]
                    CalendarDayCell(
                        date = date,
                        selected = date == selectedDate,
                        hasRecord = summary != null,
                        amountCents = summary?.finalIncomeCents,
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    selected: Boolean,
    hasRecord: Boolean,
    amountCents: Long?,
    onClick: () -> Unit
) {
    val background = if (selected) AppOrange else Color.White
    val foreground = if (selected) Color.White else Color(0xFF333333)

    Column(
        modifier = Modifier
            .height(CalendarDayCellHeightDp.dp)
            .fillMaxWidth()
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = foreground,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold
        )

        if (hasRecord) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = amountCents?.toYuanShortText().orEmpty(),
                color = foreground,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecordListView(
    selectedDate: LocalDate,
    records: List<WorkRecordItem>,
    summary: WorkSummary,
    onRecordClick: (WorkRecordItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(AppCardRadius),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            RecordSectionHeader(
                title = selectedDate.formatDayTitle(),
                totalQuantity = summary.totalQuantity,
                finalIncomeCents = summary.finalIncomeCents,
                subsidyCents = summary.subsidyCents,
                deductionCents = summary.deductionCents
            )

            if (records.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFFE7E7E7))
                RecordDetailTable(
                    records = records,
                    onRecordClick = onRecordClick
                )
            }
        }
    }
}

@Composable
private fun RecordDetailTable(
    records: List<WorkRecordItem>,
    onRecordClick: (WorkRecordItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        RecordTableHeader()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 220.dp)
                .verticalScroll(rememberScrollState())
        ) {
            records.forEachIndexed { index, record ->
                RecordRow(record = record, onClick = { onRecordClick(record) })
                if (index < records.lastIndex) {
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }
            }
        }
    }
}

@Composable
private fun RecordTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color(0xFFF4F7FB))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "产品/项目",
            modifier = Modifier.width(RecordNameColumnWidthDp.dp),
            color = Color(0xFF777777),
            fontSize = 12.sp,
            maxLines = 1
        )
        Text(
            text = "数量",
            modifier = Modifier.width(RecordQuantityColumnWidthDp.dp),
            color = Color(0xFF777777),
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            maxLines = 1
        )
        Text(
            text = "收入",
            modifier = Modifier.width(RecordIncomeColumnWidthDp.dp),
            color = Color(0xFF777777),
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            maxLines = 1
        )
        Spacer(Modifier.width(RecordChevronSizeDp.dp))
    }
}

@Composable
private fun MonthRecordListView(
    monthRecords: List<WorkRecordItem>,
    dailySummary: List<DailyWorkSummary>,
    onDateClick: (LocalDate) -> Unit,
    onRecordClick: (WorkRecordItem) -> Unit
) {
    val summaryMap = remember(dailySummary) {
        dailySummary.associateBy { it.dateEpochDay }
    }
    val groups = remember(monthRecords) {
        monthRecords.groupBy { it.dateEpochDay }
            .toList()
            .sortedByDescending { it.first }
            .map { (epochDay, records) ->
                epochDay to records.sortedByDescending { it.recordId }
            }
    }

    if (groups.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(AppCardRadius),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            EmptyListHint()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(groups) { (epochDay, records) ->
            val date = LocalDate.ofEpochDay(epochDay)
            DailyRecordSection(
                date = date,
                records = records,
                summary = summaryMap[epochDay],
                onHeaderClick = { onDateClick(date) },
                onRecordClick = onRecordClick
            )
        }
    }
}

@Composable
private fun DailyRecordSection(
    date: LocalDate,
    records: List<WorkRecordItem>,
    summary: DailyWorkSummary?,
    onHeaderClick: () -> Unit,
    onRecordClick: (WorkRecordItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardRadius),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        RecordSectionHeader(
            title = date.formatDayTitle(),
            totalQuantity = summary?.totalQuantity ?: 0,
            finalIncomeCents = summary?.finalIncomeCents ?: 0,
            subsidyCents = summary?.subsidyCents ?: 0,
            deductionCents = summary?.deductionCents ?: 0,
            onClick = onHeaderClick
        )
        HorizontalDivider(color = Color(0xFFE7E7E7))
        records.forEach { record ->
            RecordRow(record = record, onClick = { onRecordClick(record) })
        }
    }
}

@Composable
private fun RecordSectionHeader(
    title: String,
    totalQuantity: Long,
    finalIncomeCents: Long,
    subsidyCents: Long,
    deductionCents: Long,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .heightIn(min = 70.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            modifier = Modifier.widthIn(max = 126.dp),
            color = Color(0xFF333333),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "数量：$totalQuantity  金额：${finalIncomeCents.toYuanText()}",
                color = Color(0xFF333333),
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = "补贴：${subsidyCents.toYuanText()}  扣款：${deductionCents.toYuanText()}",
                color = Color(0xFF777777),
                fontSize = 12.sp,
                lineHeight = 15.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecordRow(
    record: WorkRecordItem,
    onClick: () -> Unit
) {
    val isPieceRecord = record.productId != null
    val title = when {
        isPieceRecord -> record.productName ?: "产品"
        record.subsidyCents > 0 -> "日补贴"
        record.deductionCents > 0 -> "日扣款"
        else -> "记录"
    }
    val detail = when {
        isPieceRecord -> "单价:${record.unitPriceCents?.toYuanText() ?: "-"}"
        record.remark.isNotBlank() -> record.remark
        else -> "金额调整"
    }
    val quantityText = if (isPieceRecord) {
        record.quantity.toString()
    } else {
        ""
    }
    val incomeText = when {
        record.subsidyCents > 0 -> record.subsidyCents.toSignedYuanText()
        record.deductionCents > 0 -> (-record.deductionCents).toSignedYuanText()
        else -> record.finalIncomeCents.toYuanText()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(RecordRowHeightDp.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.width(RecordNameColumnWidthDp.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF333333),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detail,
                color = Color(0xFF777777),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = quantityText,
            modifier = Modifier.width(RecordQuantityColumnWidthDp.dp),
            color = AppOrange,
            fontSize = 13.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = incomeText,
            modifier = Modifier.width(RecordIncomeColumnWidthDp.dp),
            color = if (incomeText.startsWith("-")) Color(0xFFE64646) else AppOrange,
            fontSize = 13.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF999999),
            modifier = Modifier.size(RecordChevronSizeDp.dp)
        )
    }
}

@Composable
private fun EmptyListHint(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "暂无计件记录",
            color = Color(0xFF999999),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ActionBar(
    onAddSubsidy: () -> Unit,
    onAddDeduction: () -> Unit,
    onAddRecord: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(AppCardRadius),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Button(
                    onClick = onAddSubsidy,
                    colors = ButtonDefaults.buttonColors(containerColor = AppOrange),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("日补贴", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = onAddDeduction,
                    colors = ButtonDefaults.buttonColors(containerColor = AppOrange),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("日扣款", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            ExtendedFloatingActionButton(
                onClick = onAddRecord,
                containerColor = AppOrange,
                contentColor = Color.White,
                icon = {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(22.dp))
                },
                text = { Text("记一笔", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            )
        }
    }
}
