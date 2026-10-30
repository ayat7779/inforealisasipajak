package com.example.infopajak.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FixedAppColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = AccentTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = AccentGold,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceWhite,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFF94A3B8)
)

@Composable
fun InfoPajakTheme(
    darkTheme: Boolean = false, // Locked: always ignore system dark mode to preserve vibrant theme colors
    dynamicColor: Boolean = false, // Locked: do not override with Android 12+ wallpaper colors
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = Color(0xFF0A2540).toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = FixedAppColorScheme,
        typography = Typography,
        content = content
    )
}
