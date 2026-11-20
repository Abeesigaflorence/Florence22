package com.example.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppUser
import com.example.data.model.Expense
import com.example.data.model.Flock
import com.example.data.model.ProductionRecord
import com.example.data.model.Sale
import com.example.domain.*
import com.example.ui.charts.ProfitTrendChart
import com.example.ui.common.StatCard
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun DashboardScreen(
    user: AppUser?,
    flocks: List<Flock>,
    productionRecords: List<ProductionRecord>,
    sales: List<Sale>,
    expenses: List<Expense>,
    onLogProductionClick: () -> Unit,
    onNavigateToFlocks: () -> Unit,
    onNavigateToReports: () -> Unit,
    onFlockClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstName = user?.fullName?.split(" ")?.firstOrNull()?.ifBlank { null }
    val greeting = timeOfDayGreeting(firstName)

    val activeFlocks = flocks.filter { it.status != "closed" }
    val activeBirds = activeFlocks.sumOf { it.currentCount }

    val today = LocalDate.now(ZoneOffset.UTC)
    val sevenDaysAgo = today.minusDays(7).toString()
    val eggsThisWeek = productionRecords
        .filter { it.date >= sevenDaysAgo }
        .sumOf { it.eggsCollected }

    val thisMonthKey = today.toString().take(7)
    val revThisMonth = sales.filter { it.date.startsWith(thisMonthKey) }.sumOf { it.total }
    val expThisMonth = expenses.filter { it.date.startsWith(thisMonthKey) }.sumOf { it.amount }
    val netProfitThisMonth = revThisMonth - expThisMonth

    val series = Finance.monthlySeries(sales, expenses)
    val forecastReport = forecast(series, horizon = 1)

    // Friendly human farm summary sentence
    val pulseMessage = when {
        activeFlocks.isEmpty() ->
            "Welcome to Royo Farm! Add your first batch of birds to start counting eggs and tracking profits."
        eggsThisWeek > 0 ->
            "You're caring for ${num(activeBirds.toDouble())} birds across ${activeFlocks.size} active ${if (activeFlocks.size == 1) "flock" else "flocks"}, with ${num(eggsThisWeek)} eggs collected this week."
        else ->
            "You have ${num(activeBirds.toDouble())} birds across ${activeFlocks.size} active ${if (activeFlocks.size == 1) "flock" else "flocks"}. Tap 'Record eggs' whenever you finish today's collection."
    }

    data class ActivityItem(
        val date: String,
        val title: String,
        val subtitle: String,
        val amount: Double,
        val isIncome: Boolean
    )

    val recentActivity = (
        sales.map { s ->
            ActivityItem(
                date = s.date,
                title = s.description.ifBlank { labelOf(PRODUCTS, s.product) },
                subtitle = "${humanDate(s.date)} · ${s.customer.ifBlank { "Walk-in customer" }}${if (s.flockCode.isNotBlank()) " · ${s.flockCode}" else ""}",
                amount = s.total,
                isIncome = true
            )
        } + expenses.map { e ->
            ActivityItem(
                date = e.date,
                title = e.description.ifBlank { labelOf(EXPENSE_CATEGORIES, e.category) },
                subtitle = "${humanDate(e.date)} · ${e.vendor.ifBlank { "Farm expense" }}${if (e.flockCode.isNotBlank()) " · ${e.flockCode}" else ""}",
                amount = e.amount,
                isIncome = false
            )
        }
    ).sortedByDescending { it.date }.take(5)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Atmospheric Hero Greeting Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DeepEspresso)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 148.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_farm_sunrise),
                    contentDescription = "Royo Farm morning view",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(20.dp))
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    DeepEspresso.copy(alpha = 0.88f),
                                    DeepEspresso.copy(alpha = 0.52f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = GoldenYellow.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (user?.role == "ADMIN") "FARM OWNER" else "FARM TEAM",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldenYellowBright,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = humanDate(today.toString()),
                            style = MaterialTheme.typography.labelSmall,
                            color = SurfaceButter
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = SurfaceWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Here is how your farm is doing today.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SurfaceButter
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onLogProductionClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldenYellow,
                            contentColor = DeepEspresso
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("record_eggs_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record today's eggs", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Human Farm Pulse Note
        Surface(
            color = SurfaceButter,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = WarmAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = pulseMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )
            }
        }

        // 2. Four Stat Cards Grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Birds alive",
                    value = num(activeBirds.toDouble()),
                    subtitle = "${activeFlocks.size} ${if (activeFlocks.size == 1) "flock" else "flocks"} active",
                    icon = Icons.Default.Pets,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Eggs this week",
                    value = num(eggsThisWeek),
                    subtitle = "${(eggsThisWeek / 30).toInt()} trays · last 7d",
                    icon = Icons.Default.Egg,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Sales this month",
                    value = money(revThisMonth, compact = true),
                    subtitle = "Money brought in",
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Net profit",
                    value = money(netProfitThisMonth, compact = true),
                    subtitle = "After ${money(expThisMonth, compact = true)} costs",
                    isDestructive = netProfitThisMonth < 0,
                    isAccent = netProfitThisMonth >= 0,
                    icon = Icons.Default.AccountBalanceWallet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Profit & Loss Trend Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MONEY IN VS MONEY OUT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedText,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${series.firstOrNull()?.label ?: ""} – ${series.lastOrNull()?.label ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                    }
                    Text(
                        text = "Full report →",
                        style = MaterialTheme.typography.labelMedium,
                        color = WarmAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onNavigateToReports() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                ProfitTrendChart(series = series)
            }
        }

        // 4. Sunny Yellow Forecast Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GoldenYellowDark.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = GoldenYellow)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOOKING AHEAD · ${forecastReport.nextLabel.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepEspresso.copy(alpha = 0.8f),
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val (directionLabel, directionIcon) = when (forecastReport.direction) {
                        "up" -> "Looking up" to Icons.AutoMirrored.Filled.TrendingUp
                        "down" -> "Slowing down" to Icons.AutoMirrored.Filled.TrendingDown
                        else -> "Holding steady" to Icons.AutoMirrored.Filled.TrendingFlat
                    }
                    Surface(
                        color = GoldenYellowBright,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = directionIcon,
                                contentDescription = null,
                                tint = DeepEspresso,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = directionLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = money(forecastReport.projectedProfit, compact = false),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = DeepEspresso
                )

                Text(
                    text = "Expected net profit based on your last 6 months.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DeepEspresso.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceButter.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ForecastDetailRow(
                        label = "Expected sales",
                        value = money(forecastReport.projectedRevenue)
                    )
                    ForecastDetailRow(
                        label = "Expected costs",
                        value = money(forecastReport.projectedExpenses)
                    )
                    ForecastDetailRow(
                        label = "Forecast reliability",
                        value = pct(forecastReport.confidence * 100.0, digits = 0)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "A friendly guide from your past records — real life may vary.",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepEspresso.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // 5. Flock Snapshot Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HOW YOUR FLOCKS ARE DOING",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedText,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "All flocks →",
                        style = MaterialTheme.typography.labelMedium,
                        color = WarmAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onNavigateToFlocks() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (activeFlocks.isEmpty()) {
                    Text(
                        text = "Add your first flock to keep track of survival, feed and daily eggs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        activeFlocks.take(3).forEach { flock ->
                            val survival = survivalPct(flock.initialCount.toDouble(), flock.currentCount.toDouble())
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onFlockClick(flock.id) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = flock.code,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif
                                        ),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${flock.currentCount} birds · ${pct(survival, 1)} healthy",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (survival < 90) BrickRed else SuccessGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (survival / 100.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (survival < 90) BrickRed else GoldenYellow,
                                    trackColor = SurfaceButter
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Recent Activity Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "WHAT HAPPENED RECENTLY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (recentActivity.isEmpty()) {
                    Text(
                        text = "Once you record a sale or an expense, it will appear right here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        recentActivity.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText
                                    )
                                }
                                Text(
                                    text = "${if (item.isIncome) "+" else "−"}${money(item.amount)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isIncome) SuccessGreen else BrickRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = DeepEspresso.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = DeepEspresso
        )
    }
}
