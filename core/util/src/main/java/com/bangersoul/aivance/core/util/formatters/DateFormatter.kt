package com.bangersoul.aivance.core.util.formatters

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    private val defaultFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())

    fun formatDate(date: LocalDateTime): String {
        return date.format(defaultFormatter)
    }

    fun formatCustom(date: LocalDateTime, pattern: String): String {
        return date.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }
}
