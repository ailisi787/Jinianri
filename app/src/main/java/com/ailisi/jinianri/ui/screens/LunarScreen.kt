package com.ailisi.jinianri.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ailisi.jinianri.domain.DateEngine
import com.ailisi.jinianri.ui.EventCard
import com.ailisi.jinianri.ui.MainViewModel
import com.ailisi.jinianri.ui.components.CardSurface
import com.ailisi.jinianri.ui.components.GlassBackdrop
import com.ailisi.jinianri.ui.components.colorOf
import com.ailisi.jinianri.ui.components.eventIconOf
import com.ailisi.jinianri.ui.components.iconOf
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun LunarScreen(
    viewModel: MainViewModel,
    onOpenEvent: (Long) -> Unit,
) {
    val today = remember { LocalDate.now() }
    var shownMonth by remember { mutableStateOf(YearMonth.from(today)) }
    // 选中的日期（同一天再点取消）；切换月份时重置
    var selectedDay by remember(shownMonth) { mutableStateOf<Int?>(null) }

    val cards by viewModel.filteredCards.collectAsState()
    val categories by viewModel.categories.collectAsState()

    // 当月每天的纪念日：dayOfMonth -> List<EventCard>
    val eventsByDay: Map<Int, List<EventCard>> = remember(shownMonth, cards) {
        val days = shownMonth.lengthOfMonth()
        buildMap {
            for (day in 1..days) {
                val date = shownMonth.atDay(day)
                val list = cards.filter { card ->
                    val e = card.event
                    DateEngine.occursOn(
                        dateEpochDay = e.dateEpochDay,
                        repeatYearly = e.repeatYearly,
                        isLunar = e.isLunar,
                        lunarMonth = e.lunarMonth,
                        lunarDay = e.lunarDay,
                        date = date,
                    )
                }
                if (list.isNotEmpty()) put(day, list)
            }
        }
    }

    // 当月全部纪念日（按天排序）
    val monthEvents: List<Pair<Int, EventCard>> = eventsByDay
        .toSortedMap()
        .flatMap { (day, list) -> list.sortedBy { it.daysUntil }.map { day to it } }

    // 选中某天时，只显示该天的纪念日
    val listEvents: List<Pair<Int, EventCard>> =
        if (selectedDay == null) monthEvents
        else monthEvents.filter { it.first == selectedDay }

    // 今天是否有纪念日
    val todayEventsCount = remember(cards) {
        cards.count { it.isToday }
    }

    val weekdayText = remember(today) {
        when (today.dayOfWeek) {
            DayOfWeek.MONDAY -> "星期一"
            DayOfWeek.TUESDAY -> "星期二"
            DayOfWeek.WEDNESDAY -> "星期三"
            DayOfWeek.THURSDAY -> "星期四"
            DayOfWeek.FRIDAY -> "星期五"
            DayOfWeek.SATURDAY -> "星期六"
            DayOfWeek.SUNDAY -> "星期日"
            null -> ""
        }
    }

    GlassBackdrop { prismalBackdrop ->
        Scaffold(
            containerColor = Color.Transparent,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 20.dp, end = 20.dp, top = 16.dp, bottom = 130.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // 1. 今日万年历 / 黄历卡片
                item {
                    CardSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        containerColor = Color.White,
                        borderColor = Color(0xFFEBECEF),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                        ) {
                            // 顶栏：黄历徽章 + 星期
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp),
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "传统万年历",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                                Text(
                                    weekdayText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            // 公历大字
                            Text(
                                "${today.year}年 ${today.monthValue}月 ${today.dayOfMonth}日",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            Spacer(Modifier.height(8.dp))

                            // 农历标签与生肖
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                ) {
                                    Text(
                                        "农历 · " + DateEngine.lunarTextOf(today),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                                ) {
                                    Text(
                                        "${DateEngine.ganZhiOf(today)}年 · 属${DateEngine.zodiacOf(today)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    )
                                }
                            }

                            // 今日纪念日庆祝气泡
                            if (todayEventsCount > 0) {
                                Spacer(Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            "🎉 今天有 $todayEventsCount 个纪念日，愿美好如期而至！",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. 月历卡片：有纪念日的日期显示圆点
                item {
                    CardSurface(
                        shape = RoundedCornerShape(20.dp),
                        containerColor = Color.White,
                        borderColor = Color(0xFFEBECEF),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            // 月份切换控制栏
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(onClick = { shownMonth = shownMonth.minusMonths(1) }) {
                                    Icon(
                                        Icons.Filled.ChevronLeft,
                                        contentDescription = "上个月",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        "${shownMonth.year}年 ${shownMonth.monthValue}月",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                    )
                                    // 若不是当前月，显示「回到今天」快捷胶囊
                                    if (shownMonth != YearMonth.from(today)) {
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                            onClick = {
                                                shownMonth = YearMonth.from(today)
                                                selectedDay = today.dayOfMonth
                                            },
                                        ) {
                                            Text(
                                                "回到今天",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            )
                                        }
                                    }
                                }
                                IconButton(onClick = { shownMonth = shownMonth.plusMonths(1) }) {
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = "下个月",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            WeekHeader()
                            Spacer(Modifier.height(6.dp))

                            MonthGrid(
                                month = shownMonth,
                                today = today,
                                eventsByDay = eventsByDay,
                                categories = categories,
                                selectedDay = selectedDay,
                                onSelectDay = { day ->
                                    // 再点一次同一天取消选中
                                    selectedDay = if (selectedDay == day) null else day
                                },
                            )
                        }
                    }
                }

                // 3. 选中日期：距离今天的天数卡片
                if (selectedDay != null) {
                    item {
                        val selectedDate = shownMonth.atDay(selectedDay!!)
                        val days = java.time.temporal.ChronoUnit.DAYS.between(today, selectedDate)
                        CardSurface(
                            shape = RoundedCornerShape(20.dp),
                            containerColor = Color.White,
                            borderColor = Color(0xFFEBECEF),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        "农历 " + DateEngine.lunarTextOf(selectedDate),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    when {
                                        days > 0 -> "还有 $days 天"
                                        days == 0L -> "就是今天 🎉"
                                        else -> "已过去 ${-days} 天"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.width(8.dp))
                                IconButton(
                                    onClick = { selectedDay = null },
                                    modifier = Modifier.size(26.dp),
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "清除选择",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. 当月/当天纪念日列表标题
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            when {
                                selectedDay != null && listEvents.isNotEmpty() ->
                                    "当天的纪念日 · ${listEvents.size} 个"
                                selectedDay != null -> "当天的纪念日"
                                monthEvents.isEmpty() -> "本月纪念日"
                                else -> "本月纪念日 · ${monthEvents.size} 个"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        if (selectedDay != null) {
                            TextButton(onClick = { selectedDay = null }) {
                                Text("显示全月", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                if (listEvents.isEmpty()) {
                    item {
                        CardSurface(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp),
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    if (selectedDay != null) "这天没有纪念日" else "本月还没有纪念日",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                } else {
                    items(listEvents, key = { it.second.event.id }) { (day, card) ->
                        MonthEventRow(
                            day = day,
                            card = card,
                            categories = categories,
                            onClick = { onOpenEvent(card.event.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthEventRow(
    day: Int,
    card: EventCard,
    categories: List<com.ailisi.jinianri.data.CategoryEntity>,
    onClick: () -> Unit,
) {
    val category = categories.firstOrNull { it.id == card.event.categoryId }
    val catColor = colorOf(category?.color ?: 0xFF6750A4)
    val eventIcon = eventIconOf(card.event.iconKey)

    CardSurface(
        shape = RoundedCornerShape(18.dp),
        containerColor = Color.White,
        borderColor = Color(0xFFEBECEF),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 日期数字方块
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(catColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$day",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = catColor,
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (eventIcon != null) {
                        Image(
                            painter = painterResource(eventIcon.resId),
                            contentDescription = eventIcon.label,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                    } else if (category != null) {
                        Icon(
                            iconOf(category.icon),
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        card.event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    "${if (card.event.isLunar) "农历 " else "公历 "}$day 日 · ${card.event.isLunar.let { if (it) "按农历每年重复" else "按公历每年重复" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(10.dp))
            // 倒数天数胶囊
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (card.isToday) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            ) {
                Text(
                    daysTextOf(card),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (card.isToday) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun WeekHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf(
            "一" to false,
            "二" to false,
            "三" to false,
            "四" to false,
            "五" to false,
            "六" to true,
            "日" to true,
        ).forEach { (day, isWeekend) ->
            Text(
                day,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isWeekend) FontWeight.Bold else FontWeight.Medium,
                color = if (isWeekend) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    eventsByDay: Map<Int, List<EventCard>>,
    categories: List<com.ailisi.jinianri.data.CategoryEntity>,
    selectedDay: Int?,
    onSelectDay: (Int) -> Unit,
) {
    val firstDay = month.atDay(1)
    // 周一为一周开始
    val leading = (firstDay.dayOfWeek.value + 6) % 7
    val daysInMonth = month.lengthOfMonth()
    val cells = leading + daysInMonth
    val rows = (cells + 6) / 7

    // 预计算当月每天的农历，避免在格子里反复转换
    val lunars = remember(month) {
        (1..daysInMonth).associateWith { DateEngine.toLunar(month.atDay(it)) }
    }

    Column {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    val dayNumber = index - leading + 1
                    val inMonth = dayNumber in 1..daysInMonth
                    val dayEvents = if (inMonth) eventsByDay[dayNumber].orEmpty() else emptyList()
                    val lunar = lunars[dayNumber]
                    val date = if (inMonth) month.atDay(dayNumber) else null
                    val isToday = date == today
                    val isSelected = selectedDay == dayNumber
                    val hasEvents = dayEvents.isNotEmpty()
                    val accentColor = remember(dayEvents) {
                        dayEvents.firstOrNull()?.let { card ->
                            categories.firstOrNull { it.id == card.event.categoryId }
                                ?.let { colorOf(it.color) }
                        } ?: Color(0xFF6750A4)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.72f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (hasEvents && !isToday && !isSelected) {
                                    accentColor.copy(alpha = 0.08f)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .clickable(
                                enabled = inMonth,
                                onClick = { onSelectDay(dayNumber) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (inMonth && lunar != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isToday -> MaterialTheme.colorScheme.primary
                                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                else -> Color.Transparent
                                            }
                                        )
                                        .then(
                                            if (isSelected && !isToday) {
                                                Modifier.border(
                                                    1.5.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    CircleShape,
                                                )
                                            } else Modifier
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "$dayNumber",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            isToday -> MaterialTheme.colorScheme.onPrimary
                                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                }
                                Spacer(Modifier.height(1.dp))
                                Text(
                                    lunarDayLabel(lunar.second, lunar.third),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isToday || isSelected -> MaterialTheme.colorScheme.primary
                                        hasEvents -> accentColor.copy(alpha = 0.9f)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    },
                                )
                                // 纪念日标记：优先显示事件图标，最多 2 个，多余的显示 +N
                                Box(
                                    modifier = Modifier.height(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (hasEvents) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            dayEvents.take(2).forEach { card ->
                                                val icon = eventIconOf(card.event.iconKey)
                                                if (icon != null) {
                                                    Image(
                                                        painter = painterResource(icon.resId),
                                                        contentDescription = icon.label,
                                                        modifier = Modifier.size(14.dp),
                                                    )
                                                } else {
                                                    val dotColor = categories
                                                        .firstOrNull { it.id == card.event.categoryId }
                                                        ?.let { colorOf(it.color) }
                                                        ?: Color(0xFF6750A4)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(dotColor),
                                                    )
                                                }
                                            }
                                            if (dayEvents.size > 2) {
                                                Text(
                                                    "+${dayEvents.size - 2}",
                                                    fontSize = 9.sp,
                                                    lineHeight = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = accentColor,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 农历日显示：初一显示月份名，其余显示日名 */
private fun lunarDayLabel(month: Int, day: Int): String {
    if (day == 1) {
        val leap = if (month < 0) "闰" else ""
        val m = if (month < 0) -month else month
        val names = listOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")
        return leap + (names.getOrNull(m - 1) ?: "$m") + "月"
    }
    val names = listOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )
    return names.getOrElse(day - 1) { "$day" }
}
