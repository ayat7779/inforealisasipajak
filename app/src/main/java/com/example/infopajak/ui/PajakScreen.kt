package com.example.infopajak.ui

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.infopajak.data.PajakData
import com.example.infopajak.ui.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PajakScreen(
    viewModel: PajakViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val triwulanData by viewModel.triwulanState.collectAsState()
    val isDetailLoading by viewModel.isDetailLoading.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    
    val context = LocalContext.current

    fun showDatePicker(currentDate: String, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val parts = currentDate.split("-")
        if (parts.size == 3) {
            calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TopBarGradient)
                    .statusBarsPadding()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = when {
                                    selectedCategory != null -> "Rincian $selectedCategory"
                                    activeTab == 0 -> "Dashboard KPI Realisasi"
                                    activeTab == 1 -> "Realisasi Penerimaan Daerah"
                                    else -> "Dashboard Analitik"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (selectedCategory == null) {
                                Text(
                                    text = "$startDate s/d $endDate",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (selectedCategory != null) {
                            IconButton(onClick = { viewModel.selectCategory(null) }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    actions = {
                        if (selectedCategory == null) {
                            IconButton(onClick = { viewModel.fetchData() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (selectedCategory == null) {
                NavigationBar(
                    containerColor = SurfaceWhite,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("Dashboard") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.15f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                        label = { Text("Mata Pajak") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.15f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                        label = { Text("Analitik") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.15f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            AnimatedVisibility(
                visible = selectedCategory == null && (activeTab == 0 || activeTab == 1),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                DateSelectionBar(
                    onTodayClick = {
                        val today = viewModel.getTodayDate()
                        viewModel.setDateRangeAndFetch(today, today)
                    },
                    onMonthClick = {
                        val start = viewModel.getStartOfMonthDate()
                        val today = viewModel.getTodayDate()
                        viewModel.setDateRangeAndFetch(start, today)
                    },
                    onYearClick = {
                        val start = viewModel.getStartOfYearDate()
                        val end = viewModel.getEndOfYearDate()
                        viewModel.setDateRangeAndFetch(start, end)
                    },
                    onCustomRangeClick = {
                        showDatePicker(startDate) { newStart ->
                            showDatePicker(endDate) { newEnd ->
                                viewModel.setDateRangeAndFetch(newStart, newEnd)
                            }
                        }
                    }
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (activeTab) {
                    0 -> {
                        DashboardLandingScreen(
                            viewModel = viewModel,
                            onNavigateToDetails = { category ->
                                viewModel.selectCategory(category)
                                viewModel.setActiveTab(1)
                            },
                            onNavigateToAnalytics = {
                                viewModel.setActiveTab(2)
                            }
                        )
                    }
                    1 -> {
                        if (selectedCategory != null) {
                            if (isDetailLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            } else {
                                TriwulanDetailContent(selectedCategory!!, triwulanData)
                            }
                        } else {
                            when (val state = uiState) {
                                is PajakUiState.Loading -> {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
                                is PajakUiState.Success -> {
                                    PajakContent(state.data, onCategoryClick = { viewModel.selectCategory(it) })
                                }
                                is PajakUiState.Error -> {
                                    ErrorState(state.message) { viewModel.fetchData() }
                                }
                            }
                        }
                    }
                    else -> {
                        AnalyticScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticScreen(viewModel: PajakViewModel) {
    val dailyTrend by viewModel.dailyTrend.collectAsState()
    val monthlyTrend by viewModel.monthlyTrend.collectAsState()
    val isDailyLoading by viewModel.isDailyLoading.collectAsState()
    val isMonthlyLoading by viewModel.isMonthlyLoading.collectAsState()
    val analyticError by viewModel.analyticError.collectAsState()
    val isLoading = isDailyLoading || isMonthlyLoading
    
    val selectedMonth by viewModel.selectedTrendMonth.collectAsState()
    val selectedYear by viewModel.selectedTrendYear.collectAsState()
    val selectedCategory by viewModel.selectedAnalyticCategory.collectAsState()

    val months = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val years = (2024..2026).map { it.toString() }
    val categories = listOf("Total", "PKB", "BBNKB", "PAP", "PBBKB", "Pajak Rokok", "AB")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = AccentGold,
                trackColor = PrimaryBlue.copy(alpha = 0.2f)
            )
        }
        
        analyticError?.let { error ->
            ErrorCard(message = error) {
                viewModel.setActiveTab(1)
            }
        }

        // Category Selector Card with Gradient Border & Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = PrimaryBlue.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, PrimaryBlue.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(PrimaryBlue.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    }
                    Text("Filter Kategori Analitik", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedCategory ?: "Total (Semua Pajak)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, fontWeight = if ((selectedCategory ?: "Total") == category) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        viewModel.selectAnalyticCategory(if (category == "Total") null else category)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Trend Section Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF4A148C).copy(alpha = 0.25f)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, Color(0xFF7B1FA2).copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Brush.horizontalGradient(listOf(Color(0xFF4A148C), Color(0xFF7B1FA2))), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Tren Harian", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(selectedCategory ?: "Total Realisasi", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }

                    // Month & Year Filter Badges
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Month Selector
                        var monthExpanded by remember { mutableStateOf(false) }
                        Box {
                            Surface(
                                color = Color(0xFFF3E5F5),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { monthExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(months[selectedMonth], style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6A1B9A))
                                }
                            }
                            DropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                                months.forEachIndexed { index, month ->
                                    DropdownMenuItem(
                                        text = { Text(month) },
                                        onClick = {
                                            viewModel.selectTrendMonth(index)
                                            monthExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Year Selector for Daily Trend
                        var yearExpanded by remember { mutableStateOf(false) }
                        Box {
                            Surface(
                                color = Color(0xFFF3E5F5),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { yearExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedYear.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6A1B9A))
                                }
                            }
                            DropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                                years.forEach { year ->
                                    DropdownMenuItem(
                                        text = { Text(year) },
                                        onClick = {
                                            viewModel.selectTrendYear(year.toInt())
                                            yearExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                TrendChart(data = dailyTrend, labelPrefix = "Tgl ", barColor = Color(0xFF7B1FA2))
            }
        }

        // Monthly Trend Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFE65100).copy(alpha = 0.25f)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, Color(0xFFE65100).copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Brush.horizontalGradient(listOf(Color(0xFFBF360C), Color(0xFFE65100))), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Tren Bulanan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("Tahun $selectedYear (${selectedCategory ?: "Total"})", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }

                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { expanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedYear.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFE65100))
                            }
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year) },
                                    onClick = {
                                        viewModel.selectTrendYear(year.toInt())
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                TrendChart(data = monthlyTrend, isMonthly = true, chartType = ChartType.LINE, barColor = Color(0xFFE65100))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

enum class ChartType { BAR, LINE }

@Composable
fun TrendChart(
    data: List<TrendData>,
    isMonthly: Boolean = false,
    labelPrefix: String = "",
    barColor: Color = Color(0xFF6200EE),
    chartType: ChartType = ChartType.BAR
) {
    if (data.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("Memuat data...")
        }
        return
    }

    val maxVal = (data.maxOfOrNull { it.value } ?: 0L).takeIf { it > 0 } ?: 1L
    var selectedIndex by remember { mutableStateOf(-1) }
    
    Card(
        modifier = Modifier.fillMaxWidth().height(350.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            if (selectedIndex != -1 && selectedIndex < data.size) {
                val trend = data[selectedIndex]
                val millions = trend.value / 1_000_000.0
                Text(
                    text = "${if(isMonthly) "" else "Tgl "}${trend.label}: ${formatNumber(millions)} Juta",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Text(
                    text = "Klik pada grafik untuk melihat detail",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val primaryColor = barColor
                val selectedColor = MaterialTheme.colorScheme.secondary
                val gridColor = Color.LightGray.copy(alpha = 0.5f)
                
                Canvas(
                    modifier = Modifier.fillMaxSize().pointerInput(data) {
                        detectTapGestures { offset ->
                            val spacing = size.width / data.size
                            val index = (offset.x / spacing).toInt()
                            if (index in data.indices) {
                                selectedIndex = index
                            }
                        }
                    }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height - 30.dp.toPx()
                    val spacing = canvasWidth / data.size

                    // Draw Dashed Helper Lines (Horizontal Grid)
                    val gridLines = 5
                    for (i in 0..gridLines) {
                        val yGrid = canvasHeight - (i * (canvasHeight / gridLines))
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, yGrid),
                            end = Offset(canvasWidth, yGrid),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    if (chartType == ChartType.BAR) {
                        val barWidth = spacing * 0.7f
                        data.forEachIndexed { index, trend ->
                            val barHeight = (trend.value.toDouble() / maxVal.toDouble() * canvasHeight).toFloat()
                            val x = index * spacing + (spacing - barWidth) / 2
                            val y = canvasHeight - barHeight

                            drawRect(
                                color = if (index == selectedIndex) selectedColor else primaryColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight)
                            )
                        }
                    } else {
                        // Line Chart
                        val points = data.mapIndexed { index, trend ->
                            val x = index * spacing + spacing / 2
                            val y = (canvasHeight - (trend.value.toDouble() / maxVal.toDouble() * canvasHeight)).toFloat()
                            Offset(x, y)
                        }

                        // Draw Line Path
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = primaryColor,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 3.dp.toPx()
                            )
                        }

                        // Draw Points
                        points.forEachIndexed { index, point ->
                            drawCircle(
                                color = if (index == selectedIndex) selectedColor else primaryColor,
                                radius = if (index == selectedIndex) 6.dp.toPx() else 4.dp.toPx(),
                                center = point
                            )
                        }
                    }

                    // Labels
                    data.forEachIndexed { index, trend ->
                        if (isMonthly || index % 5 == 0 || index == data.size - 1) {
                            val x = index * spacing + spacing / 2
                            drawContext.canvas.nativeCanvas.drawText(
                                "$labelPrefix${trend.label}",
                                x,
                                size.height - 5.dp.toPx(),
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.GRAY
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    textSize = 10.sp.toPx()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Gangguan Koneksi",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            TextButton(onClick = onRetry) {
                Text("Retry", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Oops! Gagal Memuat Data",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Coba Lagi")
        }
    }
}

@Composable
fun DateSelectionBar(
    onTodayClick: () -> Unit,
    onMonthClick: () -> Unit,
    onYearClick: () -> Unit,
    onCustomRangeClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = SurfaceWhite,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = onTodayClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Text("Hari Ini", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Button(
                onClick = onMonthClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkBlue),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Text("Bulan Ini", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Button(
                onClick = onYearClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Text("Tahun Ini", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
            }
            OutlinedButton(
                onClick = onCustomRangeClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp), tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(3.dp))
                Text("Range", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
        }
    }
}

@Composable
fun PajakContent(data: PajakData, onCategoryClick: (String) -> Unit) {
    val items = listOf(
        PajakItem("PKB", data.targetPkb, data.pkb?.toString(), Icons.Default.DirectionsCar, PkbLight, PkbColor, PkbGradient),
        PajakItem("BBNKB", data.targetBbnkb, data.bbnkb?.toString(), Icons.Default.SyncAlt, BbnkbLight, BbnkbColor, BbnkbGradient),
        PajakItem("PAP", data.targetAp, data.ap?.toString(), Icons.Default.WaterDrop, PapLight, PapColor, PapGradient),
        PajakItem("PBBKB", data.targetPbbkb, data.pbbkb, Icons.Default.LocalGasStation, PbbkbLight, PbbkbColor, PbbkbGradient),
        PajakItem("Pajak Rokok", data.targetPajakRokok, data.pajakRokok, Icons.Default.SmokingRooms, RokokLight, RokokColor, RokokGradient),
        PajakItem("AB", data.targetAb, data.ab?.toString(), Icons.Default.Waves, AbLight, AbColor, AbGradient)
    )

    var totalTarget = 0L
    var totalRealization = 0L

    items.forEach { item ->
        totalTarget += item.target?.toLongOrNull() ?: 0L
        totalRealization += item.realization?.toLongOrNull() ?: 0L
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TotalCard(totalTarget, totalRealization)
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .background(PrimaryBlue, RoundedCornerShape(2.dp))
            )
            Text(
                text = "Rincian Mata Pajak Daerah",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }

        items.forEach { item ->
            PajakCard(
                title = item.title,
                target = item.target,
                realization = item.realization,
                icon = item.icon,
                backgroundColor = item.color,
                accentColor = item.accentColor,
                gradient = item.gradient,
                onClick = { onCategoryClick(item.title) }
            )
        }
    }
}

@Composable
fun TriwulanDetailContent(category: String, dataList: List<TriwulanData>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Realisasi $category",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        dataList.forEachIndexed { index, item ->
            val thresholdText = when (category) {
                "PKB" -> when (index) {
                    0 -> "Target Pencapaian Target adalah 20%"
                    1 -> "Akumulasi Pencapaian Target adalah 45%"
                    2 -> "Akumulasi Pencapaian Target adalah 85%"
                    3 -> "Akumulasi Pencapaian Target adalah 100%"
                    else -> ""
                }
                "BBNKB" -> when (index) {
                    0 -> "Target Pencapaian Target adalah 20%"
                    1 -> "Akumulasi Pencapaian Target adalah 45%"
                    2 -> "Akumulasi Pencapaian Target adalah 75%"
                    3 -> "Akumulasi Pencapaian Target adalah 100%"
                    else -> ""
                }
                "PAP" -> when (index) {
                    0 -> "Target Pencapaian Target adalah 15%"
                    1 -> "Akumulasi Pencapaian Target adalah 45%"
                    2 -> "Akumulasi Pencapaian Target adalah 70%"
                    3 -> "Akumulasi Pencapaian Target adalah 100%"
                    else -> ""
                }
                "PBBKB" -> when (index) {
                    0 -> "Target Pencapaian Target adalah 20%"
                    1 -> "Akumulasi Pencapaian Target adalah 40%"
                    2 -> "Akumulasi Pencapaian Target adalah 70%"
                    3 -> "Akumulasi Pencapaian Target adalah 100%"
                    else -> ""
                }
                else -> ""
            }
            TriwulanCard(item, index + 1, thresholdText)
        }
    }
}

@Composable
fun TargetAnalysisCard(isAchieved: Boolean, note: String, percentage: Double) {
    val percentageText = String.format(Locale.getDefault(), "%.2f%%", percentage)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isAchieved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isAchieved) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isAchieved) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isAchieved) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$percentageText - $note",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isAchieved) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}

@Composable
fun TriwulanCard(item: TriwulanData, index: Int, thresholdText: String = "") {
    val targetVal = item.target?.toLongOrNull() ?: 0L
    val percentage = if (targetVal > 0) (item.realization.toDouble() / targetVal.toDouble()) else 0.0
    val percentageText = String.format(Locale.getDefault(), "%.2f%%", percentage * 100)
    
    val icon = when(index) {
        1 -> Icons.Default.Filter1
        2 -> Icons.Default.Filter2
        3 -> Icons.Default.Filter3
        else -> Icons.Default.Filter4
    }

    val (cardGradient, accentColor) = when(index) {
        1 -> Brush.linearGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))) to PrimaryBlue
        2 -> Brush.linearGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))) to Color(0xFF2E7D32)
        3 -> Brush.linearGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))) to Color(0xFFE65100)
        else -> Brush.linearGradient(listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7))) to Color(0xFF7B1FA2)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp), spotColor = accentColor.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .background(cardGradient)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(accentColor, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )
                        Text(
                            text = getQuarterMonths(index),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
                Surface(
                    color = if (percentage >= 1.0) Color(0xFF2E7D32) else accentColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = percentageText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { percentage.coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = accentColor,
                trackColor = Color.White.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Realisasi", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                Text(
                    text = formatNumber(item.realization.toString()),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            if (thresholdText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = thresholdText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun getQuarterMonths(index: Int): String = when(index) {
    1 -> "Januari - Maret"
    2 -> "April - Juni"
    3 -> "Juli - September"
    else -> "Oktober - Desember"
}

data class PajakItem(
    val title: String,
    val target: String?,
    val realization: String?,
    val icon: ImageVector,
    val color: Color,
    val accentColor: Color = Color(0xFF1565C0),
    val gradient: androidx.compose.ui.graphics.Brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(color, color))
)

@Composable
fun TotalCard(target: Long, realization: Long) {
    val percentage = if (target > 0) (realization.toDouble() / target.toDouble()) else 0.0
    val percentageText = String.format(Locale.getDefault(), "%.2f%%", percentage * 100)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFF0F2027).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(TotalPajakGradient)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFFFFD54F)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TOTAL PAJAK DAERAH",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Provinsi Riau",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                Surface(
                    color = if (percentage >= 1.0) Color(0xFF2E7D32) else Color(0xFFFF8F00),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = percentageText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { percentage.coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color(0xFFFFD54F),
                trackColor = Color.White.copy(alpha = 0.25f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Target", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                Text(
                    text = formatNumber(target.toString()),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Realisasi", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                Text(
                    text = formatNumber(realization.toString()),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F)
                )
            }
        }
    }
}

@Composable
fun PajakCard(
    title: String,
    target: String?,
    realization: String?,
    icon: ImageVector,
    backgroundColor: Color,
    accentColor: Color = Color(0xFF1565C0),
    gradient: androidx.compose.ui.graphics.Brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(backgroundColor, backgroundColor)),
    onClick: () -> Unit
) {
    val targetVal = target?.toLongOrNull() ?: 0L
    val realizationVal = realization?.toLongOrNull() ?: 0L
    val percentage = if (targetVal > 0) (realizationVal.toDouble() / targetVal.toDouble()) else 0.0
    val percentage100 = percentage * 100
    val percentageText = String.format(Locale.getDefault(), "%.2f%%", percentage100)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp), spotColor = accentColor.copy(alpha = 0.25f))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(gradient, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
                Surface(
                    color = if (percentage >= 1.0) Color(0xFF2E7D32) else accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = percentageText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (percentage >= 1.0) Color.White else accentColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { percentage.coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = accentColor,
                trackColor = backgroundColor.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Target", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Text(
                    text = formatNumber(target),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Realisasi", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Text(
                    text = formatNumber(realization),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            // Achievement Note integrated into Beranda Card
            getAchievementData(title, percentage100)?.let { (isAchieved, note) ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = if (isAchieved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAchieved) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isAchieved) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = note,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAchieved) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}

private fun getAchievementData(category: String, percentage: Double): Pair<Boolean, String>? {
    val tw1Threshold = if (category == "PAP") 15.0 else 20.0
    val tw2Threshold = if (category == "PBBKB") 40.0 else 45.0
    val tw3Threshold = when (category) {
        "PKB" -> 85.0
        "BBNKB" -> 75.0
        "PAP" -> 70.0
        "PBBKB" -> 70.0
        else -> return null
    }

    return when {
        percentage >= 100.0 -> true to "Telah Tercapai Target Triwulan IV"
        percentage >= tw3Threshold -> true to "Telah Tercapai Target Triwulan III"
        percentage >= tw2Threshold -> true to "Telah Tercapai Target Triwulan II"
        percentage >= tw1Threshold -> true to "Telah Tercapai Target Triwulan I"
        else -> false to "Belum Tercapai Target"
    }
}

fun formatNumber(value: Any?): String {
    if (value == null || value == "") return "-"
    return try {
        val num = when (value) {
            is String -> value.toDoubleOrNull() ?: return value
            is Number -> value.toDouble()
            else -> return value.toString()
        }
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
        val formatter = DecimalFormat("Rp #,###.##", symbols)
        formatter.format(num)
    } catch (e: Exception) {
        value.toString()
    }
}
