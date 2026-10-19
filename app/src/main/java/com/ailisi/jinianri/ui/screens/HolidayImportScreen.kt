package com.ailisi.jinianri.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ailisi.jinianri.domain.Festival
import com.ailisi.jinianri.domain.FestivalGroup
import com.ailisi.jinianri.domain.FestivalGroups
import com.ailisi.jinianri.domain.HolidayCalendar
import com.ailisi.jinianri.ui.MainViewModel
import com.ailisi.jinianri.ui.components.CardSurface
import com.ailisi.jinianri.ui.components.GlassBackdrop
import com.ailisi.jinianri.ui.components.eventIconOf
import java.time.LocalDate

/**
 * 节日导入页：内置节日目录按组勾选，一键导入成「节日」分类下的纪念日。
 * 已导入的节日会标出来，避免重复导入。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayImportScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val cards by viewModel.cards.collectAsState()
    val today by viewModel.today.collectAsState()
    // 勾选状态：节日名 -> 是否选中
    val selected = remember { mutableStateMapOf<String, Boolean>() }

    // 已导入的节日（名字 + 日期规则都对得上才算，用户自己建的普通事件不会误判）
    val importedNames = remember(cards) {
        val events = cards.map { it.event }
        FestivalGroups
            .flatMap { it.festivals }
            .filter { festival -> events.any { festival.matches(it) } }
            .map { it.name }
            .toSet()
    }

    val chosen = FestivalGroups.flatMap { it.festivals }
        .filter { it.name !in importedNames && selected[it.name] == true }

    GlassBackdrop { _ ->
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "节日导入",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
            bottomBar = {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        onClick = {
                            viewModel.importFestivals(chosen) { count ->
                                Toast.makeText(
                                    context,
                                    if (count > 0) "已导入 $count 个节日" else "没有可导入的节日",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                onBack()
                            }
                        },
                        enabled = chosen.isNotEmpty(),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .height(54.dp),
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (chosen.isEmpty()) "请选择要导入的节日" else "导入选中的 ${chosen.size} 个节日",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text(
                        "勾选想要的节日，导入后归入「节日」分类，并出现在首页与农历页里。" +
                            "节日日期每年自动重算（含节气与「第几个星期几」的节日）。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                    )
                }

                FestivalGroups.forEach { group ->
                    item(key = "header-${group.name}") {
                        GroupHeader(
                            group = group,
                            importedNames = importedNames,
                            selected = selected,
                        )
                    }
                    items(group.festivals, key = { "${group.name}-${it.name}" }) { festival ->
                        FestivalRow(
                            festival = festival,
                            today = today,
                            imported = festival.name in importedNames,
                            checked = selected[festival.name] == true,
                            onToggle = { selected[festival.name] = !(selected[festival.name] ?: false) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(
    group: FestivalGroup,
    importedNames: Set<String>,
    selected: MutableMap<String, Boolean>,
) {
    val selectable = group.festivals.filter { it.name !in importedNames }
    val allSelected = selectable.isNotEmpty() && selectable.all { selected[it.name] == true }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                group.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                group.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (selectable.isNotEmpty()) {
            TextButton(onClick = {
                selectable.forEach { selected[it.name] = !allSelected }
            }) {
                Text(
                    if (allSelected) "取消全选" else "全选",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun FestivalRow(
    festival: Festival,
    today: LocalDate,
    imported: Boolean,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val icon = eventIconOf(festival.icon)
    val next = remember(festival, today) { festival.nextDate(today) }
    val breakDays = HolidayCalendar.breakDaysOf(festival.name)
    val tint = MaterialTheme.colorScheme.primary

    CardSurface(
        shape = RoundedCornerShape(18.dp),
        containerColor = Color.White,
        borderColor = Color(0xFFEBECEF),
        onClick = if (imported) null else onToggle,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 节日图标
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                if (icon != null) {
                    Image(
                        painter = painterResource(icon.resId),
                        contentDescription = icon.label,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        festival.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (breakDays != null) {
                        Spacer(Modifier.width(6.dp))
                        Tag(text = "放假 $breakDays 天", color = Color(0xFFE53935))
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    buildString {
                        append(festival.dateText(today.year))
                        if (next != null) {
                            append(" · ")
                            append(if (next.year == today.year) "今年 " else "明年 ")
                            append("${next.monthValue}月${next.dayOfMonth}日")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            if (imported) {
                Tag(text = "已导入", color = MaterialTheme.colorScheme.outline)
            } else {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

@Composable
private fun Tag(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
        )
    }
}
