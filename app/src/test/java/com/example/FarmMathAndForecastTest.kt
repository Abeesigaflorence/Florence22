package com.example

import com.example.data.model.InventoryItem
import com.example.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class FarmMathAndForecastTest {

    @Test
    fun testFitAndForecastMonotonic() {
        // Known increasing series: 1,000,000 to 2,000,000 across 6 months
        val revs = listOf(1_000_000.0, 1_200_000.0, 1_400_000.0, 1_600_000.0, 1_800_000.0, 2_000_000.0)
        val fitResult = fit(revs)

        assertTrue("Slope should be positive", fitResult.slope > 0)
        assertTrue("R2 should be close to 1.0", fitResult.r2 >= 0.99)

        val series = revs.mapIndexed { i, rev ->
            Finance.MonthPoint(
                key = "2026-0${i + 1}",
                label = "M${i + 1}",
                full = "Month ${i + 1}",
                revenue = rev,
                expenses = rev * 0.6
            )
        }

        val forecastReport = forecast(series, horizon = 3, today = YearMonth.of(2026, 6))

        assertEquals(3, forecastReport.projected.size)
        assertEquals("up", forecastReport.direction)

        // Monotonic increase in projected revenue
        assertTrue(forecastReport.projected[0].revenue < forecastReport.projected[1].revenue)
        assertTrue(forecastReport.projected[1].revenue < forecastReport.projected[2].revenue)

        // Monotonic increase in projected profit
        assertTrue(forecastReport.projected[0].profit < forecastReport.projected[1].profit)
        assertTrue(forecastReport.projected[1].profit < forecastReport.projected[2].profit)
    }

    @Test
    fun testFarmMath() {
        val survival = survivalPct(500.0, 485.0)
        assertEquals(97.0, survival, 0.01)

        val age = birdAgeDays("2026-11-01", LocalDate.parse("2026-11-03"))
        assertEquals(2, age)

        val item1 = InventoryItem(quantity = 5.0, reorderLevel = 10.0)
        assertTrue(isLowStock(item1))

        val item2 = InventoryItem(quantity = 15.0, reorderLevel = 10.0)
        assertFalse(isLowStock(item2))
    }

    @Test
    fun testCsvExportFormat() {
        val series = listOf(
            Finance.MonthPoint(
                key = "2026-11",
                label = "Nov",
                full = "November 2026",
                revenue = 1500000.0,
                expenses = 800000.0
            )
        )
        val forecastReport = forecast(series, horizon = 1, today = YearMonth.of(2026, 11))
        val csv = CsvExport.generateCsv(series, forecastReport)

        assertTrue(csv.contains("Royo Farm — 6 month report"))
        assertTrue(csv.contains("November 2026"))
        assertTrue(csv.contains("Forecast"))
    }
}
