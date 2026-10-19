package com.davi.dev.gnixia.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

private val Navy950 = Color(0xFF000814)
private val Navy900 = Color(0xFF001D3D)
private val Navy800 = Color(0xFF003566)
private val Gold = Color(0xFFFFC300)
private val Yellow = Color(0xFFFFD60A)

// Internal so component Styles (see ComponentStyles.kt) can read the same tokens outside composition
internal val GnixiaColorScheme = ColorScheme(
    primary = Yellow,
    primaryDim = Gold,
    primaryContainer = Gold,
    onPrimary = Navy950,
    onPrimaryContainer = Navy950,
    secondary = Gold,
    secondaryDim = Gold,
    secondaryContainer = Navy800,
    onSecondary = Navy950,
    onSecondaryContainer = Yellow,
    surfaceContainerLow = Navy900,
    surfaceContainer = Navy900,
    surfaceContainerHigh = Navy800,
    background = Navy950,
)

@Composable
fun GnixiaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GnixiaColorScheme,
        typography = GnixiaTypography,
        content = content
    )
}
