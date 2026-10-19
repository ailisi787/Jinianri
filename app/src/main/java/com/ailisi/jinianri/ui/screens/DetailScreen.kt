package com.ailisi.jinianri.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ailisi.jinianri.data.EventEntity
import com.ailisi.jinianri.domain.DateEngine
import com.ailisi.jinianri.domain.FestivalRules
import com.ailisi.jinianri.ui.MainViewModel
import com.ailisi.jinianri.ui.components.CardSurface
import com.ailisi.jinianri.ui.components.GlassBackdrop
import com.ailisi.jinianri.ui.components.colorOf
import com.ailisi.jinianri.ui.components.eventIconOf
import com.ailisi.jinianri.ui.components.iconOf
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: MainViewModel,
    eventId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    val categories by viewModel.categories.collectAsState()
    var showDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 直接按 id 观察事件（Flow 要 remember，否则每次重组都会重建并重启收集）
    val eventFlow = remember(eventId) { viewModel.observeEvent(eventId) }
    val event by eventFlow.collectAsState(initial = null)
    val e = event ?: return

    val timing = DateEngine.timingOf(
        dateEpochDay = e.dateEpochDay,
        repeatYearly = e.repeatYearly,
        isLunar = e.isLunar,
        lunarMonth = e.lunarMonth,
        lunarDay = e.lunarDay,
        rule = e.rule,
    )
    val category = categories.firstOrNull { it.id == e.categoryId }
    val color = colorOf(category?.color ?: 0xFF6750A4)

    val nextDayOfWeek = when (timing.nextDate.dayOfWeek) {
        DayOfWeek.MONDAY -> "星期一"
        DayOfWeek.TUESDAY -> "星期二"
        DayOfWeek.WEDNESDAY -> "星期三"
        DayOfWeek.THURSDAY -> "星期四"
        DayOfWeek.FRIDAY -> "星期五"
        DayOfWeek.SATURDAY -> "星期六"
        DayOfWeek.SUNDAY -> "星期日"
        else -> ""
    }

    GlassBackdrop { backdrop ->
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "纪念日详情",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        IconButton(onClick = { onEdit(e.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "编辑")
                        }
                        IconButton(onClick = { showDelete = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(8.dp))

                // 核心大数字展示卡片（Hero Stage）：纯白卡片
                CardSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    containerColor = Color.White,
                    borderColor = Color(0xFFEBECEF),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // 顶部：分类胶囊 + 重复标识
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (category != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = color.copy(alpha = 0.10f),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = iconOf(category.icon),
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(13.dp),
                                        )
                                        Spacer(Modifier.width(5.dp))
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = color,
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                            ) {
                                Text(
                                    text = if (e.repeatYearly) "每年重复" else "单次事件",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // 3D Fluent Emoji
                        eventIconOf(e.iconKey)?.let { icon ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(color.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    painter = painterResource(icon.resId),
                                    contentDescription = icon.label,
                                    modifier = Modifier.size(60.dp),
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        // 事件标题
                        Text(
                            text = e.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(8.dp))

                        // 倒计时核心数字展示
                        val bigNumber = when {
                            e.countUp -> timing.daysSince
                            timing.daysUntil == 0L -> 0
                            timing.daysUntil < 0 -> -timing.daysUntil
                            else -> timing.daysUntil
                        }

                        if (timing.daysUntil == 0L && !e.countUp) {
                            // 就是今天：大数字"0"配分类色 + 小标签
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "0",
                                        fontSize = 68.sp,
                                        lineHeight = 70.sp,
                                        fontWeight = FontWeight.Black,
                                        color = color,
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "天",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                        modifier = Modifier.padding(bottom = 10.dp),
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = color.copy(alpha = 0.10f),
                                ) {
                                    Text(
                                        text = "🎉  就是今天",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    )
                                }
                            }

                        } else {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$bigNumber",
                                    fontSize = 68.sp,
                                    lineHeight = 70.sp,
                                    fontWeight = FontWeight.Black,
                                    color = color,
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "天",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                        }

                        Text(
                            text = labelOf(e, timing.daysUntil),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )

                        Spacer(Modifier.height(10.dp))

                        // 下一次发生提示
                        Text(
                            text = if (e.repeatYearly) {
                                "下一次：${timing.nextDate.year}年${timing.nextDate.monthValue}月${timing.nextDate.dayOfMonth}日 ($nextDayOfWeek)"
                            } else {
                                "目标日：${timing.nextDate.year}年${timing.nextDate.monthValue}月${timing.nextDate.dayOfMonth}日 ($nextDayOfWeek)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 结构化信息卡片 1：日期与日历
                CardSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color.White,
                    borderColor = Color(0xFFEBECEF),
                ) {
                    Column(Modifier.padding(vertical = 8.dp)) {
                        val solar = LocalDate.ofEpochDay(e.dateEpochDay)
                        InfoDetailRow(
                            icon = Icons.Filled.CalendarMonth,
                            iconColor = color,
                            label = if (e.isLunar) "农历日期" else "公历日期",
                            value = if (e.isLunar) {
                                "农历 " + DateEngine.lunarMonthDayText(
                                    e.lunarMonth, e.lunarDay,
                                    DateEngine.toLunar(solar).first,
                                )
                            } else {
                                "${solar.year}年${solar.monthValue}月${solar.dayOfMonth}日"
                            },
                        )
                        InfoDetailRow(
                            icon = Icons.Filled.Repeat,
                            iconColor = MaterialTheme.colorScheme.primary,
                            label = "重复规则",
                            value = if (e.repeatYearly) {
                                when {
                                    e.rule.isNotEmpty() ->
                                        "每年按${FestivalRules.describe(e.rule)}重算"
                                    e.isLunar -> "每年按农历循环"
                                    else -> "每年按公历循环"
                                }
                            } else "一次性事件",
                        )
                        InfoDetailRow(
                            icon = Icons.Filled.Notifications,
                            iconColor = MaterialTheme.colorScheme.tertiary,
                            label = "提前提醒",
                            value = when (e.remindDaysBefore) {
                                null -> "不提醒"
                                0 -> "当天准时提醒"
                                else -> "提前 ${e.remindDaysBefore} 天提醒"
                            },
                        )
                    }
                }

                // 结构化信息卡片 2：回忆寄语 / 备注（如果填写）
                if (e.note.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))
                    CardSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        containerColor = Color.White,
                        borderColor = Color(0xFFEBECEF),
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "回忆寄语 · 备忘",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = e.note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 底部操作区：编辑 & 分享
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Button(
                        onClick = { onEdit(e.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("编辑", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "【${e.title}】${labelOf(e, timing.daysUntil)}（目标日：${timing.nextDate.monthValue}月${timing.nextDate.dayOfMonth}日）",
                                )
                            }
                            context.startActivity(Intent.createChooser(share, "分享纪念日"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("分享", color = color, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(36.dp))
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("删除纪念日") },
            text = { Text("确定要删除「${e.title}」吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEvent(e)
                    showDelete = false
                    onBack()
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun InfoDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = iconColor,
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun labelOf(e: EventEntity, daysUntil: Long): String = when {
    e.countUp -> "已过去的天数"
    daysUntil == 0L -> "就是今天"
    daysUntil < 0 -> "已经过去 ${-daysUntil} 天"
    else -> "还有 $daysUntil 天"
}
