package com.pax.habit.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Pax Purple identity
val PaxPurple = Color(0xFF4F2FDB)
val PaxBlue = Color(0xFF3550FF)
val PaxViolet = Color(0xFF6A2CFF)
val PaxMagenta = Color(0xFFA32BDD)
val PaxSuccess = Color(0xFF2E9E6A)

/** Used only for hero areas. */
val PaxGradient = Brush.linearGradient(listOf(PaxBlue, PaxViolet, PaxMagenta))

object Spacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 16.dp
    val l = 24.dp
    val xl = 32.dp
}

object Elevation {
    val flat = 0.dp
    val raised = 2.dp
}

private val LightColors = lightColorScheme(
    primary = PaxPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4DFFF),
    onPrimaryContainer = Color(0xFF16046F),
    secondary = Color(0xFF5D5A7C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3DFFF),
    onSecondaryContainer = Color(0xFF1A1735),
    tertiary = Color(0xFF8B2F9E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8F0),
    onTertiaryContainer = Color(0xFF3A0048),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE4E1EC),
    onSurfaceVariant = Color(0xFF47464F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF5F2FA),
    surfaceContainer = Color(0xFFF3F0FA),
    surfaceContainerHigh = Color(0xFFE8E6F4),
    surfaceContainerHighest = Color(0xFFE2E0EC),
    outline = Color(0xFF78767F),
    outlineVariant = Color(0xFFC8C5D0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC7BFFF),
    onPrimary = Color(0xFF2A0A9A),
    primaryContainer = Color(0xFF3B1FC0),
    onPrimaryContainer = Color(0xFFE4DFFF),
    secondary = Color(0xFFC6C2EA),
    onSecondary = Color(0xFF2E2B4D),
    secondaryContainer = Color(0xFF454364),
    onSecondaryContainer = Color(0xFFE3DFFF),
    tertiary = Color(0xFFF2B0FF),
    onTertiary = Color(0xFF560A6B),
    tertiaryContainer = Color(0xFF722386),
    onTertiaryContainer = Color(0xFFFFD8F0),
    background = Color(0xFF13111C),
    onBackground = Color(0xFFE5E1EA),
    surface = Color(0xFF13111C),
    onSurface = Color(0xFFE5E1EA),
    surfaceVariant = Color(0xFF47464F),
    onSurfaceVariant = Color(0xFFC8C5D0),
    surfaceContainerLowest = Color(0xFF0E0C16),
    surfaceContainerLow = Color(0xFF1B1824),
    surfaceContainer = Color(0xFF1F1C29),
    surfaceContainerHigh = Color(0xFF2A2734),
    surfaceContainerHighest = Color(0xFF35323F),
    outline = Color(0xFF928F99),
    outlineVariant = Color(0xFF47464F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val PaxShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val PaxTypography = Typography().let { t ->
    t.copy(
        displayMedium = t.displayMedium.copy(fontWeight = FontWeight.Bold),
        headlineLarge = t.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    )
}

@Composable
fun PaxTheme(darkTheme: Boolean, dynamicColor: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = scheme, typography = PaxTypography, shapes = PaxShapes, content = content)
}
