package com.ailisi.jinianri.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ailisi.jinianri.JinianriApp
import com.ailisi.jinianri.data.CategoryEntity
import com.ailisi.jinianri.data.EventEntity
import com.ailisi.jinianri.domain.DateEngine
import com.ailisi.jinianri.domain.Festival
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 首页列表项：事件 + 计算好的时间信息 */
data class EventCard(
    val event: EventEntity,
    val nextDate: LocalDate,
    val daysUntil: Long,
    val daysSince: Long,
    val isToday: Boolean,
)

/** 分类 + 事件数量 */
data class CategoryWithCount(
    val category: CategoryEntity,
    val count: Int,
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as JinianriApp).repository
    private val uiPrefs = app.getSharedPreferences("jinianri_ui", Context.MODE_PRIVATE)

    /** 首页展示模式：true = 双列卡片，false = 列表（持久化） */
    private val _cardMode = MutableStateFlow(uiPrefs.getBoolean("home_card_mode", true))
    val cardMode: StateFlow<Boolean> = _cardMode

    fun toggleCardMode() {
        val next = !_cardMode.value
        _cardMode.value = next
        uiPrefs.edit().putBoolean("home_card_mode", next).apply()
    }

    /** 主题配色 key（持久化） */
    private val _themeKey = MutableStateFlow(uiPrefs.getString("theme_key", "violet") ?: "violet")
    val themeKey: StateFlow<String> = _themeKey

    fun setTheme(key: String) {
        _themeKey.value = key
        uiPrefs.edit().putString("theme_key", key).apply()
    }

    /** 搜索关键字 */
    val searchQuery = MutableStateFlow("")

    /** 当前选中的分类筛选：null = 全部 */
    val selectedCategoryId = MutableStateFlow<Long?>(null)

    /**
     * 今天。应用回到前台时由 [refreshToday] 更新，
     * 否则跨过午夜后首页倒计时会一直停在昨天。
     */
    private val _today = MutableStateFlow(LocalDate.now())
    val today: StateFlow<LocalDate> = _today

    fun refreshToday() {
        val current = LocalDate.now()
        if (current != _today.value) _today.value = current
    }

    val categories: StateFlow<List<CategoryEntity>> =
        repo.observeCategories().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * 事件表是否已经读到过至少一次真实结果。
     *
     * `stateIn` 的初始值是空列表，首帧分不清「正在读」和「确实没有事件」，
     * 界面会闪一下「还没有纪念日」。等到第一次真实发射后再允许展示空状态。
     */
    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded

    val cards: StateFlow<List<EventCard>> =
        combine(
            repo.observeEvents().onEach { _loaded.value = true },
            _today,
        ) { events, today ->
            events.map { event ->
                val timing = DateEngine.timingOf(
                    dateEpochDay = event.dateEpochDay,
                    repeatYearly = event.repeatYearly,
                    isLunar = event.isLunar,
                    lunarMonth = event.lunarMonth,
                    lunarDay = event.lunarDay,
                    rule = event.rule,
                    today = today,
                )
                EventCard(
                    event = event,
                    nextDate = timing.nextDate,
                    daysUntil = timing.daysUntil,
                    daysSince = timing.daysSince,
                    // 单次事件在今天也是「就是今天」，与详情页口径保持一致
                    isToday = timing.daysUntil == 0L,
                )
            }.sortedWith(
                compareBy(
                    { it.event.sortOrder ?: Int.MAX_VALUE },
                    { it.daysUntil },
                    { it.event.title },
                )
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** 筛选后的卡片：分类过滤 + 搜索 */
    val filteredCards: StateFlow<List<EventCard>> =
        combine(cards, searchQuery, selectedCategoryId) { list, query, categoryId ->
            list.filter { card ->
                val matchCategory = categoryId == null || card.event.categoryId == categoryId
                val q = query.trim()
                val matchQuery = q.isEmpty() ||
                    card.event.title.contains(q, ignoreCase = true) ||
                    card.event.note.contains(q, ignoreCase = true)
                matchCategory && matchQuery
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** 各分类下的事件数量（用于分类管理显示） */
    val categoryCounts: StateFlow<Map<Long, Int>> =
        repo.observeEvents().combine(categories) { events, cats ->
            cats.associate { cat -> cat.id to events.count { it.categoryId == cat.id } }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /** 未分类数量 */
    val uncategorizedCount: StateFlow<Int> =
        repo.observeEvents().combine(categories) { events, cats ->
            val ids = cats.map { it.id }.toSet()
            events.count { it.categoryId == null || it.categoryId !in ids }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun setSearch(query: String) {
        searchQuery.value = query
    }

    fun selectCategory(id: Long?) {
        selectedCategoryId.value = id
    }

    fun addEvent(event: EventEntity, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repo.addEvent(event)
            onDone(id)
        }
    }

    /**
     * 导入节日：按今天算好每个节日的下一次发生日，批量写入并归入「节日」分类。
     *
     * @param onDone 返回实际导入条数
     */
    fun importFestivals(festivals: List<Festival>, onDone: (Int) -> Unit = {}) {
        if (festivals.isEmpty()) {
            onDone(0)
            return
        }
        viewModelScope.launch {
            val from = LocalDate.now()
            val events = festivals.mapNotNull { festival ->
                festival.nextDate(from)?.let { festival.toEvent(it) }
            }
            onDone(repo.importFestivals(events))
        }
    }

    /**
     * 应用首页拖拽后的新顺序。
     *
     * [visibleOrder] 是当前可见卡片的 id 新顺序；未出现在其中的卡片保持原有相对位置，
     * 最后把完整顺序写入每个事件的 sortOrder（0..n-1）。
     */
    fun reorderCards(visibleOrder: List<Long>) {
        if (visibleOrder.isEmpty()) return
        viewModelScope.launch {
            val full = cards.value.map { it.event }
            if (full.isEmpty()) return@launch
            val fullIds = full.map { it.id }.toSet()
            val incoming = visibleOrder.filter { it in fullIds }.distinct()
            if (incoming.isEmpty()) return@launch
            val visible = incoming.toSet()
            val queue = ArrayDeque(incoming)
            val merged = full.map { event ->
                if (event.id in visible) queue.removeFirstOrNull() ?: event.id else event.id
            }
            val byId = full.associateBy { it.id }
            val updated = merged.mapIndexedNotNull { index, id ->
                val event = byId[id] ?: return@mapIndexedNotNull null
                if (event.sortOrder == index) null else event.copy(sortOrder = index)
            }
            if (updated.isNotEmpty()) repo.updateEvents(updated)
        }
    }

    fun updateEvent(event: EventEntity) {
        viewModelScope.launch { repo.updateEvent(event) }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch { repo.deleteEvent(event) }
    }

    suspend fun eventById(id: Long): EventEntity? = repo.eventById(id)

    fun observeEvent(id: Long) = repo.observeEvent(id)

    fun addCategory(name: String, color: Long, icon: String, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repo.addCategory(CategoryEntity(name = name, color = color, icon = icon))
            onDone(id)
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch { repo.updateCategory(category) }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch { repo.deleteCategory(category) }
    }

    fun moveCategory(from: Int, to: Int) {
        viewModelScope.launch {
            val list = categories.value.toMutableList()
            if (from !in list.indices || to !in list.indices) return@launch
            val item = list.removeAt(from)
            list.add(to, item)
            repo.updateCategories(list.mapIndexed { index, c -> c.copy(sortOrder = index) })
        }
    }
}
