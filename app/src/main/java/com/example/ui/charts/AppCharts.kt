package com.example.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.Finance
import com.example.domain.money
import com.example.ui.theme.*

@Composable
fun ProfitTrendChart(
    series: List<Finance.MonthPoint>,
    modifier: Modifier = Modifier
) {
    if (series.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Record a few sales and costs to see your 6-month trend",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )
        }
        return
    }

    val maxVal = series.maxOfOrNull { maxOf(it.revenue, it.expenses) }?.coerceAtLeast(1000.0) ?: 1000.0

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).background(GoldenYellowDark, CircleShape))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Money in", style = MaterialTheme.typography.labelSmall, color = MutedText)
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(8.dp).background(WarmAmber, CircleShape))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Money out", style = MaterialTheme.typography.labelSmall, color = MutedText)
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val width = size.width
            val height = size.height - 30f
            val stepX = if (series.size > 1) width / (series.size - 1) else width

            val gridLines = 3
            for (i in 0..gridLines) {
                val y = height * (i.toFloat() / gridLines)
                drawLine(
                    color = CardBorder,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            val revPath = Path()
            val expPath = Path()

            series.forEachIndexed { index, point ->
                val x = index * stepX
                val revY = height - (point.revenue.toFloat() / maxVal.toFloat()) * height
                val expY = height - (point.expenses.toFloat() / maxVal.toFloat()) * height

                if (index == 0) {
                    revPath.moveTo(x, revY)
                    expPath.moveTo(x, expY)
                } else {
                    revPath.lineTo(x, revY)
                    expPath.lineTo(x, expY)
                }
            }

            drawPath(
                path = revPath,
                color = GoldenYellowDark,
                style = Stroke(width = 4.5f, cap = StrokeCap.Round)
            )
            drawPath(
                path = expPath,
                color = WarmAmber,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            series.forEachIndexed { index, point ->
                val x = index * stepX
                val revY = height - (point.revenue.toFloat() / maxVal.toFloat()) * height
                val expY = height - (point.expenses.toFloat() / maxVal.toFloat()) * height

                drawCircle(color = GoldenYellowDark, radius = 5.5f, center = Offset(x, revY))
                drawCircle(color = SurfaceWhite, radius = 2.5f, center = Offset(x, revY))

                drawCircle(color = WarmAmber, radius = 5f, center = Offset(x, expY))
                drawCircle(color = SurfaceWhite, radius = 2.5f, center = Offset(x, expY))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            series.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun CostsByCategoryBarChart(
    categoryCosts: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    if (categoryCosts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No expenses recorded yet for this period", style = MaterialTheme.typography.bodyMedium, color = MutedText)
        }
        return
    }

    val maxAmount = categoryCosts.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categoryCosts.forEach { (categoryLabel, amount) ->
            val fraction = (amount / maxAmount).toFloat().coerceIn(0.04f, 1f)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = categoryLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = money(amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(SurfaceButter, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .background(GoldenYellow, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleLineTrendChart(
    dataPoints: List<Pair<String, Double>>,
    lineColor: Color = GoldenYellowDark,
    unitLabel: String = "eggs",
    modifier: Modifier = Modifier
) {
    if (dataPoints.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Log at least 2 days of production to see your trend", style = MaterialTheme.typography.bodyMedium, color = MutedText)
        }
        return
    }

    val maxVal = dataPoints.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val width = size.width
            val height = size.height - 20f
            val stepX = width / (dataPoints.size - 1)

            val path = Path()
            dataPoints.forEachIndexed { i, (_, v) ->
                val x = i * stepX
                val y = height - (v.toFloat() / maxVal.toFloat()) * height
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            dataPoints.forEachIndexed { i, (_, v) ->
                val x = i * stepX
                val y = height - (v.toFloat() / maxVal.toFloat()) * height
                drawCircle(color = lineColor, radius = 4.5f, center = Offset(x, y))
                drawCircle(color = SurfaceWhite, radius = 2f, center = Offset(x, y))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dataPoints.first().first,
                style = MaterialTheme.typography.labelSmall,
                color = MutedText
            )
            Text(
                text = dataPoints.last().first,
                style = MaterialTheme.typography.labelSmall,
                color = MutedText
            )
        }
    }
}
