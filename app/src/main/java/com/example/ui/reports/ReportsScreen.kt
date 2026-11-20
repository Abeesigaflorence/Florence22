package com.example.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.data.model.Flock
import com.example.data.model.ProductionRecord
import com.example.data.model.Sale
import com.example.domain.*
import com.example.ui.charts.CostsByCategoryBarChart
import com.example.ui.charts.SimpleLineTrendChart
import com.example.ui.common.PageHeader
import com.example.ui.common.StatCard
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun ReportsScreen(
    flocks: List<Flock>,
    productionRecords: List<ProductionRecord>,
    sales: List<Sale>,
    expenses: List<Expense>,
    onFlockClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }

    val series = remember(sales, expenses) {
        Finance.monthlySeries(sales, expenses)
    }

    val forecast3m = remember(series) {
        forecast(series, horizon = 3)
    }

    val totalRev6m = series.sumOf { it.revenue }
    val totalExp6m = series.sumOf { it.expenses }
    val netProfit6m = totalRev6m - totalExp6m
    val avgProfit = if (series.isNotEmpty()) netProfit6m / series.size else 0.0

    // Costs last 90 days
    val ninetyDaysAgo = LocalDate.now(ZoneOffset.UTC).minusDays(90).toString()
    val costs90d = expenses.filter { it.date >= ninetyDaysAgo }
        .groupBy { it.category }
        .map { (cat, list) -> labelOf(EXPENSE_CATEGORIES, cat) to list.sumOf { it.amount } }
        .sortedByDescending { it.second }

    // Eggs per month (6 months)
    val eggsByMonth = series.map { pt ->
        val monthEggs = productionRecords.filter { it.date.startsWith(pt.key) }
            .sumOf { it.eggsCollected }
        pt.label to monthEggs
    }
    val totalEggs6m = eggsByMonth.sumOf { it.second }

    // Profit by flock sorted by profit desc
    val flockProfits = flocks.map { flock ->
        val money = flockMoney(flock.id, sales, expenses)
        Triple(flock, money, money.profit)
    }.sortedByDescending { it.third }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PageHeader(
            eyebrow = "Farm insights",
            title = "Reports",
            description = "Six months of how the farm performed, plus a look ahead at the next three.",
            actionButton = {
                Button(
                    onClick = {
                        isExporting = true
                        CsvExport.exportAndShare(context, series, forecast3m)
                        isExporting = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenYellow,
                        contentColor = DeepEspresso
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isExporting
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DeepEspresso, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )

        // 4 Stats
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "6-month sales",
                    value = money(totalRev6m, compact = true),
                    subtitle = "Money brought in",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "6-month costs",
                    value = money(totalExp6m, compact = true),
                    subtitle = "Money spent",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Net profit (6m)",
                    value = money(netProfit6m, compact = true),
                    subtitle = "Overall balance",
                    isDestructive = netProfit6m < 0,
                    isAccent = netProfit6m >= 0,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Avg monthly profit",
                    value = money(avgProfit, compact = true),
                    subtitle = "${series.size} months averaged",
                    isDestructive = avgProfit < 0,
                    isAccent = avgProfit > 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 1. Revenue vs Expenses 6-Row P&L Table
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "MONEY IN VS MONEY OUT (6 MONTHS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceButter, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text("Month", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall, color = DeepEspresso, fontWeight = FontWeight.Bold)
                    Text("Sales", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = DeepEspresso, fontWeight = FontWeight.Bold)
                    Text("Costs", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = DeepEspresso, fontWeight = FontWeight.Bold)
                    Text("Profit", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = DeepEspresso, fontWeight = FontWeight.Bold)
                    Text("Margin", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelSmall, color = DeepEspresso, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                series.forEach { pt ->
                    val margin = if (pt.revenue > 0) (pt.profit / pt.revenue) * 100.0 else 0.0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pt.label, modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(money(pt.revenue, compact = true), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        Text(money(pt.expenses, compact = true), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        Text(
                            money(pt.profit, compact = true),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (pt.profit < 0) BrickRed else SuccessGreen
                        )
                        Text(pct(margin, 0), modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.bodySmall, color = MutedText)
                    }
                    HorizontalDivider(color = SurfaceButter)
                }
            }
        }

        // 2. Forecast Card + 3 Months Projection Table
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GoldenYellowDark.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Surface(
                    color = GoldenYellow,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "LOOKING AHEAD · NEXT 3 MONTHS",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepEspresso,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Forecast Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceButter, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text("Month", modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelSmall, color = DeepEspresso, fontWeight = FontWeight.Bold)
                    Text("Exp. Sales", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = DeepEspresso, fontWeight = FontWeight.Bold)
                    Text("Exp. Costs", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = DeepEspresso, fontWeight = FontWeight.Bold)
                    Text("Exp. Profit", modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.labelSmall, color = DeepEspresso, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                forecast3m.projected.forEach { pm ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pm.label, modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(money(pm.revenue, compact = true), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        Text(money(pm.expenses, compact = true), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        Text(
                            money(pm.profit, compact = true),
                            modifier = Modifier.weight(1.1f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (pm.profit < 0) BrickRed else SuccessGreen
                        )
                    }
                    HorizontalDivider(color = SurfaceButter)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "This forecast draws a steady trend from your last ${series.size} months. Use it as a helpful guide rather than a guarantee.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    fontSize = 11.sp
                )
            }
        }

        // 3. Costs by Category (Last 90 Days)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WHERE THE MONEY WENT · 90 DAYS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedText,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                CostsByCategoryBarChart(categoryCosts = costs90d)
            }
        }

        // 4. Eggs per Month (6 Months)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EGGS COLLECTED EACH MONTH",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedText,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${num(totalEggs6m)} eggs in 6m", style = MaterialTheme.typography.bodySmall, color = WarmAmber, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(14.dp))
                SimpleLineTrendChart(dataPoints = eggsByMonth)
            }
        }

        // 5. Profit by Flock List
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "PROFIT BY FLOCK",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (flocks.isEmpty()) {
                    Text(
                        text = "Add flocks, sales, and expenses to see how much each batch is earning.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        flockProfits.forEach { (flock, money, profit) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onFlockClick(flock.id) }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = flock.code,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${money(money.revenue, compact = true)} sales · ${money(money.cost, compact = true)} costs",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText
                                    )
                                }
                                Text(
                                    text = money(profit),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (profit < 0) BrickRed else SuccessGreen
                                )
                            }
                            HorizontalDivider(color = SurfaceButter)
                        }
                    }
                }
            }
        }
    }
}
