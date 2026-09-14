package com.ailisi.jinianri.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ailisi.jinianri.data.EventEntity
import com.ailisi.jinianri.domain.DateEngine
import com.ailisi.jinianri.ui.MainViewModel
import com.ailisi.jinianri.ui.components.CardSurface
import com.ailisi.jinianri.ui.components.CategoryColorOptions
import com.ailisi.jinianri.ui.components.CategoryIconOptions
import com.ailisi.jinianri.ui.components.EventIconGroups
import com.ailisi.jinianri.ui.components.GlassBackdrop
import com.ailisi.jinianri.ui.components.SolarDatePickerDialog
import com.ailisi.jinianri.ui.components.colorOf
import com.ailisi.jinianri.ui.components.eventIconOf
import com.ailisi.jinianri.ui.components.iconOf
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    viewModel: MainViewModel,
    eventId: Long,
    presetCategoryId: Long?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
) {
    val categories by viewModel.categories.collectAsState()
    var loaded by remember { mutableStateOf(eventId <= 0) }
    var editing by remember { mutableStateOf<EventEntity?>(null) }

    // 表单状态
    var title by remember { mutableStateOf("") }
    var isLunar by remember { mutableStateOf(false) }
    var solarDate by remember { mutableStateOf(LocalDate.now()) }
    var lunarYear by remember { mutableStateOf(DateEngine.toLunar(LocalDate.now()).first) }
    var lunarMonth by remember { mutableStateOf(DateEngine.toLunar(LocalDate.now()).second) }
    var lunarDay by remember { mutableStateOf(DateEngine.toLunar(LocalDate.now()).third) }
    var repeatYearly by remember { mutableStateOf(true) }
    var countUp by remember { mutableStateOf(false) }
    var categoryId by remember { mutableStateOf<Long?>(presetCategoryId) }
    var iconKey by remember { mutableStateOf("") }
    var remindDays by remember { mutableStateOf<Int?>(null) }
    var note by remember { mutableStateOf("") }

    var showSolarPicker by remember { mutableStateOf(false) }
    var showLunarPicker by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    // 加载已有事件
    LaunchedEffect(eventId) {
        if (eventId > 0) {
            viewModel.eventById(eventId)?.let { e ->
                editing = e
                title = e.title
                isLunar = e.isLunar
                solarDate = LocalDate.ofEpochDay(e.dateEpochDay)
                if (e.isLunar) {
                    lunarYear = DateEngine.toLunar(solarDate).first
                    lunarMonth = e.lunarMonth
                    lunarDay = e.lunarDay
                }
                repeatYearly = e.repeatYearly
                countUp = e.countUp
                categoryId = e.categoryId
                iconKey = e.iconKey
                remindDays = e.remindDaysBefore
                note = e.note
            }
            loaded = true
        }
    }

    GlassBackdrop { _ ->
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (eventId > 0) "编辑纪念日" else "添加纪念日",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            },
        ) { padding ->
            if (!loaded) return@Scaffold
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 1. 基本信息卡片：标题输入 + 3D Emoji 快捷选择
                item {
                    CardSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "基本信息",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                // 3D 图标预览与选择器入口
                                val selectedIcon = eventIconOf(iconKey)
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                                        .border(
                                            1.5.dp,
                                            if (selectedIcon != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                            RoundedCornerShape(18.dp),
                                        )
                                        .clickable { showIconPicker = true },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (selectedIcon != null) {
                                        Image(
                                            painter = painterResource(selectedIcon.resId),
                                            contentDescription = selectedIcon.label,
                                            modifier = Modifier.size(40.dp),
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Filled.EmojiEmotions,
                                                contentDescription = "选择图标",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(26.dp),
                                            )
                                            Text(
                                                "图标",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 10.sp,
                                            )
                                        }
                                    }
                                }

                                // 纪念日名称输入框
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = {
                                        title = it
                                        titleError = false
                                    },
                                    label = { Text("纪念日名称") },
                                    placeholder = { Text("例如：相恋纪念日、妈妈生日") },
                                    isError = titleError,
                                    supportingText = if (titleError) {
                                        { Text("请输入名称") }
                                    } else null,
                                    singleLine = true,
                                    shape = RoundedCornerShape(18.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    ),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                // 2. 日期与日历卡片：公历/农历切换 + 大号可点击日期框 + 每年重复开关
                item {
                    CardSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "日期设定",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(14.dp))

                            // 公历 / 农历分段切换
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                SegmentedButton(
                                    selected = !isLunar,
                                    onClick = { isLunar = false },
                                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                                ) {
                                    Text("公历 (阳历)", fontWeight = if (!isLunar) FontWeight.Bold else FontWeight.Normal)
                                }
                                SegmentedButton(
                                    selected = isLunar,
                                    onClick = { isLunar = true },
                                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                                ) {
                                    Text("农历 (阴历)", fontWeight = if (isLunar) FontWeight.Bold else FontWeight.Normal)
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // 大号日期展示卡片（点击呼出选择器）
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                ),
                                onClick = {
                                    if (isLunar) showLunarPicker = true else showSolarPicker = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Filled.DateRange,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            if (isLunar) "农历日期" else "公历日期",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            if (isLunar) {
                                                "农历 " + DateEngine.lunarMonthDayText(lunarMonth, lunarDay, lunarYear)
                                            } else {
                                                "${solarDate.year}年 ${solarDate.monthValue}月 ${solarDate.dayOfMonth}日"
                                            },
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "修改日期",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // 每年重复开关
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Filled.Repeat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("每年重复", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "生日、纪念日开启；一次性事件（如考试）关闭",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Switch(
                                    checked = repeatYearly,
                                    onCheckedChange = { repeatYearly = it },
                                )
                            }
                        }
                    }
                }

                // 3. 计时与提醒规则卡片
                item {
                    CardSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "计时与提醒",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(14.dp))

                            // 计时方式
                            Text(
                                "计时方向",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(8.dp))
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                SegmentedButton(
                                    selected = !countUp,
                                    onClick = { countUp = false },
                                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                                ) {
                                    Text("倒计时 (还剩 X 天)")
                                }
                                SegmentedButton(
                                    selected = countUp,
                                    onClick = { countUp = true },
                                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                                ) {
                                    Text("正计时 (已过 X 天)")
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            // 提前提醒
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "提前提醒",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                listOf(
                                    null to "不提醒",
                                    0 to "当天",
                                    1 to "提前 1 天",
                                    3 to "提前 3 天",
                                    7 to "提前 7 天",
                                    30 to "提前 30 天",
                                ).forEach { (value, label) ->
                                    val isSelected = remindDays == value
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { remindDays = value },
                                        label = { Text(label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. 分类卡片
                item {
                    CardSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "所属分类",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(12.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(categories.size) { index ->
                                    val category = categories[index]
                                    val isSelected = categoryId == category.id
                                    val catColor = colorOf(category.color)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { categoryId = category.id },
                                        label = {
                                            Text(
                                                category.name,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            )
                                        },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(catColor),
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = catColor.copy(alpha = 0.18f),
                                            selectedLabelColor = catColor,
                                        ),
                                        border = if (isSelected) {
                                            androidx.compose.foundation.BorderStroke(1.5.dp, catColor)
                                        } else null,
                                        shape = RoundedCornerShape(14.dp),
                                    )
                                }
                                item {
                                    AssistChip(
                                        onClick = { showCategoryDialog = true },
                                        label = { Text("新建分类") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. 备注卡片
                item {
                    CardSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "备忘与故事",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = note,
                                onValueChange = { note = it },
                                placeholder = { Text("记录下当时的感动、心愿或备忘事项（选填）...") },
                                minLines = 3,
                                shape = RoundedCornerShape(18.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                // 6. 底部保存 CTA 按钮
                item {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                                return@Button
                            }
                            val resolvedSolar = if (isLunar) {
                                DateEngine.lunarDateInYear(lunarYear, lunarMonth, lunarDay) ?: LocalDate.now()
                            } else solarDate

                            val entity = (editing ?: EventEntity(
                                title = title,
                                dateEpochDay = resolvedSolar.toEpochDay(),
                            )).copy(
                                title = title.trim(),
                                dateEpochDay = resolvedSolar.toEpochDay(),
                                isLunar = isLunar,
                                lunarMonth = if (isLunar) lunarMonth else 0,
                                lunarDay = if (isLunar) lunarDay else 0,
                                repeatYearly = repeatYearly,
                                countUp = countUp,
                                categoryId = categoryId,
                                iconKey = iconKey,
                                remindDaysBefore = remindDays,
                                note = note.trim(),
                            )
                            if (eventId > 0) {
                                viewModel.updateEvent(entity)
                                onSaved(eventId)
                            } else {
                                viewModel.addEvent(entity) { id -> onSaved(id) }
                            }
                        },
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("保存纪念日", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 公历日期选择（自定义对话框：Material3 的 DatePicker 在中文下星期标题显示不完整）
    if (showSolarPicker) {
        SolarDatePickerDialog(
            initialDate = solarDate,
            onConfirm = {
                solarDate = it
                showSolarPicker = false
            },
            onDismiss = { showSolarPicker = false },
        )
    }

    // 农历日期选择
    if (showLunarPicker) {
        LunarPickerDialog(
            initialYear = lunarYear,
            initialMonth = lunarMonth,
            initialDay = lunarDay,
            onConfirm = { y, m, d ->
                lunarYear = y
                lunarMonth = m
                lunarDay = d
                showLunarPicker = false
            },
            onDismiss = { showLunarPicker = false },
        )
    }

    // 事件图标选择
    if (showIconPicker) {
        EventIconPickerDialog(
            initialKey = iconKey,
            onConfirm = {
                iconKey = it
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false },
        )
    }

    // 新建分类
    if (showCategoryDialog) {
        CategoryEditDialog(
            title = "新建分类",
            initialName = "",
            initialColor = 0xFF6750A4,
            initialIcon = "category",
            onConfirm = { name, color, icon ->
                viewModel.addCategory(name, color, icon) { id ->
                    categoryId = id
                }
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false },
        )
    }
}

/** 农历日期选择对话框：年 / 月（含闰月）/ 日 */
@Composable
fun LunarPickerDialog(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    onConfirm: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var year by remember { mutableStateOf(initialYear) }
    var month by remember { mutableStateOf(initialMonth) }
    var day by remember { mutableStateOf(initialDay) }
    val years = remember { (1930..2100).toList() }
    val months = remember(year) { DateEngine.lunarMonthsOf(year) }
    val maxDay = remember(year, month) { DateEngine.lunarMonthDayCount(year, month) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text("选择农历日期", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                // 实时选中的大字预览框
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "农历 " + DateEngine.lunarMonthDayText(month, day.coerceAtMost(maxDay), year),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
                Spacer(Modifier.height(14.dp))
                // 年
                Text("年份", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                WheelRow(
                    values = years,
                    selected = year,
                    display = { "${it}年" },
                    onSelect = { year = it },
                )
                Spacer(Modifier.height(8.dp))
                // 月
                Text("月份", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                WheelRow(
                    values = months,
                    selected = month,
                    display = { m ->
                        val leap = if (m < 0) "闰" else ""
                        leap + "${if (m < 0) -m else m}月"
                    },
                    onSelect = { month = it },
                )
                Spacer(Modifier.height(8.dp))
                // 日
                Text("日期", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                WheelRow(
                    values = (1..maxDay).toList(),
                    selected = day.coerceAtMost(maxDay),
                    display = { d -> "初$d" },
                    onSelect = { day = it },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(year, month, day.coerceAtMost(maxDay)) },
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

@Composable
private fun WheelRow(
    values: List<Int>,
    selected: Int,
    display: (Int) -> String,
    onSelect: (Int) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(values.size) { index ->
            val v = values[index]
            FilterChip(
                selected = v == selected,
                onClick = { onSelect(v) },
                label = { Text(display(v), style = MaterialTheme.typography.labelMedium) },
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

/** 分类新建/编辑对话框：名称 + 颜色 + 图标 + 实时预览 */
@Composable
fun CategoryEditDialog(
    title: String,
    initialName: String,
    initialColor: Long,
    initialIcon: String,
    onConfirm: (String, Long, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var color by remember { mutableStateOf(initialColor) }
    var icon by remember { mutableStateOf(initialIcon) }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // 实时预览标签
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colorOf(color).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colorOf(color).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(colorOf(color)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                iconOf(icon),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            name.ifBlank { "分类名称预览" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorOf(color),
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("分类名称") },
                    placeholder = { Text("例如：工作、恋爱、旅行") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("请输入分类名称") }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(14.dp))
                Text(
                    "主题颜色",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(CategoryColorOptions.size) { index ->
                        val c = CategoryColorOptions[index]
                        val isSelected = c == color
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(colorOf(c))
                                .clickable { color = c },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    "专属图标",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(CategoryIconOptions.size) { index ->
                        val (key, vector) = CategoryIconOptions[index]
                        FilterChip(
                            selected = key == icon,
                            onClick = { icon = key },
                            label = {
                                Icon(vector, contentDescription = key, modifier = Modifier.size(18.dp))
                            },
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onConfirm(name.trim(), color, icon)
                    }
                },
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

/** 事件图标选择对话框：按分组展示 Fluent Emoji，可清除选择 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventIconPickerDialog(
    initialKey: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedKey by remember { mutableStateOf(initialKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text("选择 3D 图标", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                FilterChip(
                    selected = selectedKey.isEmpty(),
                    onClick = { selectedKey = "" },
                    label = { Text("不使用图标") },
                    shape = RoundedCornerShape(12.dp),
                )
                EventIconGroups.forEach { (groupName, icons) ->
                    Spacer(Modifier.height(14.dp))
                    Text(
                        groupName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        icons.forEach { icon ->
                            val selected = icon.key == selectedKey
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                    .border(
                                        if (selected) 2.dp else 1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(16.dp),
                                    )
                                    .clickable {
                                        selectedKey = if (selected) "" else icon.key
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    painter = painterResource(icon.resId),
                                    contentDescription = icon.label,
                                    modifier = Modifier.size(36.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedKey) },
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
