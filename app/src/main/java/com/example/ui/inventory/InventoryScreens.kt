package com.example.ui.inventory

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.example.data.model.InventoryItem
import com.example.data.model.StockMovement
import com.example.domain.*
import com.example.ui.common.EmptyState
import com.example.ui.common.PageHeader
import com.example.ui.common.StatCard
import com.example.ui.theme.*
import kotlin.math.max

@Composable
fun InventoryScreen(
    items: List<InventoryItem>,
    movements: List<StockMovement>,
    onNewItemClick: () -> Unit,
    onMovementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemsTracked = items.size
    val totalStockValue = items.sumOf { it.quantity * it.unitCost }
    val needsReordering = items.count { isLowStock(it) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PageHeader(
            eyebrow = "Store",
            title = "Inventory",
            description = "Feed, medication and equipment — stock values and reorder alerts update automatically.",
            actionButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onMovementClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepEspresso)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Move", style = MaterialTheme.typography.labelMedium, color = DeepEspresso)
                    }
                    Button(
                        onClick = onNewItemClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldenYellow,
                            contentColor = DeepEspresso
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New item", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Items",
                value = "$itemsTracked",
                subtitle = "In store",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Stock value",
                value = money(totalStockValue, compact = true),
                subtitle = "Auto total",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Low stock",
                value = "$needsReordering",
                subtitle = "Needs reorder",
                isDestructive = needsReordering > 0,
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
                    text = "ON HAND IN STORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (items.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Inventory2,
                        title = "Your store is empty",
                        description = "Add feed bags, vaccines, or tools so you always know what's left before running out.",
                        ctaText = "Add first item",
                        onCtaClick = onNewItemClick
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items.forEach { item ->
                            val low = isLowStock(item)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = TextPrimary
                                        )
                                        if (low) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = BrickRed.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Reorder",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = BrickRed,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${labelOf(INVENTORY_CATEGORIES, item.category)} · ${money(item.unitCost)} each",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText
                                    )
                                }

                                Text(
                                    text = "${num(item.quantity)} ${item.unit}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (low) BrickRed else SuccessGreen
                                )
                            }
                            HorizontalDivider(color = SurfaceButter)
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "RECENT STOCK MOVEMENTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (movements.isEmpty()) {
                    Text("No stock movements recorded yet.", style = MaterialTheme.typography.bodyMedium, color = MutedText)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        movements.take(40).forEach { mov ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = if (mov.type == "in") SuccessGreen.copy(alpha = 0.15f) else BrickRed.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (mov.type == "in") "In" else "Out",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (mov.type == "in") SuccessGreen else BrickRed,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = mov.itemName.ifBlank { "Item" },
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        text = "${humanDate(mov.date)}${if (mov.note.isNotBlank()) " · ${mov.note}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText
                                    )
                                }
                                Text(
                                    text = "${if (mov.type == "in") "+" else "−"}${num(mov.quantity)}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (mov.type == "in") SuccessGreen else BrickRed
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
fun InventoryItemFormDialog(
    onDismiss: () -> Unit,
    onSubmit: (name: String, category: String, unit: String, quantity: Double, reorderLevel: Double, unitCost: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("feed") }
    var unit by remember { mutableStateOf("bag") }
    var quantityStr by remember { mutableStateOf("") }
    var reorderLevelStr by remember { mutableStateOf("") }
    var unitCostStr by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var expandedCatDropdown by remember { mutableStateOf(false) }

    val qtyVal = quantityStr.toDoubleOrNull() ?: 0.0
    val costVal = unitCostStr.toDoubleOrNull() ?: 0.0
    val autoStockValue = qtyVal * costVal

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
                    text = "Add Store Item",
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif),
                    color = TextPrimary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name *") },
                    placeholder = { Text("e.g. Layers Mash 50kg") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCatDropdown,
                    onExpandedChange = { expandedCatDropdown = !expandedCatDropdown }
                ) {
                    OutlinedTextField(
                        value = labelOf(INVENTORY_CATEGORIES, selectedCategory),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCatDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCatDropdown,
                        onDismissRequest = { expandedCatDropdown = false }
                    ) {
                        INVENTORY_CATEGORIES.forEach { (key, label) ->
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit *") },
                        placeholder = { Text("e.g. bag") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Stock on hand") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = reorderLevelStr,
                        onValueChange = { reorderLevelStr = it },
                        label = { Text("Alert me below") },
                        placeholder = { Text("e.g. 5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unitCostStr,
                        onValueChange = { unitCostStr = it },
                        label = { Text("Unit Cost (UGX)") },
                        placeholder = { Text("e.g. 85000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (autoStockValue > 0) {
                    Surface(
                        color = SurfaceButter,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = WarmAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Total stock value", style = MaterialTheme.typography.bodySmall, color = MutedText)
                            }
                            Text(
                                text = money(autoStockValue),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DeepEspresso
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
                            if (name.isBlank()) {
                                errorMsg = "Please give this item a name"
                            } else {
                                val reorder = reorderLevelStr.toDoubleOrNull() ?: 0.0
                                isSaving = true
                                onSubmit(name, selectedCategory, unit, qtyVal, reorder, costVal)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldenYellow,
                            contentColor = DeepEspresso
                        ),
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "Saving..." else "Add Item", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockMovementFormDialog(
    items: List<InventoryItem>,
    onDismiss: () -> Unit,
    onSubmit: (item: InventoryItem, type: String, quantity: Double, date: String, note: String) -> Unit
) {
    var selectedItem by remember { mutableStateOf(items.firstOrNull()) }
    var expandedItemDropdown by remember { mutableStateOf(false) }

    var type by remember { mutableStateOf("out") }
    var quantityStr by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(todayISO()) }
    var note by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val qtyVal = quantityStr.toDoubleOrNull() ?: 0.0
    val currentQty = selectedItem?.quantity ?: 0.0
    val projectedQty = max(0.0, currentQty + if (type == "in") qtyVal else -qtyVal)

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
                    text = "Update Stock",
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif),
                    color = TextPrimary
                )

                ExposedDropdownMenuBox(
                    expanded = expandedItemDropdown,
                    onExpandedChange = { expandedItemDropdown = !expandedItemDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedItem?.let { "${it.name} (${num(it.quantity)} ${it.unit} left)" } ?: "Select item",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Which item? *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedItemDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedItemDropdown,
                        onDismissRequest = { expandedItemDropdown = false }
                    ) {
                        items.forEach { itm ->
                            DropdownMenuItem(
                                text = { Text("${itm.name} (${num(itm.quantity)} ${itm.unit})") },
                                onClick = {
                                    selectedItem = itm
                                    expandedItemDropdown = false
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
                        selected = type == "out",
                        onClick = { type = "out" },
                        label = { Text("Used / Out") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "in",
                        onClick = { type = "in" },
                        label = { Text("Received / In") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Quantity *") },
                        placeholder = { Text("e.g. 5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Automatic Balance Preview
                if (selectedItem != null && qtyVal > 0) {
                    val willNeedReorder = projectedQty <= (selectedItem?.reorderLevel ?: 0.0)
                    Surface(
                        color = SurfaceButter,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = WarmAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New stock balance", style = MaterialTheme.typography.labelSmall, color = WarmAmber, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${num(projectedQty, 1)} ${selectedItem?.unit ?: ""}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (willNeedReorder) BrickRed else DeepEspresso
                            )
                            if (willNeedReorder) {
                                Text(
                                    text = "Heads up: this puts stock at or below your reorder alert.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrickRed
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Reason / Note") },
                    placeholder = { Text("e.g. Fed to House 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
                            val itm = selectedItem
                            if (itm == null) {
                                errorMsg = "Please choose an item"
                            } else if (qtyVal <= 0.0) {
                                errorMsg = "Quantity must be greater than 0"
                            } else {
                                isSaving = true
                                onSubmit(itm, type, qtyVal, date, note)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldenYellow,
                            contentColor = DeepEspresso
                        ),
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "Saving..." else "Update Stock", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
