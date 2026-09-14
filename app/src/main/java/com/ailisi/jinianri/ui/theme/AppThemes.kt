package com.ailisi.jinianri.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * 主题配色方案。
 *
 * 色值取自 GitHub 上流行的开源配色项目：
 * Catppuccin Latte（MIT）、Rosé Pine Dawn、Solarized、GitHub Primer、Gruvbox Light、Material 3。
 * [primary] 主色（按钮/选中态/底栏液滴），[tertiary] 辅助色（玻璃背景晕染）。
 */
data class ThemePalette(
    val key: String,
    val name: String,
    val source: String,
    val primary: Color,
    val tertiary: Color,
)

val ThemePalettes: List<ThemePalette> = listOf(
    ThemePalette("violet", "原野紫", "Material 3", Color(0xFF6750A4), Color(0xFF8E6F5B)),
    ThemePalette("indigo", "靛蓝", "GitHub Primer", Color(0xFF0969DA), Color(0xFF8250DF)),
    ThemePalette("sky", "晴空蓝", "Catppuccin Latte", Color(0xFF1E66F5), Color(0xFF04A5E5)),
    ThemePalette("teal", "薄荷青", "Catppuccin Latte", Color(0xFF179299), Color(0xFF209FB5)),
    ThemePalette("green", "抹茶绿", "Catppuccin Latte", Color(0xFF40A02B), Color(0xFF179299)),
    ThemePalette("lavender", "薰衣草", "Catppuccin Latte", Color(0xFF7287FD), Color(0xFF8839EF)),
    ThemePalette("pink", "樱花粉", "Catppuccin Latte", Color(0xFFEA76CB), Color(0xFFDC8A78)),
    ThemePalette("peach", "落日橘", "Catppuccin Latte", Color(0xFFFE640B), Color(0xFFDF8E1D)),
    ThemePalette("red", "枫叶红", "Catppuccin Latte", Color(0xFFD20F39), Color(0xFFE64553)),
    ThemePalette("solarized", "深海蓝", "Solarized", Color(0xFF268BD2), Color(0xFFCB4B16)),
    ThemePalette("rosepine", "玫瑰木", "Rosé Pine Dawn", Color(0xFF907AA9), Color(0xFFD7827E)),
    ThemePalette("forest", "松林绿", "Rosé Pine", Color(0xFF286983), Color(0xFF56949F)),
    ThemePalette("amber", "琥珀棕", "Gruvbox Light", Color(0xFFB57614), Color(0xFFAF3A03)),
)

fun paletteOf(key: String?): ThemePalette =
    ThemePalettes.firstOrNull { it.key == key } ?: ThemePalettes.first()

/** 中性底色：温润微白、精致细浅描边（不随配色变化） */
internal val BaseLightColors: ColorScheme = lightColorScheme(
    background = Color(0xFFF9FAFC),
    onBackground = Color(0xFF14171A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF16191D),
    surfaceVariant = Color(0xFFF1F3F7),
    onSurfaceVariant = Color(0xFF656F7D),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F8FA),
    surfaceContainer = Color(0xFFEFF1F5),
    surfaceContainerHigh = Color(0xFFE8EBF0),
    surfaceContainerHighest = Color(0xFFDFE3EA),
    outline = Color(0xFF8C96A5),
    outlineVariant = Color(0xFFE5E8EE),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

/** 由配色生成完整浅色 ColorScheme：容器色 = 主色向白/黑插值 */
fun schemeFor(palette: ThemePalette): ColorScheme {
    val primary = palette.primary
    val tertiary = palette.tertiary
    val secondary = lerp(primary, tertiary, 0.5f)
    return BaseLightColors.copy(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = lerp(primary, Color.White, 0.87f),
        onPrimaryContainer = lerp(primary, Color.Black, 0.4f),
        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = lerp(secondary, Color.White, 0.87f),
        onSecondaryContainer = lerp(secondary, Color.Black, 0.4f),
        tertiary = tertiary,
        onTertiary = Color.White,
        tertiaryContainer = lerp(tertiary, Color.White, 0.87f),
        onTertiaryContainer = lerp(tertiary, Color.Black, 0.4f),
    )
}
