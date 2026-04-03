package com.example.appghichiso.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.appghichiso.ui.theme.AppTypography

// ─────────────────────────────────────────────
//  Bảng màu: Xanh Biển & Xanh Ngọc (Ocean + Teal)
// ─────────────────────────────────────────────

// Blues
val OceanBlue        = Color(0xFF0077B6)   // Primary
val OceanBlueDark    = Color(0xFF005F92)   // PrimaryDark
val OceanBlueLight   = Color(0xFFCAE9FF)   // PrimaryContainer
val OnOceanBlueLight = Color(0xFF001E30)   // OnPrimaryContainer
val DeepNavy         = Color(0xFF003554)   // InverseSurface hint

// Teals
val Teal             = Color(0xFF009688)   // Secondary
val TealDark         = Color(0xFF00796B)
val TealContainer    = Color(0xFFB2DFDB)   // SecondaryContainer
val OnTealContainer  = Color(0xFF00363C)   // OnSecondaryContainer

// Cyan (Tertiary accent)
val Cyan             = Color(0xFF00B4D8)   // Tertiary
val CyanContainer    = Color(0xFFCFF4FC)   // TertiaryContainer
val OnCyanContainer  = Color(0xFF001F25)

// Neutrals
val BlueGray50       = Color(0xFFF0F8FF)   // Background
val BlueGray100      = Color(0xFFDCEFF9)   // SurfaceVariant
val BlueGray900      = Color(0xFF001E30)   // OnBackground / OnSurface
val SlateGray        = Color(0xFF41545D)   // OnSurfaceVariant

// Error
val ErrorRed         = Color(0xFFBA1A1A)
val ErrorRedContainer= Color(0xFFFFDAD6)

// ─── Light Color Scheme ───────────────────────
private val LightColors = lightColorScheme(
    primary               = OceanBlue,
    onPrimary             = Color.White,
    primaryContainer      = OceanBlueLight,
    onPrimaryContainer    = OnOceanBlueLight,

    secondary             = Teal,
    onSecondary           = Color.White,
    secondaryContainer    = TealContainer,
    onSecondaryContainer  = OnTealContainer,

    tertiary              = Cyan,
    onTertiary            = Color.White,
    tertiaryContainer     = CyanContainer,
    onTertiaryContainer   = OnCyanContainer,

    background            = BlueGray50,
    onBackground          = BlueGray900,

    surface               = Color.White,
    onSurface             = BlueGray900,
    surfaceVariant        = BlueGray100,
    onSurfaceVariant      = SlateGray,

    error                 = ErrorRed,
    onError               = Color.White,
    errorContainer        = ErrorRedContainer,
    onErrorContainer      = Color(0xFF410002),

    outline               = Color(0xFF6D8795),
    outlineVariant        = Color(0xFFC0D8E6),

    inverseSurface        = DeepNavy,
    inverseOnSurface      = BlueGray50,
    inversePrimary        = Color(0xFF90CAFF)
)

// ─── Dark Color Scheme ────────────────────────
private val DarkColors = darkColorScheme(
    primary               = Color(0xFF90CAFF),
    onPrimary             = Color(0xFF003258),
    primaryContainer      = OceanBlueDark,
    onPrimaryContainer    = OceanBlueLight,

    secondary             = Color(0xFF80CBC4),
    onSecondary           = Color(0xFF00504B),
    secondaryContainer    = TealDark,
    onSecondaryContainer  = TealContainer,

    tertiary              = Color(0xFF00D7F3),
    onTertiary            = Color(0xFF003541),
    tertiaryContainer     = Color(0xFF004E5F),
    onTertiaryContainer   = CyanContainer,

    background            = Color(0xFF001E30),
    onBackground          = Color(0xFFCAE9FF),

    surface               = Color(0xFF001E30),
    onSurface             = Color(0xFFCAE9FF),
    surfaceVariant        = Color(0xFF1E3A4A),
    onSurfaceVariant      = Color(0xFFC0D8E6),

    error                 = Color(0xFFFFB4AB),
    onError               = Color(0xFF690005),
    errorContainer        = Color(0xFF93000A),
    onErrorContainer      = Color(0xFFFFDAD6),

    outline               = Color(0xFF8AAEBB),
    outlineVariant        = Color(0xFF1E3A4A),

    inverseSurface        = OceanBlueLight,
    inverseOnSurface      = OnOceanBlueLight,
    inversePrimary        = OceanBlue
)

// ─── AppTheme entry point ─────────────────────
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = AppTypography,
        content     = content
    )
}


