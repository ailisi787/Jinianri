package com.ailisi.jinianri.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemInfo
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ailisi.jinianri.data.CategoryEntity
import com.ailisi.jinianri.domain.DateEngine
import com.ailisi.jinianri.ui.EventCard
import com.ailisi.jinianri.ui.MainViewModel
import com.ailisi.jinianri.ui.components.CardSurface
import com.ailisi.jinianri.ui.components.GlassBackdrop
import com.ailisi.jinianri.ui.components.LocalBackdrop
import com.ailisi.jinianri.ui.components.colorOf
import com.ailisi.jinianri.ui.components.eventIconOf
import com.ailisi.jinianri.ui.components.iconOf
import com.ailisi.jinianri.ui.components.LiquidGlassBox
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onAddEvent: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    onOpenCategories: () -> Unit,
) {
    val cards by viewModel.filteredCards.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val selected by viewModel.selectedCategoryId.collectAsState()
    val cardMode by viewModel.cardMode.collectAsState()

    val today = remember { LocalDate.now() }
    val weekDay = remember(today) {
        when (today.dayOfWeek) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
            else -> ""
        }
    }
    val dateSubtitle = remember(today) {
        "${today.monthValue}月${today.dayOfMonth}日 $weekDay · 农历${DateEngine.lunarTextOf(today)}"
    }

    // 所有卡片统一尺寸、都可长按拖动排序
    val displayCards = cards

    // 拖拽排序所需的网格状态、控制器与"列表内容层" backdrop（拖动时才会录制，避免常态开销）
    val gridState = rememberLazyStaggeredGridState()
    val dragController = rememberCardDragController(gridState)
    val listBackdrop = rememberLayerBackdrop()
    val gridCoordinates = remember { mutableStateOf<LayoutCoordinates?>(null) }
    val itemCoordinates = remember { mutableMapOf<Long, LayoutCoordinates>() }

    GlassBackdrop { backdrop ->
        val dragBackdrop = rememberCombinedBackdrop(backdrop, listBackdrop)
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                GlassFab(
                    onClick = onAddEvent,
                    modifier = Modifier.padding(bottom = 88.dp, end = 4.dp),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                Spacer(Modifier.height(14.dp))

                // 顶部标题栏 + 日期 + 视图切换胶囊
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dateSubtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "纪念日",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            ) {
                                Text(
                                    text = "${cards.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }

                    // 视图模式切换小胶囊：液态玻璃材质
                    LiquidGlassBox(
                        shape = RoundedCornerShape(16.dp),
                        tintColor = Color.White.copy(alpha = 0.72f),
                        lensStrength = 8.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(
                                        if (cardMode) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable { if (!cardMode) viewModel.toggleCardMode() }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.GridView,
                                    contentDescription = "网格视图",
                                    tint = if (cardMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(17.dp),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(
                                        if (!cardMode) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable { if (cardMode) viewModel.toggleCardMode() }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ViewAgenda,
                                    contentDescription = "列表视图",
                                    tint = if (!cardMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(17.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // 搜索框：纯白底色、细微浅边、清空按钮
                SearchField(
                    value = query,
                    onValueChange = viewModel::setSearch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                )

                Spacer(Modifier.height(10.dp))

                // 分类标签栏
                CategoryChipsRow(
                    categories = categories,
                    selectedId = selected,
                    onSelect = viewModel::selectCategory,
                )

                Spacer(Modifier.height(8.dp))

                // 内容区：空状态 / 支持长按拖动排序的流式布局（网格与列表共用一套拖拽逻辑）
                if (cards.isEmpty()) {
                    EmptyHint(
                        hasQuery = query.isNotEmpty() || selected != null,
                        onAdd = onAddEvent,
                    )
                } else {
                    val cardsById = remember(displayCards) { displayCards.associateBy { it.event.id } }
                    val displayIds = remember(displayCards) { displayCards.map { it.event.id } }
                    LaunchedEffect(displayIds) { dragController.updateBase(displayIds) }
                    dragController.onDrop = viewModel::reorderCards
                    val orderedCards = (dragController.orderIds ?: displayIds).mapNotNull { cardsById[it] }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(if (cardMode) 2 else 1),
                            state = gridState,
                            modifier = Modifier
                                .fillMaxSize()
                                .onGloballyPositioned { gridCoordinates.value = it }
                                .then(
                                    if (dragController.isDragging) Modifier.layerBackdrop(listBackdrop)
                                    else Modifier
                                ),
                            contentPadding = PaddingValues(
                                start = 20.dp, end = 20.dp, top = 4.dp, bottom = 130.dp,
                            ),
                            verticalItemSpacing = if (cardMode) 12.dp else 10.dp,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(orderedCards, key = { it.event.id }) { card ->
                                val isDragging = dragController.draggingId == card.event.id
                                val haptics = LocalHapticFeedback.current
                                val itemModifier = Modifier
                                    .then(if (isDragging) Modifier else Modifier.animateItem())
                                    .alpha(if (isDragging) 0f else 1f)
                                    .onGloballyPositioned { itemCoordinates[card.event.id] = it }
                                    .pointerInput(card.event.id) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { local ->
                                                val info = gridState.layoutInfo.visibleItemsInfo
                                                    .firstOrNull { it.key == card.event.id }
                                                if (info != null) {
                                                    val actual = actualPositionOf(
                                                        gridCoordinates.value,
                                                        itemCoordinates[card.event.id],
                                                    )
                                                    dragController.start(
                                                        id = card.event.id,
                                                        layoutOffset = info.offset,
                                                        actualPosition = actual,
                                                        local = local,
                                                        size = info.size,
                                                    )
                                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            },
                                            onDrag = { change, amount ->
                                                change.consume()
                                                dragController.onDrag(amount)
                                            },
                                            onDragEnd = { dragController.end() },
                                            onDragCancel = { dragController.cancel() },
                                        )
                                    }
                                if (cardMode) {
                                    EventGridCard(
                                        card = card,
                                        categories = categories,
                                        onClick = { onOpenEvent(card.event.id) },
                                        modifier = itemModifier,
                                    )
                                } else {
                                    EventRow(
                                        card = card,
                                        categories = categories,
                                        onClick = { onOpenEvent(card.event.id) },
                                        modifier = itemModifier,
                                    )
                                }
                            }
                        }

                        // 悬浮的液态玻璃卡片：跟手移动，并实时折射下方卡片与背景
                        CardDragOverlay(
                            controller = dragController,
                            cardsById = cardsById,
                            categories = categories,
                            cardMode = cardMode,
                            backdrop = dragBackdrop,
                        )
                    }
                }
            }
        }
    }
}

/** 现代液态玻璃质感搜索框 */
@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LiquidGlassBox(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        tintColor = Color.White.copy(alpha = 0.75f),
        lensStrength = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        "搜索纪念日名称或备注...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            AnimatedVisibility(
                visible = value.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(20.dp),
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "清空搜索",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/** 分类标签栏：圆润胶囊药丸 */
@Composable
private fun CategoryChipsRow(
    categories: List<CategoryEntity>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            CategoryChip(
                label = "全部",
                selected = selectedId == null,
                onClick = { onSelect(null) },
            )
        }
        items(categories, key = { it.id }) { category ->
            CategoryChip(
                label = category.name,
                selected = selectedId == category.id,
                dotColor = colorOf(category.color),
                onClick = { onSelect(category.id) },
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    dotColor: Color? = null,
    onClick: () -> Unit,
) {
    LiquidGlassBox(
        shape = RoundedCornerShape(16.dp),
        tintColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
                    else Color.White.copy(alpha = 0.72f),
        lensStrength = 8.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (selected) Color.White else dotColor),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** 事件列表卡片：左色条 + 极简纯白（Todoist / Linear 风格） */
@Composable
private fun EventRow(
    card: EventCard,
    categories: List<CategoryEntity>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glassLift: Float = 0f,
) {
    val category = categories.firstOrNull { it.id == card.event.categoryId }
    val catColor = colorOf(category?.color ?: 0xFF6750A4)
    val eventIcon = eventIconOf(card.event.iconKey)
    val isPast = !card.event.countUp && card.daysUntil < 0
    val number = if (card.event.countUp) card.daysSince else abs(card.daysUntil)

    CardSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        containerColor = Color.White,
        borderColor = Color(0xFFEBECEF),
        onClick = onClick,
        glassLift = glassLift,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(catColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                if (eventIcon != null) {
                    Image(
                        painter = painterResource(eventIcon.resId),
                        contentDescription = eventIcon.label,
                        modifier = Modifier.size(32.dp),
                    )
                } else {
                    Icon(
                        imageVector = iconOf(category?.icon ?: "category"),
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(21.dp),
                    )
                }
            }

            Spacer(Modifier.width(13.dp))

            // 标题与次要信息
            Column(Modifier.weight(1f)) {
                Text(
                    text = card.event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (category != null) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = catColor,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD1D5DB))
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = gridDateTextOf(card),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // 右侧天数倒计胶囊
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (card.isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else catColor.copy(alpha = 0.08f),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (card.isToday) {
                        Text(
                            text = "🎉",
                            fontSize = 13.sp,
                        )
                        Text(
                            text = "今天",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Text(
                            text = if (card.event.countUp) "已" else if (isPast) "已过" else "还有",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                        )
                        Text(
                            text = "$number 天",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = catColor,
                        )
                    }
                }
            }
        }
    }
}

/** 双列网格卡片：顶部色条 + 原始布局（分类标签 + 数字 + Emoji 右下） */
@Composable
private fun EventGridCard(
    card: EventCard,
    categories: List<CategoryEntity>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glassLift: Float = 0f,
) {
    val category = categories.firstOrNull { it.id == card.event.categoryId }
    val catColor = colorOf(category?.color ?: 0xFF6750A4)
    val eventIcon = eventIconOf(card.event.iconKey)
    val isPast = !card.event.countUp && card.daysUntil < 0
    val number = if (card.event.countUp) card.daysSince else abs(card.daysUntil)

    CardSurface(
        shape = RoundedCornerShape(18.dp),
        containerColor = Color.White,
        borderColor = Color(0xFFEBECEF),
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        glassLift = glassLift,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
                // 顶栏：分类小徽章与重复标记
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (category != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = catColor.copy(alpha = 0.08f),
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = catColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    if (card.event.repeatYearly) {
                        Icon(
                            Icons.Filled.Repeat,
                            contentDescription = "每年重复",
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 标题
                Text(
                    text = card.event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(8.dp))

                // 数字 + Emoji 图标（底部行）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        if (card.isToday) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = catColor.copy(alpha = 0.10f),
                            ) {
                                Text(
                                    text = "今天 🎉",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = catColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        } else {
                            Text(
                                text = if (card.event.countUp) "已经" else if (isPast) "已过去" else "还有",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$number",
                                    fontSize = 32.sp,
                                    lineHeight = 34.sp,
                                    fontWeight = FontWeight.Black,
                                    color = catColor,
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = "天",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                            }
                        }
                    }

                    if (eventIcon != null) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(catColor.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(eventIcon.resId),
                                contentDescription = eventIcon.label,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 脚注日期
                Text(
                    text = gridDateTextOf(card),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp,
                )
            }
        }
    }


/** 卡片脚注：农历事件显示农历，其余显示目标公历日期 */
private fun gridDateTextOf(card: EventCard): String {
    val d = card.nextDate
    return if (card.event.isLunar) {
        "农历 " + DateEngine.lunarMonthDayText(card.event.lunarMonth, card.event.lunarDay, d.year)
    } else {
        "%d年%02d月%02d日".format(d.year, d.monthValue, d.dayOfMonth)
    }
}

/** 「已经 N 天」还是「还有 N 天」 */
fun daysTextOf(card: EventCard): String = when {
    card.event.countUp -> "已经 ${card.daysSince} 天"
    card.daysUntil == 0L -> "就是今天"
    card.daysUntil < 0 -> "已经 ${-card.daysUntil} 天"
    else -> "还有 ${card.daysUntil} 天"
}

@Composable
private fun EmptyHint(hasQuery: Boolean, onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 大 Emoji 圆形容器
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (hasQuery) "🔍" else "📅",
                fontSize = 40.sp,
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = if (hasQuery) "未找到匹配的纪念日" else "还没有纪念日",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (hasQuery) "试试换个关键词，或切换其他分类" else "记录每一个闪光时刻，从这里开始",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        if (!hasQuery) {
            Spacer(Modifier.height(24.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primary,
                onClick = onAdd,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "添加第一个纪念日",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

/** 悬浮添加按钮（在 Scaffold FAB slot 中，LocalBackdrop 不可用，故使用标准 FAB） */
@Composable
fun GlassFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
        ),
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = "添加纪念日",
            tint = Color.White,
            modifier = Modifier.size(26.dp),
        )
    }
}

/** 卡片在网格坐标系中的实际位置；坐标系未就绪时返回 null，由调用方回退到 layoutInfo */
private fun actualPositionOf(
    gridCoordinates: LayoutCoordinates?,
    itemCoordinates: LayoutCoordinates?,
): Offset? {
    if (gridCoordinates == null || itemCoordinates == null) return null
    if (!gridCoordinates.isAttached || !itemCoordinates.isAttached) return null
    return gridCoordinates.localPositionOf(itemCoordinates, Offset.Zero)
}

@Composable
private fun rememberCardDragController(gridState: LazyStaggeredGridState): CardDragController {
    val scope = rememberCoroutineScope()
    return remember(gridState, scope) { CardDragController(gridState, scope) }
}

/**
 * 首页卡片拖拽排序控制器：长按拾起 → 跟手移动 → 松手回弹到目标格并持久化顺序。
 *
 * 拖动期间维持一份临时顺序（[orderIds]），网格实时按新顺序排布（配合 animateItem 位移动画），
 * 被拖动的卡片本体隐藏，由 [CardDragOverlay] 渲染一张悬浮玻璃卡片跟手移动。
 */
@Stable
private class CardDragController(
    private val gridState: LazyStaggeredGridState,
    private val scope: CoroutineScope,
) {
    /** 拖动过程中的临时顺序（卡片 id）；为空时使用外部（数据库）顺序 */
    var orderIds: List<Long>? by mutableStateOf(null)
        private set

    /** 正在拖动的卡片 id */
    var draggingId: Long? by mutableStateOf(null)
        private set

    /** 被拖动卡片的尺寸 */
    var draggingSize: IntSize by mutableStateOf(IntSize.Zero)
        private set

    /** 悬浮卡片左上角在网格坐标系中的位置 */
    var overlayOffset: Offset by mutableStateOf(Offset.Zero)
        private set

    /** 是否正在回弹吸附动画 */
    var settling: Boolean by mutableStateOf(false)
        private set

    private val settleAnimation = Animatable(Offset.Zero, Offset.VectorConverter)
    val settleOffset: Offset get() = settleAnimation.value

    private val liftAnimation = Animatable(0f)

    /** 玻璃提升量：1 = 悬空折射态，0 = 静止态；落位后平滑回到 0，避免撤层时突然变色 */
    val lift: Float get() = liftAnimation.value

    /** 拖动排序提交回调（参数为可见卡片的新顺序） */
    var onDrop: ((List<Long>) -> Unit)? = null

    private var baseIds: List<Long> = emptyList()
    private var grabOffset = Offset.Zero
    private var pointer = Offset.Zero
    private var boundsDelta = Offset.Zero
    private var autoScrollJob: Job? = null

    val isDragging: Boolean get() = draggingId != null

    /** 当前生效的卡片顺序 */
    val workingIds: List<Long> get() = orderIds ?: baseIds

    /** 数据库顺序变化时同步；与临时顺序一致（或卡片集合变化）时结束临时顺序 */
    fun updateBase(ids: List<Long>) {
        baseIds = ids
        val current = orderIds
        if (current != null && (current == ids || current.toSet() != ids.toSet())) {
            orderIds = null
        }
    }

    fun start(
        id: Long,
        layoutOffset: IntOffset,
        actualPosition: Offset?,
        local: Offset,
        size: IntSize,
    ) {
        if (draggingId != null) return
        val ids = workingIds
        if (id !in ids) return
        orderIds = ids
        draggingId = id
        draggingSize = size
        grabOffset = local
        val layoutTopLeft = Offset(layoutOffset.x.toFloat(), layoutOffset.y.toFloat())
        val itemTopLeft = actualPosition ?: layoutTopLeft
        boundsDelta = itemTopLeft - layoutTopLeft
        pointer = itemTopLeft + local
        overlayOffset = itemTopLeft
        settling = false
        startAutoScroll()
        scope.launch {
            liftAnimation.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 500f))
        }
    }

    fun onDrag(delta: Offset) {
        val id = draggingId ?: return
        if (settling) return
        pointer += delta
        overlayOffset = pointer - grabOffset

        val ids = orderIds ?: return
        val candidates = gridState.layoutInfo.visibleItemsInfo.filter { info ->
            val key = info.key as? Long
            key != null && key != id && key in ids
        }
        val target = candidates.firstOrNull { pointer in it.bounds().translate(boundsDelta) }
            ?: candidates.minByOrNull { distanceSquared(pointer, it.bounds().translate(boundsDelta)) }
        if (target != null) {
            val targetKey = target.key as? Long
            val to = targetKey?.let { ids.indexOf(it) } ?: -1
            val from = ids.indexOf(id)
            if (to >= 0 && from >= 0 && to != from) {
                val rect = target.bounds().translate(boundsDelta)
                val after = when {
                    pointer.y > rect.center.y + rect.height * 0.15f -> true
                    pointer.y < rect.center.y - rect.height * 0.15f -> false
                    else -> pointer.x > rect.center.x
                }
                val dest = if (to > from) {
                    if (after) to else to - 1
                } else {
                    if (after) to + 1 else to
                }
                val next = ids.toMutableList()
                next.removeAt(from)
                next.add(dest.coerceIn(0, next.size), id)
                orderIds = next
            }
        }
    }

    fun end() = settle(commit = true)

    fun cancel() = settle(commit = false)

    /** 拖到网格上/下边缘时持续自动滚动 */
    private fun startAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = scope.launch {
            while (isActive && draggingId != null) {
                withFrameNanos { }
                val viewport = gridState.layoutInfo.viewportSize
                if (viewport.height <= 0) continue
                val threshold = 96f
                val maxSpeed = 16f
                val dy = when {
                    pointer.y < threshold -> -(threshold - pointer.y) / threshold * maxSpeed
                    pointer.y > viewport.height - threshold ->
                        (pointer.y - (viewport.height - threshold)) / threshold * maxSpeed
                    else -> 0f
                }
                if (dy != 0f) gridState.scrollBy(dy)
            }
        }
    }

    private fun settle(commit: Boolean) {
        val id = draggingId ?: return
        if (settling) return
        settling = true
        autoScrollJob?.cancel()
        autoScrollJob = null
        val ids = orderIds
        val changed = commit && ids != null && ids != baseIds
        scope.launch {
            if (commit) {
                // 等一帧，让最后一次排序完成布局，再取目标格位置做回弹吸附
                withFrameNanos { }
                val slot = gridState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == id }
                if (slot != null) {
                    settleAnimation.snapTo(overlayOffset)
                    settleAnimation.animateTo(
                        Offset(slot.offset.x.toFloat(), slot.offset.y.toFloat()) + boundsDelta,
                        spring(dampingRatio = 0.75f, stiffness = 420f),
                    )
                }
            }
            if (changed) onDrop?.invoke(ids)
            // 落位后玻璃从"悬空折射态"平滑回到"静止态"，再撤掉悬浮层，避免颜色突然跳变
            liftAnimation.animateTo(0f, spring(dampingRatio = 1f, stiffness = 420f))
            draggingId = null
            settling = false
            orderIds = if (changed) ids else null
        }
    }

    private fun LazyStaggeredGridItemInfo.bounds(): Rect = Rect(
        left = offset.x.toFloat(),
        top = offset.y.toFloat(),
        right = (offset.x + size.width).toFloat(),
        bottom = (offset.y + size.height).toFloat(),
    )

    private fun distanceSquared(point: Offset, rect: Rect): Float {
        val dx = point.x - rect.center.x
        val dy = point.y - rect.center.y
        return dx * dx + dy * dy
    }
}

/** 拖拽中的悬浮玻璃卡片：跟手浮在内容之上，并实时折射下方卡片与背景 */
@Composable
private fun CardDragOverlay(
    controller: CardDragController,
    cardsById: Map<Long, EventCard>,
    categories: List<CategoryEntity>,
    cardMode: Boolean,
    backdrop: Backdrop,
) {
    val draggingId = controller.draggingId ?: return
    val card = cardsById[draggingId] ?: return
    val density = LocalDensity.current
    val lift = controller.lift
    val topLeft = if (controller.settling) controller.settleOffset else controller.overlayOffset
    val dragWidth = with(density) { controller.draggingSize.width.toDp() }
    val dragHeight = with(density) { controller.draggingSize.height.toDp() }

    CompositionLocalProvider(LocalBackdrop provides backdrop) {
        Box(
            modifier = Modifier
                .offset { IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()) }
                .size(dragWidth, dragHeight)
                .graphicsLayer {
                    val scale = 1f + 0.05f * lift
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            if (cardMode) {
                EventGridCard(
                    card = card,
                    categories = categories,
                    onClick = {},
                    glassLift = lift,
                )
            } else {
                EventRow(
                    card = card,
                    categories = categories,
                    onClick = {},
                    glassLift = lift,
                )
            }
        }
    }
}


