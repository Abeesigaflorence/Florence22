package com.example.ui.flocks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Expense
import com.example.data.model.Flock
import com.example.data.model.ProductionRecord
import com.example.data.model.Sale
import com.example.domain.*
import com.example.ui.charts.SimpleLineTrendChart
import com.example.ui.common.EmptyState
import com.example.ui.common.PageHeader
import com.example.ui.common.StatCard
import com.example.ui.theme.*

@Composable
fun FlockListScreen(
    flocks: List<Flock>,
    onFlockClick: (String) -> Unit,
    onNewFlockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PageHeader(
            eyebrow = "Poultry",
            title = "Flocks",
            description = "Keep an eye on every batch of birds — their age, health, and daily count.",
            actionButton = {
                Button(
                    onClick = onNewFlockClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenYellow,
                        contentColor = DeepEspresso
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("new_flock_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New flock", fontWeight = FontWeight.Bold)
                }
            }
        )

        if (flocks.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Pets,
                title = "No flocks added yet",
                description = "Add your first batch of birds to start tracking eggs, feed, and profits.",
                ctaText = "Add your first flock",
                onCtaClick = onNewFlockClick
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(flocks, key = { it.id }) { flock ->
                    FlockCard(flock = flock, onClick = { onFlockClick(flock.id) })
                }
            }
        }
    }
}

@Composable
fun FlockCard(
    flock: Flock,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val survival = survivalPct(flock.initialCount.toDouble(), flock.currentCount.toDouble())
    val age = birdAgeDays(flock.placementDate)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = flock.code,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "${flock.breed.ifBlank { "Mixed breed" }} · ${flock.house.ifBlank { "Main house" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }

                // Type Badge
                Surface(
                    color = if (flock.status == "active") GoldenYellow else SurfaceButter,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (flock.type == "broiler") "Broilers" else "Layers",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (flock.status == "active") DeepEspresso else MutedText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Three Figures: Birds alive, Days old, Survival %
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FlockMetricColumn(label = "BIRDS ALIVE", value = num(flock.currentCount.toDouble()))
                FlockMetricColumn(label = "AGE", value = "$age days")
                FlockMetricColumn(
                    label = "HEALTHY",
                    value = pct(survival, 1),
                    valueColor = if (survival < 90) BrickRed else SuccessGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (survival / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (survival < 90) BrickRed else GoldenYellow,
                trackColor = SurfaceButter
            )

            Spacer(modifier = Modifier.height(10.dp))

            val lossSummary = if (flock.mortalityCount == 0) "No losses recorded" else "${flock.mortalityCount} lost"
            Text(
                text = "Placed ${humanDate(flock.placementDate)} · $lossSummary",
                style = MaterialTheme.typography.labelSmall,
                color = MutedText
            )
        }
    }
}

@Composable
private fun FlockMetricColumn(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MutedText,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            ),
            color = valueColor
        )
    }
}

