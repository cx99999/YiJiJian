package com.example.piecework

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.piecework.ui.home.buildExportCsv
import com.example.piecework.ui.home.buildProductStats
import com.example.piecework.ui.home.HomeScreen
import com.example.piecework.ui.home.HomeViewModel
import com.example.piecework.ui.theme.PieceworkTheme
import java.io.File

class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PieceworkTheme {
                val uiState = viewModel.uiState.collectAsStateWithLifecycle()

                HomeScreen(
                    uiState = uiState.value,
                    onModeChange = viewModel::switchMode,
                    onDateSelected = viewModel::selectDate,
                    onPreviousMonth = viewModel::goToPreviousMonth,
                    onNextMonth = viewModel::goToNextMonth,
                    onAddPieceRecord = viewModel::addPieceRecord,
                    onAddProduct = viewModel::addProduct,
                    onUpdateProduct = viewModel::updateProduct,
                    onUpdatePieceRecord = viewModel::updatePieceRecord,
                    onUpdateSubsidyRecord = viewModel::updateSubsidyRecord,
                    onUpdateDeductionRecord = viewModel::updateDeductionRecord,
                    onDeleteRecord = viewModel::deleteRecord,
                    onAddSubsidy = viewModel::addSubsidy,
                    onAddDeduction = viewModel::addDeduction,
                    onExportData = {
                        exportCurrentMonth(uiState.value)
                    }
                )
            }
        }
    }

    private fun exportCurrentMonth(uiState: com.example.piecework.ui.home.HomeUiState) {
        runCatching {
            val productStats = buildProductStats(uiState.monthRecords)
            val csv = buildExportCsv(
                month = uiState.currentMonth,
                records = uiState.monthRecords,
                summary = uiState.monthSummary,
                productStats = productStats
            )
            val exportDir = File(cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, "易计件-${uiState.currentMonth}.csv")
            file.writeText(csv, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "易计件数据导出")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "导出数据")
            if (sendIntent.resolveActivity(packageManager) != null) {
                startActivity(chooser)
            } else {
                Toast.makeText(this, "没有找到可用的导出应用", Toast.LENGTH_SHORT).show()
            }
        }.onFailure {
            Toast.makeText(this, "导出失败，请重试", Toast.LENGTH_SHORT).show()
        }
    }
}
