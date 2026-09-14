package com.ailisi.jinianri.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/** 应用主题：仅日间模式；[themeKey] 对应 AppThemes.kt 中的配色方案 */
@Composable
fun JinianriTheme(
    themeKey: String = "violet",
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = schemeFor(paletteOf(themeKey)),
        shapes = AppShapes,
        content = content,
    )
}
