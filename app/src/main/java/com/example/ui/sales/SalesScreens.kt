package com.example.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
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
import com.example.data.model.Sale
import com.example.domain.*
import com.example.ui.common.EmptyState
import com.example.ui.common.PageHeader
import com.example.ui.common.StatCard
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToLong

@Composable
fun SalesScreen(
    sales: List<Sale>,
    flocks: List<Flock>,
    onRecordSaleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val thisMonthKey = LocalDate.now(ZoneOffset.UTC).toString().take(7)
    val revThisMonth = sales.filter { it.date.startsWith(thisMonthKey) }.sumOf { it.total }
    val awaitingPayment = sales.filter { it.paymentStatus == "pending" }.sumOf { it.total }
    val allTimeCount = sales.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PageHeader(
            eyebrow = "Money in",
            title = "Sales",
            description = "Eggs, birds and manure sold — totals calculate automatically as you enter quantities.",
            actionButton = {
                Button(
                    onClick = onRecordSaleClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenYellow,
                        contentColor = DeepEspresso
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record sale", fontWeight = FontWeight.Bold)
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "This month",
                value = money(revThisMonth, compact = true),
                subtitle = "Recorded sales",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Pending pay",
                value = money(awaitingPayment, compact = true),
                subtitle = "Still owed",
                isDestructive = awaitingPayment > 0,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Sales logged",
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
                Text(
                    text = "RECENT SALES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (sales.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.AttachMoney,
                        title = "No sales recorded yet",
                        description = "Record egg trays, live birds, or manure sold — we'll add up the totals for you.",
                        ctaText = "Record your first sale",
                        onCtaClick = onRecordSaleClick
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        sales.take(50).forEach { s ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = s.description.ifBlank { labelOf(PRODUCTS, s.product) },
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = TextPrimary
                                        )
                                        if (s.paymentStatus == "pending") {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = SurfaceButter,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Pending",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = WarmAmber,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${humanDate(s.date)} · ${s.customer.ifBlank { "Walk-in customer" }} · ${num(s.quantity)} ${s.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText
                                    )
                                }
                                Text(
                                    text = money(s.total),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
fun SaleFormDialog(
    flocks: List<Flock>,
    onDismiss: () -> Unit,
    onSubmit: (date: String, product: String, description: String, quantity: Double, unit: String, unitPrice: Double, total: Double, customer: String, flockId: String, flockCode: String, paymentStatus: String) -> Unit
) {
    var date by remember { mutableStateOf(todayISO()) }
    var selectedProduct by remember { mutableStateOf("eggs") }
    var description by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("tray") }
    var unitPriceStr by remember { mutableStateOf("") }
    var totalStr by remember { mutableStateOf("") }
    var manualTotalOverride by remember { mutableStateOf(false) }
    var customer by remember { mutableStateOf("") }
    var selectedFlock by remember { mutableStateOf<Flock?>(null) }
    var paymentStatus by remember { mutableStateOf("paid") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var expandedProductDropdown by remember { mutableStateOf(false) }
    var expandedFlockDropdown by remember { mutableStateOf(false) }

    // Automatic calculator logic: automatically updates total when quantity or unitPrice changes
    val qtyVal = quantityStr.toDoubleOrNull() ?: 0.0
    val unitPriceVal = unitPriceStr.toDoubleOrNull() ?: 0.0
    val autoTotal = qtyVal * unitPriceVal

    LaunchedEffect(quantityStr, unitPriceStr, manualTotalOverride) {
        if (!manualTotalOverride) {
            totalStr = if (autoTotal > 0.0) autoTotal.roundToLong().toString() else ""
        }
    }

    val effectiveTotal = totalStr.toDoubleOrNull() ?: autoTotal

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
                    text = "Record a Sale",
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif),
                    color = TextPrimary
                )

                // Product selector
                ExposedDropdownMenuBox(
                    expanded = expandedProductDropdown,
                    onExpandedChange = { expandedProductDropdown = !expandedProductDropdown }
                ) {
                    OutlinedTextField(
                        value = labelOf(PRODUCTS, selectedProduct),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("What did you sell? *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProductDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedProductDropdown,
                        onDismissRequest = { expandedProductDropdown = false }
                    ) {
                        PRODUCTS.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedProduct = key
                                    unit = when (key) {
                                        "eggs" -> "tray"
                                        "live_birds" -> "bird"
                                        "manure" -> "bag"
                                        else -> "piece"
                                    }
                                    expandedProductDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Quantity") },
                        placeholder = { Text("e.g. 10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unitPriceStr,
                        onValueChange = { unitPriceStr = it },
                        label = { Text("Price per $unit") },
                        placeholder = { Text("e.g. 12000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = totalStr,
                        onValueChange = {
                            totalStr = it
                            manualTotalOverride = it.isNotBlank()
                        },
                        label = { Text(if (manualTotalOverride) "Custom Total" else "Auto Total (UGX)") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Live Automatic Calculator Summary Card
                Surface(
                    color = SurfaceButter,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
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
                                    text = if (manualTotalOverride) "Custom amount" else "Automatic total",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WarmAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (manualTotalOverride && autoTotal > 0) {
                                TextButton(
                                    onClick = { manualTotalOverride = false },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("Use auto (${money(autoTotal)})", style = MaterialTheme.typography.labelSmall, color = DeepEspresso)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (effectiveTotal > 0) money(effectiveTotal) else "Enter quantity & unit price above",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (effectiveTotal > 0) DeepEspresso else MutedText
                        )
                        if (qtyVal > 0 && unitPriceVal > 0 && !manualTotalOverride) {
                            val eggHint = if (unit.equals("tray", ignoreCase = true)) " (${(qtyVal * 30).toInt()} eggs)" else ""
                            Text(
                                text = "${num(qtyVal, 1)} $unit × ${money(unitPriceVal)}$eggHint",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("e.g. Morning egg pickup") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = customer,
                    onValueChange = { customer = it },
                    label = { Text("Buyer's name") },
                    placeholder = { Text("Walk-in customer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Optional Flock selector
                ExposedDropdownMenuBox(
                    expanded = expandedFlockDropdown,
                    onExpandedChange = { expandedFlockDropdown = !expandedFlockDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedFlock?.code ?: "General farm sale",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From which flock? (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFlockDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFlockDropdown,
                        onDismissRequest = { expandedFlockDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("General farm sale") },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = paymentStatus == "paid",
                        onClick = { paymentStatus = "paid" },
                        label = { Text("Paid now") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = paymentStatus == "pending",
                        onClick = { paymentStatus = "pending" },
                        label = { Text("Will pay later") },
                        modifier = Modifier.weight(1f)
                    )
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
                            if (effectiveTotal <= 0.0) {
                                errorMsg = "Please enter quantity and price, or a total above 0"
                            } else {
                                isSaving = true
                                onSubmit(
                                    date,
                                    selectedProduct,
                                    description,
                                    qtyVal,
                                    unit,
                                    unitPriceVal,
                                    effectiveTotal,
                                    customer,
                                    selectedFlock?.id ?: "",
                                    selectedFlock?.code ?: "",
                                    paymentStatus
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldenYellow,
                            contentColor = DeepEspresso
                        ),
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "Saving..." else "Save Sale", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
