package com.example.infopajak.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary & Accent Brand Colors (Bapenda Riau Identity)
val PrimaryBlue = Color(0xFF1565C0)
val PrimaryDarkBlue = Color(0xFF0D47A1)
val AccentTeal = Color(0xFF00897B)
val AccentCyan = Color(0xFF0288D1)
val AccentGold = Color(0xFFFFB300)

val BackgroundLight = Color(0xFFF1F5F9) // Slate-100
val SurfaceWhite = Color(0xFFFFFFFF)
val TextDark = Color(0xFF0F172A) // Slate-900
val TextMuted = Color(0xFF64748B) // Slate-500

// Module Specific Gradient Palettes
// 1. Dashboard Executive Gradient
val DashboardGradStart = Color(0xFF0D47A1) // Deep Royal Blue
val DashboardGradEnd = Color(0xFF1976D2)   // Vibrant Blue
val DashboardGradient = Brush.linearGradient(listOf(DashboardGradStart, DashboardGradEnd))

// 2. Mata Pajak Total Card Gradient
val MataPajakTotalStart = Color(0xFF0F2027) // Dark Emerald / Slate
val MataPajakTotalMid = Color(0xFF203A43)
val MataPajakTotalEnd = Color(0xFF2C5364)
val TotalPajakGradient = Brush.horizontalGradient(listOf(MataPajakTotalStart, MataPajakTotalMid, MataPajakTotalEnd))

// 3. TopBar Gradient
val TopBarGradStart = Color(0xFF0A2540)
val TopBarGradEnd = Color(0xFF1E3A8A)
val TopBarGradient = Brush.horizontalGradient(listOf(TopBarGradStart, TopBarGradEnd))

// 4. Analitik Card Gradients
val AnalyticDailyStart = Color(0xFF4A148C) // Royal Purple
val AnalyticDailyEnd = Color(0xFF7B1FA2)
val AnalyticMonthlyStart = Color(0xFFBF360C) // Deep Orange
val AnalyticMonthlyEnd = Color(0xFFE65100)

// Tax Category Distinct Vibrant Colors & Light Background Gradients
val PkbColor = Color(0xFF1E88E5)
val PkbLight = Color(0xFFE3F2FD)
val PkbGradient = Brush.horizontalGradient(listOf(Color(0xFF1976D2), Color(0xFF42A5F5)))

val BbnkbColor = Color(0xFF2E7D32)
val BbnkbLight = Color(0xFFE8F5E9)
val BbnkbGradient = Brush.horizontalGradient(listOf(Color(0xFF2E7D32), Color(0xFF66BB6A)))

val PapColor = Color(0xFF00838F)
val PapLight = Color(0xFFE0F7FA)
val PapGradient = Brush.horizontalGradient(listOf(Color(0xFF00838F), Color(0xFF26C6DA)))

val PbbkbColor = Color(0xFFE65100)
val PbbkbLight = Color(0xFFFFF3E0)
val PbbkbGradient = Brush.horizontalGradient(listOf(Color(0xFFE65100), Color(0xFFFFA726)))

val RokokColor = Color(0xFFC2185B)
val RokokLight = Color(0xFFFCE4EC)
val RokokGradient = Brush.horizontalGradient(listOf(Color(0xFFC2185B), Color(0xFFEC407A)))

val AbColor = Color(0xFF6A1B9A)
val AbLight = Color(0xFFF3E5F5)
val AbGradient = Brush.horizontalGradient(listOf(Color(0xFF6A1B9A), Color(0xFFAB47BC)))
