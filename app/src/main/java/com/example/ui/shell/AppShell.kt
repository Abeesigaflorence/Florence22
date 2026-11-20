package com.example.ui.shell

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.*
import com.example.data.repo.*
import com.example.domain.CURRENCY
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.expenses.ExpenseFormDialog
import com.example.ui.expenses.ExpensesScreen
import com.example.ui.flocks.FlockDetailScreen
import com.example.ui.flocks.FlockFormDialog
import com.example.ui.flocks.FlockListScreen
import com.example.ui.inventory.InventoryItemFormDialog
import com.example.ui.inventory.InventoryScreen
import com.example.ui.inventory.StockMovementFormDialog
import com.example.ui.production.ProductionFormDialog
import com.example.ui.production.ProductionScreen
import com.example.ui.reports.ReportsScreen
import com.example.ui.sales.SaleFormDialog
import com.example.ui.sales.SalesScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class Screen {
    DASHBOARD,
    FLOCKS,
    FLOCK_DETAIL,
    PRODUCTION,
    SALES,
    EXPENSES,
    INVENTORY,
    REPORTS,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    user: AppUser?,
    flockRepo: FlockRepository,
    productionRepo: ProductionRepository,
    salesRepo: SalesRepository,
    expenseRepo: ExpenseRepository,
    inventoryRepo: InventoryRepository,
    onSignOutClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Observables
    val flocks by flockRepo.observeFlocks().collectAsState(initial = emptyList())
    val productionRecords by productionRepo.observeAllProduction().collectAsState(initial = emptyList())
    val sales by salesRepo.observeSales().collectAsState(initial = emptyList())
    val expenses by expenseRepo.observeExpenses().collectAsState(initial = emptyList())
    val inventoryItems by inventoryRepo.observeItems().collectAsState(initial = emptyList())
    val stockMovements by inventoryRepo.observeMovements().collectAsState(initial = emptyList())

    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var selectedFlockId by remember { mutableStateOf<String?>(null) }
    var preselectedFlockForProduction by remember { mutableStateOf<Flock?>(null) }

    // Dialog form states
    var showFlockDialog by remember { mutableStateOf(false) }
    var showProductionDialog by remember { mutableStateOf(false) }
    var showSaleDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showInventoryItemDialog by remember { mutableStateOf(false) }
    var showStockMovementDialog by remember { mutableStateOf(false) }

    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val navBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = DeepEspresso,
        selectedTextColor = DeepEspresso,
        indicatorColor = GoldenYellowSoft,
        unselectedIconColor = MutedText,
        unselectedTextColor = MutedText
    )

    val navRailItemColors = NavigationRailItemDefaults.colors(
        selectedIconColor = DeepEspresso,
        selectedTextColor = DeepEspresso,
        indicatorColor = GoldenYellowSoft,
        unselectedIconColor = MutedText,
        unselectedTextColor = MutedText
    )

    val drawerItemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = SurfaceButter,
        selectedIconColor = DeepEspresso,
        selectedTextColor = DeepEspresso,
        unselectedIconColor = MutedText,
        unselectedTextColor = MutedText
    )

    // Back handler: pop sub-screens or return to Dashboard
    BackHandler(enabled = currentScreen != Screen.DASHBOARD) {
        if (currentScreen == Screen.FLOCK_DETAIL) {
            currentScreen = Screen.FLOCKS
        } else {
            currentScreen = Screen.DASHBOARD
        }
    }

    // Modal drawer content for secondary destinations & quick profile switch
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isTablet,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = WarmCream,
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Brand Header & Logged-in Details in Drawer
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GoldenYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "Royo Farm logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Royo Farm",
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
                            color = TextPrimary
                        )
                        Text(
                            text = if (user?.role == "ADMIN") "Farm owner" else "Farm staff",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarmAmber,
                            fontWeight = FontWeight.Bold
                        )
                        if (!user?.email.isNullOrBlank()) {
                            Text(
                                text = user?.email ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Home") },
                    selected = currentScreen == Screen.DASHBOARD,
                    onClick = {
                        currentScreen = Screen.DASHBOARD
                        scope.launch { drawerState.close() }
                    },
                    colors = drawerItemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Pets, contentDescription = null) },
                    label = { Text("Flocks") },
                    selected = currentScreen == Screen.FLOCKS || currentScreen == Screen.FLOCK_DETAIL,
                    onClick = {
                        currentScreen = Screen.FLOCKS
                        scope.launch { drawerState.close() }
                    },
                    colors = drawerItemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Egg, contentDescription = null) },
                    label = { Text("Daily Eggs") },
                    selected = currentScreen == Screen.PRODUCTION,
                    onClick = {
                        currentScreen = Screen.PRODUCTION
                        scope.launch { drawerState.close() }
                    },
                    colors = drawerItemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    label = { Text("Sales") },
                    selected = currentScreen == Screen.SALES,
                    onClick = {
                        currentScreen = Screen.SALES
                        scope.launch { drawerState.close() }
                    },
                    colors = drawerItemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                    label = { Text("Expenses") },
                    selected = currentScreen == Screen.EXPENSES,
                    onClick = {
                        currentScreen = Screen.EXPENSES
                        scope.launch { drawerState.close() }
                    },
                    colors = drawerItemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                    label = { Text("Feed & Store") },
                    selected = currentScreen == Screen.INVENTORY,
                    onClick = {
                        currentScreen = Screen.INVENTORY
                        scope.launch { drawerState.close() }
                    },
                    colors = drawerItemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Reports") },
                    selected = currentScreen == Screen.REPORTS,
                    onClick = {
                        currentScreen = Screen.REPORTS
                        scope.launch { drawerState.close() }
                    },
                    colors = drawerItemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Profile & Team") },
                    selected = currentScreen == Screen.SETTINGS,
                    onClick = {
                        currentScreen = Screen.SETTINGS
                        scope.launch { drawerState.close() }
                    },
                    colors = drawerItemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = CardBorder)

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = BrickRed) },
                    label = { Text("Sign out", color = BrickRed) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSignOutClick()
                    },
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Tablet Navigation Rail
            if (isTablet) {
                NavigationRail(
                    containerColor = SurfaceWhite,
                    contentColor = TextPrimary,
                    header = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldenYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "Royo Farm",
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                ) {
                    NavigationRailItem(
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("Home") },
                        selected = currentScreen == Screen.DASHBOARD,
                        onClick = { currentScreen = Screen.DASHBOARD },
                        colors = navRailItemColors
                    )
                    NavigationRailItem(
                        icon = { Icon(Icons.Default.Pets, contentDescription = null) },
                        label = { Text("Flocks") },
                        selected = currentScreen == Screen.FLOCKS || currentScreen == Screen.FLOCK_DETAIL,
                        onClick = { currentScreen = Screen.FLOCKS },
                        colors = navRailItemColors
                    )
                    NavigationRailItem(
                        icon = { Icon(Icons.Default.Egg, contentDescription = null) },
                        label = { Text("Eggs") },
                        selected = currentScreen == Screen.PRODUCTION,
                        onClick = { currentScreen = Screen.PRODUCTION },
                        colors = navRailItemColors
                    )
                    NavigationRailItem(
                        icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        label = { Text("Sales") },
                        selected = currentScreen == Screen.SALES,
                        onClick = { currentScreen = Screen.SALES },
                        colors = navRailItemColors
                    )
                    NavigationRailItem(
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                        label = { Text("Costs") },
                        selected = currentScreen == Screen.EXPENSES,
                        onClick = { currentScreen = Screen.EXPENSES },
                        colors = navRailItemColors
                    )
                    NavigationRailItem(
                        icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                        label = { Text("Store") },
                        selected = currentScreen == Screen.INVENTORY,
                        onClick = { currentScreen = Screen.INVENTORY },
                        colors = navRailItemColors
                    )
                    NavigationRailItem(
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = { Text("Reports") },
                        selected = currentScreen == Screen.REPORTS,
                        onClick = { currentScreen = Screen.REPORTS },
                        colors = navRailItemColors
                    )
                    NavigationRailItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Team") },
                        selected = currentScreen == Screen.SETTINGS,
                        onClick = { currentScreen = Screen.SETTINGS },
                        colors = navRailItemColors
                    )
                }
            }

            // Main Screen Scaffold
            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = WarmCream,
                topBar = {
                    Column {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(GoldenYellow),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_app_logo),
                                            contentDescription = "Royo Farm Logo",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Royo Farm",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = DeepEspresso
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = GoldenYellowSoft,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = CURRENCY,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DeepEspresso,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            },
                            navigationIcon = {
                                if (!isTablet) {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Open menu", tint = DeepEspresso)
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = WarmCream
                            )
                        )
                        HorizontalDivider(color = CardBorder)
                    }
                },
                bottomBar = {
                    if (!isTablet) {
                        Column {
                            HorizontalDivider(color = CardBorder)
                            NavigationBar(
                                containerColor = SurfaceWhite,
                                contentColor = DeepEspresso,
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                                    label = { Text("Home") },
                                    selected = currentScreen == Screen.DASHBOARD,
                                    onClick = { currentScreen = Screen.DASHBOARD },
                                    colors = navBarItemColors
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Pets, contentDescription = null) },
                                    label = { Text("Flocks") },
                                    selected = currentScreen == Screen.FLOCKS || currentScreen == Screen.FLOCK_DETAIL,
                                    onClick = { currentScreen = Screen.FLOCKS },
                                    colors = navBarItemColors
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Egg, contentDescription = null) },
                                    label = { Text("Eggs") },
                                    selected = currentScreen == Screen.PRODUCTION,
                                    onClick = { currentScreen = Screen.PRODUCTION },
                                    colors = navBarItemColors
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                                    label = { Text("Sales") },
                                    selected = currentScreen == Screen.SALES,
                                    onClick = { currentScreen = Screen.SALES },
                                    colors = navBarItemColors
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                                    label = { Text("Reports") },
                                    selected = currentScreen == Screen.REPORTS,
                                    onClick = { currentScreen = Screen.REPORTS },
                                    colors = navBarItemColors
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentScreen) {
                        Screen.DASHBOARD -> DashboardScreen(
                            user = user,
                            flocks = flocks,
                            productionRecords = productionRecords,
                            sales = sales,
                            expenses = expenses,
                            onLogProductionClick = {
                                preselectedFlockForProduction = null
                                showProductionDialog = true
                            },
                            onNavigateToFlocks = { currentScreen = Screen.FLOCKS },
                            onNavigateToReports = { currentScreen = Screen.REPORTS },
                            onFlockClick = { id ->
                                selectedFlockId = id
                                currentScreen = Screen.FLOCK_DETAIL
                            }
                        )

                        Screen.FLOCKS -> FlockListScreen(
                            flocks = flocks,
                            onFlockClick = { id ->
                                selectedFlockId = id
                                currentScreen = Screen.FLOCK_DETAIL
                            },
                            onNewFlockClick = { showFlockDialog = true }
                        )

                        Screen.FLOCK_DETAIL -> FlockDetailScreen(
                            flockId = selectedFlockId ?: "",
                            flocks = flocks,
                            productionRecords = productionRecords,
                            sales = sales,
                            expenses = expenses,
                            onBackClick = { currentScreen = Screen.FLOCKS },
                            onLogProductionClick = { f ->
                                preselectedFlockForProduction = f
                                showProductionDialog = true
                            }
                        )

                        Screen.PRODUCTION -> ProductionScreen(
                            flocks = flocks,
                            records = productionRecords,
                            onLogProductionClick = {
                                preselectedFlockForProduction = null
                                showProductionDialog = true
                            }
                        )

                        Screen.SALES -> SalesScreen(
                            sales = sales,
                            flocks = flocks,
                            onRecordSaleClick = { showSaleDialog = true }
                        )

                        Screen.EXPENSES -> ExpensesScreen(
                            expenses = expenses,
                            flocks = flocks,
                            onAddExpenseClick = { showExpenseDialog = true }
                        )

                        Screen.INVENTORY -> InventoryScreen(
                            items = inventoryItems,
                            movements = stockMovements,
                            onNewItemClick = { showInventoryItemDialog = true },
                            onMovementClick = { showStockMovementDialog = true }
                        )

                        Screen.REPORTS -> ReportsScreen(
                            flocks = flocks,
                            productionRecords = productionRecords,
                            sales = sales,
                            expenses = expenses,
                            onFlockClick = { id ->
                                selectedFlockId = id
                                currentScreen = Screen.FLOCK_DETAIL
                            }
                        )

                        Screen.SETTINGS -> SettingsScreen(
                            user = user,
                            flocksCount = flocks.size,
                            productionCount = productionRecords.size,
                            salesCount = sales.size,
                            expensesCount = expenses.size,
                            inventoryCount = inventoryItems.size,
                            onSignOutClick = onSignOutClick
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showFlockDialog) {
        FlockFormDialog(
            onDismiss = { showFlockDialog = false },
            onSubmit = { code, breed, type, house, placementDate, count, notes ->
                scope.launch {
                    try {
                        flockRepo.createFlock(code, breed, type, house, placementDate, count, notes)
                        Toast.makeText(context, "Flock saved!", Toast.LENGTH_SHORT).show()
                        showFlockDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not save: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (showProductionDialog) {
        ProductionFormDialog(
            flocks = flocks,
            initialFlock = preselectedFlockForProduction,
            onDismiss = { showProductionDialog = false },
            onSubmit = { flock, date, eggs, broken, mortality, feed, notes ->
                scope.launch {
                    try {
                        productionRepo.logProduction(flock, date, eggs, broken, mortality, feed, notes)
                        Toast.makeText(context, "Daily eggs saved!", Toast.LENGTH_SHORT).show()
                        showProductionDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not save: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (showSaleDialog) {
        SaleFormDialog(
            flocks = flocks,
            onDismiss = { showSaleDialog = false },
            onSubmit = { date, product, desc, qty, unit, uPrice, total, customer, flockId, flockCode, paymentStatus ->
                scope.launch {
                    try {
                        salesRepo.recordSale(date, product, desc, qty, unit, uPrice, total, customer, flockId, flockCode, paymentStatus)
                        Toast.makeText(context, "Sale recorded!", Toast.LENGTH_SHORT).show()
                        showSaleDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not save: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (showExpenseDialog) {
        ExpenseFormDialog(
            flocks = flocks,
            onDismiss = { showExpenseDialog = false },
            onSubmit = { date, category, desc, amount, vendor, flockId, flockCode ->
                scope.launch {
                    try {
                        expenseRepo.recordExpense(date, category, desc, amount, vendor, flockId, flockCode)
                        Toast.makeText(context, "Expense saved!", Toast.LENGTH_SHORT).show()
                        showExpenseDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not save: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (showInventoryItemDialog) {
        InventoryItemFormDialog(
            onDismiss = { showInventoryItemDialog = false },
            onSubmit = { name, category, unit, quantity, reorder, cost ->
                scope.launch {
                    try {
                        inventoryRepo.addItem(name, category, unit, quantity, reorder, cost)
                        Toast.makeText(context, "Added to store!", Toast.LENGTH_SHORT).show()
                        showInventoryItemDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not save: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (showStockMovementDialog) {
        StockMovementFormDialog(
            items = inventoryItems,
            onDismiss = { showStockMovementDialog = false },
            onSubmit = { item, type, quantity, date, note ->
                scope.launch {
                    try {
                        inventoryRepo.recordMovement(item, type, quantity, date, note)
                        val msg = if (type == "in") "Stock added!" else "Stock updated!"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        showStockMovementDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not save: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}
