package com.ailisi.jinianri.data

import kotlinx.coroutines.flow.Flow

/** 数据仓库：事件与分类的统一入口 */
class JinianriRepository(private val db: JinianriDatabase) {    fun observeEvents(): Flow<List<EventEntity>> = db.events().observeAll()
    fun observeEvent(id: Long): Flow<EventEntity?> = db.events().observeById(id)
    fun observeCategories(): Flow<List<CategoryEntity>> = db.categories().observeAll()

    suspend fun eventById(id: Long): EventEntity? = db.events().byId(id)
    suspend fun categories(): List<CategoryEntity> = db.categories().all()
    suspend fun categoryById(id: Long): CategoryEntity? = db.categories().byId(id)

    suspend fun addEvent(event: EventEntity): Long {
        // 已经处于自定义排序模式时，新事件追加到末尾
        val maxOrder = db.events().maxSortOrder()
        val ordered = if (maxOrder != null) event.copy(sortOrder = maxOrder + 1) else event
        return db.events().insert(ordered)
    }

    /**
     * 批量导入节日：统一归入「节日」分类（分类不存在则新建），
     * 若当前处于自定义排序模式，则按顺序追加到末尾。
     *
     * @return 实际写入的条数
     */
    suspend fun importFestivals(events: List<EventEntity>): Int {
        if (events.isEmpty()) return 0
        val categoryId = ensureFestivalCategoryId()
        val maxOrder = db.events().maxSortOrder()
        val ordered = events.mapIndexed { index, event ->
            event.copy(categoryId = categoryId, sortOrder = maxOrder?.plus(index + 1))
        }
        db.events().insertAll(ordered)
        return ordered.size
    }

    /** 「节日」分类的 id：已有同名分类就复用（用户在分类管理里改过色也保留），否则新建 */
    private suspend fun ensureFestivalCategoryId(): Long {
        val categories = db.categories().all()
        categories.firstOrNull { it.name == FESTIVAL_CATEGORY }?.let { return it.id }
        val order = categories.maxOfOrNull { it.sortOrder }?.plus(1) ?: 0
        return db.categories().insert(
            CategoryEntity(
                name = FESTIVAL_CATEGORY,
                color = FESTIVAL_CATEGORY_COLOR,
                icon = "celebration",
                sortOrder = order,
            )
        )
    }

    suspend fun updateEvent(event: EventEntity) = db.events().update(event.copy(updatedAt = System.currentTimeMillis()))
    suspend fun updateEvents(events: List<EventEntity>) = db.events().updateAll(events)
    suspend fun deleteEvent(event: EventEntity) = db.events().delete(event)

    suspend fun addCategory(category: CategoryEntity): Long {
        val order = db.categories().all().maxOfOrNull { it.sortOrder } ?: 0
        return db.categories().insert(category.copy(sortOrder = order + 1))
    }

    suspend fun updateCategory(category: CategoryEntity) = db.categories().update(category)
    suspend fun updateCategories(categories: List<CategoryEntity>) = db.categories().updateAll(categories)

    /** 删除分类：旗下事件移入未分类 */
    suspend fun deleteCategory(category: CategoryEntity) {
        if (category.isSystem) return
        db.categories().deleteAndDetach(category)
    }

    suspend fun countInCategory(categoryId: Long): Int = db.events().countInCategory(categoryId)

    companion object {
        /** 节日导入落到的分类名 */
        const val FESTIVAL_CATEGORY = "节日"

        /** 节日分类默认色（中国红） */
        private const val FESTIVAL_CATEGORY_COLOR = 0xFFE53935
    }
}
