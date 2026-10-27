package com.ailisi.jinianri.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/**
 * 应用主题：仅日间模式；[themeKey] 对应 AppThemes.kt 中的配色方案。
 *
 * [schemeFor] 每次调用都会新建一个 [ColorScheme]，若直接在组合里调用，
 * 任何一次重组（例如回到前台触发的重组）都会让 LocalColorScheme 变化，
 * 整棵树跟着重组、液态玻璃的录制层被重建，表现出来就是整屏闪一下。
 * 这里按 themeKey 记忆化，只有真正换配色时才生成新实例。
 */
@Composable
fun JinianriTheme(
    themeKey: String = "violet",
    content: @Composable () -> Unit,
) {
    val colorScheme = remember(themeKey) { schemeFor(paletteOf(themeKey)) }
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AppShapes,
        content = content,
    )
}
