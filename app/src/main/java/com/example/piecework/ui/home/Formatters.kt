package com.example.piecework.ui.home

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

private const val AmountScale = 1_000.0
private const val AmountInputPointShift = 3

fun Long.toYuanText(): String {
    return String.format(Locale.US, "%.3f", this / AmountScale)
}

fun Long.toSignedYuanText(): String {
    val sign = if (this >= 0) "+" else "-"
    return "$sign${kotlin.math.abs(this).toYuanText()}"
}

fun Long.toYuanShortText(): String {
    val amount = this / AmountScale
    val absolute = kotlin.math.abs(amount)
    val text = when {
        absolute >= 10_000 -> String.format(Locale.US, "%.1fw", amount / 10_000.0)
        absolute >= 1_000 -> String.format(Locale.US, "%.1fk", amount / 1_000.0)
        else -> String.format(Locale.US, "%.3f", amount)
    }
    return "¥$text"
}

fun YearMonth.formatMonthRange(): String {
    val start = atDay(1)
    val end = atEndOfMonth()
    return "%02d.%02d - %02d.%02d".format(
        start.monthValue,
        start.dayOfMonth,
        end.monthValue,
        end.dayOfMonth
    )
}

fun LocalDate.formatDayTitle(): String {
    val week = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    return "%02d.%02d %s".format(monthValue, dayOfMonth, week[dayOfWeek.value - 1])
}

fun String.toCentsOrNull(): Long? {
    return runCatching {
        BigDecimal(trim())
            .movePointRight(AmountInputPointShift)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }.getOrNull()
}
