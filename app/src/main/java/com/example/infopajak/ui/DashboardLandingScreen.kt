package com.example.infopajak.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infopajak.data.PajakData
import com.example.infopajak.ui.theme.*
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
            .background(BackgroundLight)
    ) {
        when (val state = uiState) {
            is PajakUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memuat Dashboard KPI Realisasi...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
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
        // Hero Header Card - Royal Executive Blue Gradient
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), spotColor = PrimaryDarkBlue.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0A2540),
                                Color(0xFF1565C0),
                                Color(0xFF1E88E5)
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
                        Surface(
                            color = Color(0xFFFFB300),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "DASHBOARD EKSEKUTIF",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Info Realisasi Pajak Daerah",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Badan Pendapatan Daerah Provinsi Riau",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    IconButton(
                        onClick = onRefresh,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date badge
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Periode: $startDate s/d $endDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
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
                        color = if (totalPercentage >= 100.0) Color(0xFF2E7D32) else Color(0xFFFF8F00),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (totalPercentage >= 100.0) "TARGET TERCAPAI" else "DALAM PROSES",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { (totalPercentage / 100.0).coerceIn(0.0, 1.0).toFloat() },
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
                text = "Indikator Kinerja Utama (KPI)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }

        // 4 KPI Cards Grid with Distinct Gradients
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LandingKpiCard(
                modifier = Modifier.weight(1f),
                title = "Efektivitas Capaian",
                value = String.format(Locale.getDefault(), "%.2f%%", totalPercentage),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                accentColor = if (totalPercentage >= 100.0) Color(0xFF2E7D32) else PrimaryBlue,
                gradient = Brush.linearGradient(
                    if (totalPercentage >= 100.0) listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                    else listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                ),
                statusText = if (totalPercentage >= 100.0) "Sangat Efektif" else "Berprogres Positif",
                description = "Realisasi vs Target"
            )
            LandingKpiCard(
                modifier = Modifier.weight(1f),
                title = if (totalGap <= 0) "Surplus Realisasi" else "Sisa Target (Gap)",
                value = formatNumber(abs(totalGap)),
                icon = if (totalGap <= 0) Icons.Default.CheckCircle else Icons.Default.PieChart,
                accentColor = if (totalGap <= 0) Color(0xFF2E7D32) else Color(0xFFE65100),
                gradient = Brush.linearGradient(
                    if (totalGap <= 0) listOf(Color(0xFFE8F5E9), Color(0xFFA5D6A7))
                    else listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))
                ),
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
                accentColor = Color(0xFF0288D1),
                gradient = Brush.linearGradient(listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC))),
                statusText = topPerformer?.let { formatNumber(it.third) } ?: "-",
                description = "Mata Pajak Tertinggi"
            )
            LandingKpiCard(
                modifier = Modifier.weight(1f),
                title = "Pajak Tuntas 100%",
                value = "$achievedCount / ${items.size} Pajak",
                icon = Icons.AutoMirrored.Filled.FactCheck,
                accentColor = Color(0xFF7B1FA2),
                gradient = Brush.linearGradient(listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7))),
                statusText = lowestPerformer?.let { "${it.first.title}: ${String.format(Locale.getDefault(), "%.1f%%", it.second)}" } ?: "-",
                description = "Capaian Target Penuh"
            )
        }

        // Quick Navigation to Details / Analytics Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF94A3B8)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
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
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    TextButton(onClick = { onNavigateToDetails(null) }) {
                        Text("Lihat Semua", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                    }
                }

                // Mini preview cards of top 3 pajak with colored gradient accents
                performanceList.take(3).forEach { (item, pct, realVal) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetails(item.title) }
                            .border(1.dp, item.accentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = item.color.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(item.gradient, shape = RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(item.icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color.White)
                                }
                                Column {
                                    Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextDark)
                                    Text(
                                        formatNumber(realVal),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%.2f%%", pct),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pct >= 100.0) Color(0xFF2E7D32) else item.accentColor
                                )
                                Text(
                                    text = if (pct >= 100.0) "Tercapai" else "Proses",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons Row with Gradient Highlights
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onNavigateToDetails(null) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buka Rincian", color = Color.White, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onNavigateToAnalytics,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF7B1FA2), Color(0xFFE65100)))),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite)
            ) {
                Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF7B1FA2))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analisis Tren", color = Color(0xFF7B1FA2), fontWeight = FontWeight.Bold)
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
    gradient: Brush,
    statusText: String,
    description: String
) {
    Card(
        modifier = modifier.shadow(3.dp, RoundedCornerShape(14.dp), spotColor = accentColor.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(accentColor.copy(alpha = 0.18f), shape = RoundedCornerShape(6.dp)),
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
                    color = TextDark,
                    fontWeight = FontWeight.SemiBold,
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

            HorizontalDivider(color = accentColor.copy(alpha = 0.25f), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextMuted,
                    maxLines = 1
                )
            }
        }
    }
}
