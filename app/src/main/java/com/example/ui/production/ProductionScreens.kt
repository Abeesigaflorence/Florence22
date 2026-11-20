package com.example.ui.production

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Flock
import com.example.data.model.ProductionRecord
import com.example.domain.*
import com.example.ui.charts.SimpleLineTrendChart
import com.example.ui.common.EmptyState
import com.example.ui.common.PageHeader
import com.example.ui.common.StatCard
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.max

@Composable
fun ProductionScreen(
    flocks: List<Flock>,
    records: List<ProductionRecord>,
    onLogProductionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now(ZoneOffset.UTC)
    val sevenDaysAgo = today.minusDays(7).toString()
    val last7Records = records.filter { it.date >= sevenDaysAgo }

    val eggs7d = last7Records.sumOf { it.eggsCollected }
    val mortality7d = last7Records.sumOf { it.mortality }
    val feed7d = last7Records.sumOf { it.feedUsedKg }

    val dailyTotals = records.take(60)
        .groupBy { it.date }
        .mapValues { entry -> entry.value.sumOf { it.eggsCollected } }
        .toList()
        .sortedBy { it.first }
        .takeLast(14)
        .map { fmtDateShort(it.first) to it.second }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PageHeader(
            eyebrow = "Daily check-in",
            title = "Production",
            description = "Count today's eggs, feed, and birds — we'll automatically calculate trays and update flock counts.",
            actionButton = {
                Button(
                    onClick = onLogProductionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenYellow,
                        contentColor = DeepEspresso
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record", fontWeight = FontWeight.Bold)
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Eggs (7d)",
                value = num(eggs7d),
                subtitle = "${(eggs7d / 30).toInt()} full trays",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Birds lost",
                value = "${mortality7d.toInt()}",
                subtitle = "Last 7 days",
                isDestructive = mortality7d > 0,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Feed used",
                value = "${num(feed7d, 1)} kg",
                subtitle = "Last 7 days",
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
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
                        text = "DAILY EGG OUTPUT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedText,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${dailyTotals.size} days logged",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                SimpleLineTrendChart(dataPoints = dailyTotals, unitLabel = "eggs")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "RECENT LOGS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (records.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Egg,
                        title = "No daily logs yet",
                        description = "Record today's eggs, feed, and any bird losses to keep your farm records up to date.",
                        ctaText = "Record today's collection",
                        onCtaClick = onLogProductionClick
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        records.take(40).forEach { rec ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rec.flockCode.ifBlank { "Flock" },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = TextPrimary
                                    )
                                    val lostStr = if (rec.mortality > 0) " · ${rec.mortality.toInt()} lost" else ""
                                    val brokenStr = if (rec.eggsBroken > 0) " · ${rec.eggsBroken.toInt()} broken" else ""
                                    Text(
                                        text = "${humanDate(rec.date)} · ${num(rec.feedUsedKg, 1)} kg feed$lostStr$brokenStr",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText
                                    )
                                }
                                Text(
                                    text = "${rec.eggsCollected.toInt()} eggs",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SuccessGreen
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionFormDialog(
    flocks: List<Flock>,
    initialFlock: Flock? = null,
    onDismiss: () -> Unit,
    onSubmit: (flock: Flock, date: String, eggs: Double, broken: Double, lost: Double, feed: Double, notes: String) -> Unit
) {
    val activeFlocks = flocks.filter { it.status != "closed" }
    var selectedFlock by remember { mutableStateOf(initialFlock ?: activeFlocks.firstOrNull() ?: flocks.firstOrNull()) }
    var expandedFlockDropdown by remember { mutableStateOf(false) }

    var date by remember { mutableStateOf(todayISO()) }
    var eggsStr by remember { mutableStateOf("") }
    var brokenStr by remember { mutableStateOf("") }
    var mortalityStr by remember { mutableStateOf("") }
    var feedStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Automatic calculator values
    val eggsVal = eggsStr.toIntOrNull() ?: 0
    val brokenVal = brokenStr.toIntOrNull() ?: 0
    val mortalityVal = mortalityStr.toIntOrNull() ?: 0
    val usableEggs = max(0, eggsVal - brokenVal)
    val fullTrays = usableEggs / 30
    val looseEggs = usableEggs % 30
    val currentBirds = selectedFlock?.currentCount ?: 0
    val layingRate = if (currentBirds > 0 && eggsVal > 0) (eggsVal.toDouble() / currentBirds) * 100.0 else 0.0
    val birdsAfter = max(0, currentBirds - mortalityVal)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Record Daily Production",
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif),
                    color = TextPrimary
                )

                // Flock selector
                ExposedDropdownMenuBox(
                    expanded = expandedFlockDropdown,
                    onExpandedChange = { expandedFlockDropdown = !expandedFlockDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedFlock?.code ?: "Select a flock",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Which flock? *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFlockDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFlockDropdown,
                        onDismissRequest = { expandedFlockDropdown = false }
                    ) {
                        flocks.forEach { f ->
                            DropdownMenuItem(
                                text = { Text("${f.code} (${f.currentCount} alive)") },
                                onClick = {
                                    selectedFlock = f
                                    expandedFlockDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = eggsStr,
                    onValueChange = { eggsStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Total Eggs Collected") },
                    placeholder = { Text("e.g. 450") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brokenStr,
                        onValueChange = { brokenStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Broken Eggs") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mortalityStr,
                        onValueChange = { mortalityStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Birds Lost") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = feedStr,
                    onValueChange = { feedStr = it },
                    label = { Text("Feed Used (kg)") },
                    placeholder = { Text("e.g. 62.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Automatic Production Calculator Summary Card
                if (eggsVal > 0 || mortalityVal > 0) {
                    Surface(
                        color = SurfaceButter,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                    text = "Automatic summary",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WarmAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (eggsVal > 0) {
                                Text(
                                    text = "$usableEggs good eggs = $fullTrays trays + $looseEggs loose",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepEspresso
                                )
                                if (currentBirds > 0) {
                                    Text(
                                        text = "Laying rate: ${pct(layingRate, 1)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText
                                    )
                                }
                            }
                            if (mortalityVal > 0 && selectedFlock != null) {
                                Text(
                                    text = "Birds remaining in ${selectedFlock?.code}: $birdsAfter alive",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrickRed,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
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
                            val f = selectedFlock
                            if (f == null) {
                                errorMsg = "Please choose a flock first"
                            } else {
                                val eggs = eggsStr.toDoubleOrNull() ?: 0.0
                                val broken = brokenStr.toDoubleOrNull() ?: 0.0
                                val mortality = mortalityStr.toDoubleOrNull() ?: 0.0
                                val feed = feedStr.toDoubleOrNull() ?: 0.0
                                isSaving = true
                                onSubmit(f, date, eggs, broken, mortality, feed, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldenYellow,
                            contentColor = DeepEspresso
                        ),
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "Saving..." else "Save Log", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
