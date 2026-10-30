package com.example.infopajak.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infopajak.R
import com.example.infopajak.data.PajakData
import java.util.*
import kotlin.math.abs

@Composable
fun DashboardLandingScreen(
    viewModel: PajakViewModel,
    onNavigateToDetails: (String?) -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is PajakUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memuat Dashboard KPI Realisasi...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            is PajakUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(state.message) { viewModel.fetchData() }
                }
            }
            is PajakUiState.Success -> {
                DashboardLandingContent(
                    data = state.data,
                    startDate = startDate,
                    endDate = endDate,
                    onNavigateToDetails = onNavigateToDetails,
                    onNavigateToAnalytics = onNavigateToAnalytics,
                    onRefresh = { viewModel.fetchData() }
                )
            }
        }
    }
}

@Composable
fun DashboardLandingContent(
    data: PajakData,
    startDate: String,
    endDate: String,
    onNavigateToDetails: (String?) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onRefresh: () -> Unit
) {
    val items = listOf(
        PajakItem("PKB", data.targetPkb, data.pkb?.toString(), Icons.Default.DirectionsCar, Color(0xFFE3F2FD)),
        PajakItem("BBNKB", data.targetBbnkb, data.bbnkb?.toString(), Icons.Default.SyncAlt, Color(0xFFE8F5E9)),
        PajakItem("PAP", data.targetAp, data.ap?.toString(), Icons.Default.WaterDrop, Color(0xFFE0F7FA)),
        PajakItem("PBBKB", data.targetPbbkb, data.pbbkb, Icons.Default.LocalGasStation, Color(0xFFFFF8E1)),
        PajakItem("Pajak Rokok", data.targetPajakRokok, data.pajakRokok, Icons.Default.SmokingRooms, Color(0xFFFFEBEE)),
        PajakItem("AB", data.targetAb, data.ab?.toString(), Icons.Default.Waves, Color(0xFFF3E5F5))
    )

    var totalTarget = 0L
    var totalRealization = 0L

    items.forEach { item ->
        totalTarget += item.target?.toLongOrNull() ?: 0L
        totalRealization += item.realization?.toLongOrNull() ?: 0L
    }

    val totalPercentage = if (totalTarget > 0) (totalRealization.toDouble() / totalTarget.toDouble()) * 100 else 0.0
    val totalGap = totalTarget - totalRealization

    val performanceList = items.map { item ->
        val targetVal = item.target?.toLongOrNull() ?: 0L
        val realVal = item.realization?.toLongOrNull() ?: 0L
        val pct = if (targetVal > 0) (realVal.toDouble() / targetVal.toDouble()) * 100 else 0.0
        Triple(item, pct, realVal)
    }.sortedByDescending { it.second }

    val topPerformer = performanceList.firstOrNull()
    val lowestPerformer = performanceList.lastOrNull()
    val achievedCount = performanceList.count { it.second >= 100.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DASHBOARD EKSEKUTIF",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Info Realisasi Pajak Daerah",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Bapenda Provinsi Riau",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    IconButton(
                        onClick = onRefresh,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date badge
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Periode: $startDate s/d $endDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Big Progress Percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Total Capaian Realisasi",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f%%", totalPercentage),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Surface(
                        color = if (totalPercentage >= 100.0) Color(0xFF2E7D32) else Color.White.copy(alpha = 0.25f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = if (totalPercentage >= 100.0) "TARGET TERCAPAI" else "DALAM PROSES",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (totalPercentage / 100.0).coerceIn(0.0, 1.0).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = Color(0xFFFFD54F),
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Target",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = formatNumber(totalTarget),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Realisasi",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = formatNumber(totalRealization),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }
            }
        }

        // Section Title
        Text(
            text = "Indikator Kinerja Utama (KPI)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 4 KPI Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LandingKpiCard(
                modifier = Modifier.weight(1f),
                title = "Efektivitas Capaian",
                value = String.format(Locale.getDefault(), "%.2f%%", totalPercentage),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                accentColor = if (totalPercentage >= 100.0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                statusText = if (totalPercentage >= 100.0) "Sangat Efektif" else "Berprogres Positif",
                description = "Realisasi vs Target"
            )
            LandingKpiCard(
                modifier = Modifier.weight(1f),
                title = if (totalGap <= 0) "Surplus Realisasi" else "Sisa Target (Gap)",
                value = formatNumber(abs(totalGap)),
                icon = if (totalGap <= 0) Icons.Default.CheckCircle else Icons.Default.PieChart,
                accentColor = if (totalGap <= 0) Color(0xFF2E7D32) else Color(0xFFE65100),
                statusText = if (totalGap <= 0) "Melampaui Target" else "Perlu Dikejar",
                description = if (totalGap <= 0) "Surplus Anggaran" else "Kekurangan Penerimaan"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LandingKpiCard(
                modifier = Modifier.weight(1f),
                title = "Kinerja Terbaik",
                value = topPerformer?.let { "${it.first.title} (${String.format(Locale.getDefault(), "%.1f%%", it.second)})" } ?: "-",
                icon = Icons.Default.Stars,
                accentColor = Color(0xFF1976D2),
                statusText = topPerformer?.let { formatNumber(it.third) } ?: "-",
                description = "Mata Pajak Tertinggi"
            )
            LandingKpiCard(
                modifier = Modifier.weight(1f),
                title = "Pajak Tuntas 100%",
                value = "$achievedCount / ${items.size} Pajak",
                icon = Icons.AutoMirrored.Filled.FactCheck,
                accentColor = Color(0xFF7B1FA2),
                statusText = lowestPerformer?.let { "${it.first.title}: ${String.format(Locale.getDefault(), "%.1f%%", it.second)}" } ?: "-",
                description = "Capaian Target Penuh"
            )
        }

        // Quick Navigation to Details / Analytics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ringkasan Per Mata Pajak",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { onNavigateToDetails(null) }) {
                        Text("Lihat Semua")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                // Mini preview cards of top 3 pajak
                performanceList.take(3).forEach { (item, pct, realVal) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetails(item.title) },
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(item.color, shape = MaterialTheme.shapes.small),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(item.icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Black)
                                }
                                Column {
                                    Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        formatNumber(realVal),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%.2f%%", pct),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pct >= 100.0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (pct >= 100.0) "Tercapai" else "Proses",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onNavigateToDetails(null) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buka Rincian")
            }

            OutlinedButton(
                onClick = onNavigateToAnalytics,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analisis Tren")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LandingKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    statusText: String,
    description: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(accentColor.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                maxLines = 1
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
        }
    }
}
