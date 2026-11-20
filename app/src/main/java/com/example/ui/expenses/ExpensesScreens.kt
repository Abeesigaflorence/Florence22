package com.example.ui.expenses

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ReceiptLong
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
import com.example.data.model.Expense
import com.example.data.model.Flock
import com.example.domain.*
import com.example.ui.charts.CostsByCategoryBarChart
import com.example.ui.common.EmptyState
import com.example.ui.common.PageHeader
import com.example.ui.common.StatCard
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToLong

@Composable
fun ExpensesScreen(
    expenses: List<Expense>,
    flocks: List<Flock>,
    onAddExpenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val thisMonthKey = LocalDate.now(ZoneOffset.UTC).toString().take(7)
    val thisMonthExpenses = expenses.filter { it.date.startsWith(thisMonthKey) }
    val spentThisMonth = thisMonthExpenses.sumOf { it.amount }

    val byCategory = thisMonthExpenses.groupBy { it.category }
        .map { (cat, list) -> labelOf(EXPENSE_CATEGORIES, cat) to list.sumOf { it.amount } }
        .sortedByDescending { it.second }

    val largestCost = byCategory.firstOrNull()?.first ?: "None"
    val allTimeCount = expenses.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PageHeader(
            eyebrow = "Money out",
            title = "Expenses",
            description = "Feed, medication, labour and overheads — keep every farm cost clear.",
            actionButton = {
                Button(
                    onClick = onAddExpenseClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenYellow,
                        contentColor = DeepEspresso
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add expense", fontWeight = FontWeight.Bold)
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Spent (mo)",
                value = money(spentThisMonth, compact = true),
                subtitle = "All categories",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Biggest cost",
                value = largestCost,
                subtitle = "This month",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Entries",
                value = "$allTimeCount",
                subtitle = "All time",
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
                        text = "WHERE MONEY WENT THIS MONTH",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedText,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${byCategory.size} categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                CostsByCategoryBarChart(categoryCosts = byCategory)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "RECENT EXPENSES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (expenses.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.ReceiptLong,
                        title = "No expenses recorded yet",
                        description = "Track feed bags, vet care, wages, and transport costs in one place.",
                        ctaText = "Record first expense",
                        onCtaClick = onAddExpenseClick
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        expenses.take(50).forEach { e ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = e.description.ifBlank { labelOf(EXPENSE_CATEGORIES, e.category) },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = TextPrimary
                                    )
                                    val flockLabel = if (e.flockCode.isNotBlank()) e.flockCode else "General farm cost"
                                    Text(
                                        text = "${humanDate(e.date)} · ${e.vendor.ifBlank { "Supplier not noted" }} · $flockLabel",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText
                                    )
                                }
                                Text(
                                    text = money(e.amount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BrickRed
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
fun ExpenseFormDialog(
    flocks: List<Flock>,
    onDismiss: () -> Unit,
    onSubmit: (date: String, category: String, description: String, amount: Double, vendor: String, flockId: String, flockCode: String) -> Unit
) {
    var date by remember { mutableStateOf(todayISO()) }
    var selectedCategory by remember { mutableStateOf("feed") }
    var description by remember { mutableStateOf("") }
    var qtyStr by remember { mutableStateOf("") }
    var unitPriceStr by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var manualAmountOverride by remember { mutableStateOf(false) }
    var vendor by remember { mutableStateOf("") }
    var selectedFlock by remember { mutableStateOf<Flock?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var expandedCatDropdown by remember { mutableStateOf(false) }
    var expandedFlockDropdown by remember { mutableStateOf(false) }

    val qtyVal = qtyStr.toDoubleOrNull() ?: 0.0
    val unitPriceVal = unitPriceStr.toDoubleOrNull() ?: 0.0
    val autoAmount = qtyVal * unitPriceVal

    LaunchedEffect(qtyStr, unitPriceStr, manualAmountOverride) {
        if (!manualAmountOverride && (qtyStr.isNotBlank() || unitPriceStr.isNotBlank())) {
            amountStr = if (autoAmount > 0.0) autoAmount.roundToLong().toString() else ""
        }
    }

    val effectiveAmount = amountStr.toDoubleOrNull() ?: autoAmount

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
                    text = "Add an Expense",
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif),
                    color = TextPrimary
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCatDropdown,
                    onExpandedChange = { expandedCatDropdown = !expandedCatDropdown }
                ) {
                    OutlinedTextField(
                        value = labelOf(EXPENSE_CATEGORIES, selectedCategory),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("What was it for? *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCatDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCatDropdown,
                        onDismissRequest = { expandedCatDropdown = false }
                    ) {
                        EXPENSE_CATEGORIES.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedCategory = key
                                    expandedCatDropdown = false
                                }
                            )
                        }
                    }
                }

                // Optional Qty x Unit Price auto-calculator
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Qty (optional)") },
                        placeholder = { Text("e.g. 5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unitPriceStr,
                        onValueChange = { unitPriceStr = it },
                        label = { Text("Unit Cost (opt)") },
                        placeholder = { Text("e.g. 85000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it.filter { ch -> ch.isDigit() }
                        manualAmountOverride = it.isNotBlank()
                    },
                    label = { Text("Total Amount (UGX) *") },
                    placeholder = { Text("Auto-calculated or enter total") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (effectiveAmount > 0) {
                    Surface(
                        color = SurfaceButter,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = WarmAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (qtyVal > 0 && unitPriceVal > 0 && !manualAmountOverride)
                                        "${num(qtyVal, 1)} × ${money(unitPriceVal)}"
                                    else "Total expense",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedText
                                )
                            }
                            Text(
                                text = money(effectiveAmount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DeepEspresso
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details") },
                    placeholder = { Text("e.g. 5 bags layer mash") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = vendor,
                    onValueChange = { vendor = it },
                    label = { Text("Paid to (vendor)") },
                    placeholder = { Text("e.g. Kigo Feeds Ltd") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expandedFlockDropdown,
                    onExpandedChange = { expandedFlockDropdown = !expandedFlockDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedFlock?.code ?: "General farm cost",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Which flock? (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFlockDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFlockDropdown,
                        onDismissRequest = { expandedFlockDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("General farm cost") },
                            onClick = {
                                selectedFlock = null
                                expandedFlockDropdown = false
                            }
                        )
                        flocks.forEach { f ->
                            DropdownMenuItem(
                                text = { Text(f.code) },
                                onClick = {
                                    selectedFlock = f
                                    expandedFlockDropdown = false
                                }
                            )
                        }
                    }
                }

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
                            if (effectiveAmount <= 0.0) {
                                errorMsg = "Amount must be greater than 0"
                            } else {
                                isSaving = true
                                onSubmit(
                                    date,
                                    selectedCategory,
                                    description,
                                    effectiveAmount,
                                    vendor,
                                    selectedFlock?.id ?: "",
                                    selectedFlock?.code ?: ""
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldenYellow,
                            contentColor = DeepEspresso
                        ),
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "Saving..." else "Save Expense", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
