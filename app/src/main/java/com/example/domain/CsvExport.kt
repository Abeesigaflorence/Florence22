package com.example.domain

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToLong

object CsvExport {

    fun generateCsv(
        series: List<Finance.MonthPoint>,
        forecastReport: ForecastReport
    ): String {
        val todayStr = LocalDate.now(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.UK))

        val sb = StringBuilder()
        sb.append("\"Royo Farm — 6 month report\",\"\",\"\",\"\"\n")
        sb.append("\"Generated $todayStr\",\"\",\"\",\"\"\n")
        sb.append(",,,\n")
        sb.append("\"Month\",\"Revenue (UGX)\",\"Expenses (UGX)\",\"Profit (UGX)\"\n")

        for (pt in series) {
            val rev = pt.revenue.roundToLong()
            val exp = pt.expenses.roundToLong()
            val prof = pt.profit.roundToLong()
            sb.append("\"${pt.full}\",\"$rev\",\"$exp\",\"$prof\"\n")
        }

        sb.append(",,,\n")
        sb.append("\"Forecast\",\"Revenue (UGX)\",\"Expenses (UGX)\",\"Profit (UGX)\"\n")

        for (pm in forecastReport.projected.take(3)) {
            val rev = pm.revenue.roundToLong()
            val exp = pm.expenses.roundToLong()
            val prof = pm.profit.roundToLong()
            sb.append("\"${pm.full}\",\"$rev\",\"$exp\",\"$prof\"\n")
        }

        return sb.toString()
    }

    fun exportAndShare(
        context: Context,
        series: List<Finance.MonthPoint>,
        forecastReport: ForecastReport
    ): Boolean {
        return runCatching {
            val csvData = generateCsv(series, forecastReport)
            val file = File(context.cacheDir, "royo-farm-report.csv")
            file.writeText(csvData)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Royo Farm report")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Share Royo Farm Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        }.getOrDefault(false)
    }
}
