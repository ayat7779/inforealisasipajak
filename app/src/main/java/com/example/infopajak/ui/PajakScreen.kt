package com.example.infopajak.ui

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when {
                                selectedCategory != null -> "Rincian $selectedCategory"
                                activeTab == 1 -> "Dashboard Analitik"
                                else -> "Realisasi Penerimaan Daerah"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (selectedCategory == null && activeTab == 0) {
                            Text(
                                text = "$startDate s/d $endDate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (selectedCategory != null) {
                        IconButton(onClick = { viewModel.selectCategory(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    if (selectedCategory == null && activeTab == 0) {
                        IconButton(onClick = { viewModel.fetchData() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (selectedCategory == null) {
                NavigationBar {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Beranda") }
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                        label = { Text("Analitik") }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (activeTab == 0) {
                AnimatedVisibility(
                    visible = selectedCategory == null,
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
            } else {
                AnalyticScreen(viewModel)
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

    val months = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val years = (2024..2026).map { it.toString() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        
        analyticError?.let { error ->
            ErrorCard(message = error) {
                viewModel.setActiveTab(1) // Just to trigger refresh feel if needed, though buttons do it
            }
        }

        // Daily Trend Section
        Text("Tren Harian (Total Realisasi)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Month Selector
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text("Bulan: ", style = MaterialTheme.typography.bodySmall)
                var monthExpanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { monthExpanded = true }, contentPadding = PaddingValues(0.dp)) {
                        Text(months[selectedMonth], style = MaterialTheme.typography.bodyMedium)
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
            }

            // Year Selector for Daily Trend
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text("Tahun: ", style = MaterialTheme.typography.bodySmall)
                var yearExpanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { yearExpanded = true }, contentPadding = PaddingValues(0.dp)) {
                        Text(selectedYear.toString(), style = MaterialTheme.typography.bodyMedium)
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
        TrendChart(data = dailyTrend, labelPrefix = "Tgl ")

        HorizontalDivider()

        // Monthly Trend Section
        Text("Tren Bulanan Tahun $selectedYear", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pilih Tahun: ", style = MaterialTheme.typography.bodyMedium)
            var expanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(selectedYear.toString())
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
        TrendChart(data = monthlyTrend, isMonthly = true)
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun TrendChart(data: List<TrendData>, isMonthly: Boolean = false, labelPrefix: String = "") {
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
                val barColor = Color(0xFF6200EE)
                val selectedBarColor = MaterialTheme.colorScheme.secondary
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
                    val barWidth = (canvasWidth / data.size) * 0.7f
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

                    data.forEachIndexed { index, trend ->
                        val barHeight = (trend.value.toDouble() / maxVal.toDouble() * canvasHeight).toFloat()
                        val x = index * spacing + (spacing - barWidth) / 2
                        val y = canvasHeight - barHeight

                        drawRect(
                            color = if (index == selectedIndex) selectedBarColor else barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight)
                        )

                        // Labels
                        if (isMonthly || index % 5 == 0 || index == data.size - 1) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "$labelPrefix${trend.label}",
                                x + barWidth / 2,
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
    onCustomRangeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onTodayClick,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Text("Hari Ini", style = MaterialTheme.typography.labelSmall)
        }
        Button(
            onClick = onMonthClick,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Text("Bulan Ini", style = MaterialTheme.typography.labelSmall)
        }
        OutlinedButton(
            onClick = onCustomRangeClick,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Range", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun PajakContent(data: PajakData, onCategoryClick: (String) -> Unit) {
    val items = listOf(
        PajakItem("PKB", data.targetPkb, data.pkb?.toString(), Icons.Default.DirectionsCar, Color(0xFFE3F2FD)), // Blue
        PajakItem("BBNKB", data.targetBbnkb, data.bbnkb?.toString(), Icons.Default.SyncAlt, Color(0xFFE8F5E9)), // Green
        PajakItem("PAP", data.targetAp, data.ap?.toString(), Icons.Default.WaterDrop, Color(0xFFE0F7FA)), // Cyan
        PajakItem("PBBKB", data.targetPbbkb, data.pbbkb, Icons.Default.LocalGasStation, Color(0xFFFFF8E1)), // Amber
        PajakItem("Pajak Rokok", data.targetPajakRokok, data.pajakRokok, Icons.Default.SmokingRooms, Color(0xFFFFEBEE)), // Red
        PajakItem("AB", data.targetAb, data.ab?.toString(), Icons.Default.Waves, Color(0xFFF3E5F5)) // Purple
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TotalCard(totalTarget, totalRealization)
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        items.forEach { item ->
            PajakCard(
                title = item.title,
                target = item.target,
                realization = item.realization,
                icon = item.icon,
                backgroundColor = item.color,
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
            TriwulanCard(item, index + 1)
        }
    }
}

@Composable
fun TriwulanCard(item: TriwulanData, index: Int) {
    val targetVal = item.target?.toLongOrNull() ?: 0L
    val percentage = if (targetVal > 0) (item.realization.toDouble() / targetVal.toDouble()) else 0.0
    val percentageText = String.format(Locale.getDefault(), "%.2f%%", percentage * 100)
    
    val icon = when(index) {
        1 -> Icons.Default.Filter1
        2 -> Icons.Default.Filter2
        3 -> Icons.Default.Filter3
        else -> Icons.Default.Filter4
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getQuarterMonths(index),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Text(
                    text = percentageText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { percentage.coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Realisasi", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatNumber(item.realization.toString()),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
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
    val color: Color
)

@Composable
fun TotalCard(target: Long, realization: Long) {
    val percentage = if (target > 0) (realization.toDouble() / target.toDouble()) else 0.0
    val percentageText = String.format(Locale.getDefault(), "%.2f%%", percentage * 100)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TOTAL PAJAK",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = percentageText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { percentage.coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier.fillMaxWidth().height(12.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Target", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatNumber(target.toString()),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Realisasi", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatNumber(realization.toString()),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PajakCard(title: String, target: String?, realization: String?, icon: ImageVector, backgroundColor: Color, onClick: () -> Unit) {
    val targetVal = target?.toLongOrNull() ?: 0L
    val realizationVal = realization?.toLongOrNull() ?: 0L
    val percentage = if (targetVal > 0) (realizationVal.toDouble() / targetVal.toDouble()) else 0.0
    val percentageText = String.format(Locale.getDefault(), "%.2f%%", percentage * 100)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black // Ensuring legibility on colored cards
                    )
                }
                Text(
                    text = percentageText,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (percentage >= 1.0) MaterialTheme.colorScheme.primary else Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { percentage.coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.LightGray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Target", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                Text(
                    text = formatNumber(target),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Realisasi", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                Text(
                    text = formatNumber(realization),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }
        }
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
