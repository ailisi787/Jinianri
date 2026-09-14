package com.ailisi.jinianri.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow

val LocalBackdrop = compositionLocalOf<Backdrop?> { null }

/**
 * 液态玻璃环境容器：
 * 底层提供清爽温润底色与极淡雅的环境光斑（Ambient Aura），
 * 为上层的透镜折射提供丰富的光学介质，同时提供统一的 Backdrop 捕获。
 */
@Composable
fun GlassBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(Backdrop) -> Unit,
) {
    val backdrop = rememberLayerBackdrop()

    Box(modifier = modifier.fillMaxSize()) {
        // 底层：温润光影画布（带有极淡雅柔和光斑，供透镜物理折射）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {
            // 素雅清澈底色
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F7F9))
            )
            // 极淡雅的紫蓝灵动环境光斑（右上）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x266750A4),
                                Color(0x006750A4)
                            ),
                            center = Offset(1000f, 320f),
                            radius = 900f
                        )
                    )
            )
            // 温润暖橙环境光斑（左中）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x1FE87A5D),
                                Color(0x00E87A5D)
                            ),
                            center = Offset(120f, 1250f),
                            radius = 950f
                        )
                    )
            )
            // 清透青蓝环境光斑（右中下），保证下半屏的卡片也有可折射的光学介质
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x1A3E8BD8),
                                Color(0x003E8BD8)
                            ),
                            center = Offset(950f, 1550f),
                            radius = 850f
                        )
                    )
            )
        }

        // 上层内容：通过 CompositionLocal 注入 backdrop
        CompositionLocalProvider(LocalBackdrop provides backdrop) {
            content(backdrop)
        }
    }
}

/**
 * 兼容旧代码的 GlassCard
 */
@Composable
fun GlassCard(
    backdrop: Backdrop? = LocalBackdrop.current,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    tint: Color = Color.Unspecified,
    tintAlpha: Float = 0.08f,
    blurRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    CardSurface(
        modifier = modifier,
        shape = shape,
        onClick = onClick,
        content = content,
    )
}

/**
 * 液态玻璃药丸/面板容器（Kyant AndroidLiquidGlass 同款效果）：
 * 自带 Lens 物理折射、色散、边缘高光 Highlight 与内阴影 InnerShadow。
 * 适用于搜索栏、胶囊药丸、浮动按钮等。
 */
@Composable
fun LiquidGlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    tintColor: Color = Color.White.copy(alpha = 0.78f),
    lensStrength: Dp = 12.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val backdrop = LocalBackdrop.current

    if (backdrop != null) {
        Box(
            modifier = modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                        vibrancy()
                        blur(8f.dp.toPx())
                        lens(
                            lensStrength.toPx(),
                            lensStrength.toPx(),
                            chromaticAberration = true
                        )
                    },
                    highlight = {
                        Highlight.Default.copy(alpha = 0.90f)
                    },
                    shadow = {
                        Shadow(alpha = 0.10f)
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = 6f.dp,
                            alpha = 0.22f
                        )
                    },
                    onDrawSurface = {
                        drawRect(tintColor)
                    }
                )
                .clip(shape)
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick)
                    else Modifier
                ),
            content = content
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = tintColor,
            border = BorderStroke(0.8.dp, Color(0xFFEBECEF)),
            onClick = onClick ?: {},
            enabled = onClick != null,
            shadowElevation = 1.dp,
        ) {
            Box(content = content)
        }
    }
}


/**
 * 物理透镜折射卡片（Kyant AndroidLiquidGlass 同款效果）：
 * 凸透镜物理折射（lens）+ 三棱镜色散（chromaticAberration）+ 晶莹边缘高光（Highlight）+ 内阴影（InnerShadow）
 *
 * [glassLift] 是"玻璃提升量"：0 = 静止在页面上，1 = 被拖拽悬空。
 * 提升过程中折射更强、容器更通透、高光与阴影同步增强，形成"玻璃被拿起"的观感。
 */
@Composable
fun CardSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    containerColor: Color = Color.White,
    borderColor: Color = Color(0xFFEBECEF),
    onClick: (() -> Unit)? = null,
    enableGlass: Boolean = true,
    glassLift: Float = 0f,
    content: @Composable () -> Unit,
) {
    val backdrop = LocalBackdrop.current

    if (backdrop != null && enableGlass) {
        val lift = glassLift.coerceIn(0f, 1f)
        // 当 containerColor 为纯白时，为了呈现通透折射感，调整透明度让光斑折射穿透；
        // 拖拽悬浮时容器更通透，光线几乎直接穿过玻璃
        val glassSurfaceColor = if (containerColor == Color.White) {
            Color(0xFFFAFAFA).copy(alpha = 0.58f - 0.50f * lift)
        } else {
            containerColor.copy(alpha = (containerColor.alpha * (0.85f - 0.70f * lift)).coerceIn(0.05f, 0.9f))
        }

        Box(
            modifier = modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                        vibrancy()
                        blur((8f + 2f * lift).dp.toPx())
                        lens(
                            (20f + 30f * lift).dp.toPx(),
                            (18f + 26f * lift).dp.toPx(),
                            chromaticAberration = true
                        )
                    },
                    highlight = {
                        Highlight.Default.copy(alpha = 0.85f + 0.15f * lift)
                    },
                    shadow = {
                        Shadow(alpha = 0.08f + 0.24f * lift)
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = (5f + 9f * lift).dp,
                            alpha = 0.20f + 0.30f * lift
                        )
                    },
                    onDrawSurface = {
                        drawRect(glassSurfaceColor)
                    }
                )
                .clip(shape)
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick)
                    else Modifier
                )
        ) {
            content()
        }
    } else {
        // Fallback: 常规纯白卡片
        Surface(
            modifier = modifier,
            shape = shape,
            color = containerColor,
            border = BorderStroke(0.8.dp, borderColor),
            onClick = onClick ?: {},
            enabled = onClick != null,
            shadowElevation = 1.dp,
            tonalElevation = 0.dp,
        ) {
            content()
        }
    }
}


