package com.ailisi.jinianri.ui.liquid

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop

/**
 * 应用底部导航：直接使用 Kyant0/AndroidLiquidGlass 的官方 LiquidBottomTabs 组件，
 * 液态玻璃折射、模糊、选中项液滴的滑动与拖拽效果全部来自该库。
 *
 * 该栏由导航宿主常驻渲染（不随页面切换重建），因此点击标签时液滴会平滑滑到目标位置。
 */
@Composable
fun LiquidBottomBar(
    backdrop: Backdrop,
    selectedIndex: () -> Int,
    onSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LiquidBottomTabs(
        selectedTabIndex = selectedIndex,
        onTabSelected = onSelected,
        backdrop = backdrop,
        tabsCount = 3,
        modifier = modifier,
    ) {
        BottomTabItem("首页", Icons.Filled.Home) { onSelected(0) }
        BottomTabItem("农历", Icons.Filled.CalendarMonth) { onSelected(1) }
        BottomTabItem("我的", Icons.Filled.Person) { onSelected(2) }
    }
}

@Composable
private fun RowScope.BottomTabItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    LiquidBottomTab(onClick = onClick) {
        Icon(
            icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(22.dp),
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}