@Composable
fun FlockDetailScreen(
    flockId: String,
    flocks: List<Flock>,
    productionRecords: List<ProductionRecord>,
    sales: List<Sale>,
    expenses: List<Expense>,
    onBackClick: () -> Unit,
    onLogProductionClick: (Flock) -> Unit,
    modifier: Modifier = Modifier
) {
    val flock = flocks.firstOrNull { it.id == flockId }

    if (flock == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("We couldn't find that flock anymore", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow, contentColor = DeepEspresso)
            ) {
                Text("Back to flocks", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val flockRecords = productionRecords.filter { it.flockId == flock.id }
        .sortedByDescending { it.date }

    val survival = survivalPct(flock.initialCount.toDouble(), flock.currentCount.toDouble())
    val age = birdAgeDays(flock.placementDate)
    val totalEggs = flockRecords.sumOf { it.eggsCollected }
    val totalFeedKg = flockRecords.sumOf { it.feedUsedKg }
    val moneyInfo = flockMoney(flock.id, sales, expenses)

    // Newest 14 records for eggs trend (chronological)
    val trendData = flockRecords.take(14).reversed().map {
        fmtDateShort(it.date) to it.eggsCollected
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Back link & Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp), tint = WarmAmber)
                Spacer(modifier = Modifier.width(6.dp))
                Text("All flocks", color = WarmAmber, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { onLogProductionClick(flock) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldenYellow,
                    contentColor = DeepEspresso
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Record eggs", fontWeight = FontWeight.Bold)
            }
        }

        // Header info
        Column {
            Surface(
                color = SurfaceButter,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "${flock.breed.ifBlank { "Mixed breed" }} · ${flock.house.ifBlank { "Main house" }}".uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmAmber,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = flock.code,
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Text(
                text = "Placed ${fmtDate(flock.placementDate)} · $age days on the farm",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )
        }

        // 4 Stat Cards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Birds alive",
                    value = num(flock.currentCount.toDouble()),
                    subtitle = "of ${num(flock.initialCount.toDouble())} placed",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Healthy",
                    value = pct(survival, 1),
                    subtitle = if (flock.mortalityCount == 0) "No losses" else "${flock.mortalityCount} lost",
                    isDestructive = survival < 90,
                    isAccent = survival >= 95,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Eggs collected",
                    value = num(totalEggs),
                    subtitle = "${(totalEggs / 30).toInt()} trays total",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Flock net profit",
                    value = money(moneyInfo.profit, compact = true),
                    subtitle = "${money(moneyInfo.revenue, compact = true)} in · ${money(moneyInfo.cost, compact = true)} out",
                    isDestructive = moneyInfo.profit < 0,
                    isAccent = moneyInfo.profit >= 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Eggs per day trend
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "EGGS COLLECTED EACH DAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                SimpleLineTrendChart(dataPoints = trendData)
            }
        }

        // Flock Facts Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "AT A GLANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                FactRow("Flock type", if (flock.type == "broiler") "Broilers · meat" else "Layers · table eggs")
                FactRow("Feed given", "${num(totalFeedKg, 1)} kg")
                FactRow("Recorded costs", money(moneyInfo.cost))
                FactRow("Status", if (flock.status == "active") "Active & producing" else "Closed")
                if (flock.notes.isNotBlank()) {
                    FactRow("Notes", flock.notes)
                }
            }
        }

        // Production History (Newest 12)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "RECENT COLLECTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (flockRecords.isEmpty()) {
                    Text(
                        text = "No daily logs recorded for this flock yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        flockRecords.take(12).forEach { rec ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(humanDate(rec.date), style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    val lostText = if (rec.mortality > 0) " · ${rec.mortality.toInt()} lost" else ""
                                    val brokenText = if (rec.eggsBroken > 0) " · ${rec.eggsBroken.toInt()} cracked" else ""
                                    Text(
                                        text = "${num(rec.feedUsedKg, 1)} kg feed$lostText$brokenText",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText
                                    )
                                }
                                Text(
                                    text = "${rec.eggsCollected.toInt()} eggs",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = WarmAmber
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

@Composable
private fun FactRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MutedText)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
fun FlockFormDialog(
    onDismiss: () -> Unit,
    onSubmit: (code: String, breed: String, type: String, house: String, placementDate: String, initialCount: Int, notes: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("layer") }
    var house by remember { mutableStateOf("") }
    var placementDate by remember { mutableStateOf(todayISO()) }
    var initialCountStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val initialCountVal = initialCountStr.toIntOrNull() ?: 0
    val calculatedAgeDays = remember(placementDate) { birdAgeDays(placementDate) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Text(
                        text = "Add a New Flock",
                        style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif),
                        color = TextPrimary
                    )
                    Text(
                        text = "Tell us about this batch so we can track its age and survival automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Flock Name / Code *") },
                    placeholder = { Text("e.g. Layers A · 2026") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Breed") },
                    placeholder = { Text("e.g. Lohmann Brown") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "layer",
                        onClick = { type = "layer" },
                        label = { Text("Layers (Eggs)") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldenYellowSoft,
                            selectedLabelColor = DeepEspresso
                        )
                    )
                    FilterChip(
                        selected = type == "broiler",
                        onClick = { type = "broiler" },
                        label = { Text("Broilers (Meat)") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldenYellowSoft,
                            selectedLabelColor = DeepEspresso
                        )
                    )
                }

                OutlinedTextField(
                    value = house,
                    onValueChange = { house = it },
                    label = { Text("House / Pen") },
                    placeholder = { Text("e.g. House 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = placementDate,
                    onValueChange = { placementDate = it },
                    label = { Text("Placement Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = initialCountStr,
                    onValueChange = { initialCountStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Birds Placed *") },
                    placeholder = { Text("e.g. 500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Automatic Flock Preview
                Surface(
                    color = SurfaceButter,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = WarmAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Automatic batch summary",
                                style = MaterialTheme.typography.labelMedium,
                                color = WarmAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (initialCountVal > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Starting count", style = MaterialTheme.typography.bodySmall, color = MutedText)
                                Text(
                                    "${num(initialCountVal.toDouble())} ${if (type == "broiler") "broilers" else "layers"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Age from placement", style = MaterialTheme.typography.bodySmall, color = MutedText)
                                Text(
                                    "$calculatedAgeDays days · 100% healthy",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SuccessGreen
                                )
                            }
                        } else {
                            Text(
                                text = "Enter how many birds you placed to preview age and starting count.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = BrickRed, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = MutedText) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val count = initialCountStr.toIntOrNull() ?: 0
                            if (code.isBlank()) {
                                errorMsg = "Please give this flock a name or code"
                            } else if (count <= 0) {
                                errorMsg = "Please enter at least 1 bird"
                            } else {
                                isSaving = true
                                onSubmit(code, breed, type, house, placementDate, count, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldenYellow,
                            contentColor = DeepEspresso
                        ),
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "Saving..." else "Add Flock", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
