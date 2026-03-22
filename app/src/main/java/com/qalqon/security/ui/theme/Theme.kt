package com.qalqon.security.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val QalqonDarkScheme = darkColorScheme(
    primary = Color(0xFFD32F2F),
    secondary = Color(0xFFF57C00),
    tertiary = Color(0xFF2E7D32),
    background = Color(0xFF0E1118),
    surface = Color(0xFF151A24),
)

private val QalqonLightScheme = lightColorScheme(
    primary = Color(0xFFB71C1C),
    secondary = Color(0xFFEF6C00),
    tertiary = Color(0xFF2E7D32),
    background = Color(0xFFF4F7FB),
    surface = Color(0xFFFFFFFF),
)

@Composable
fun QalqonTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) QalqonDarkScheme else QalqonLightScheme,
        typography = Typography,
        content = content,
    )
}
