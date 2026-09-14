package com.ailisi.jinianri.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY dateEpochDay ASC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun byId(id: Long): EventEntity?

    @Query("SELECT * FROM events WHERE id = :id")
    fun observeById(id: Long): Flow<EventEntity?>

    @Insert
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    /** 批量更新（拖动排序用） */
    @Update
    suspend fun updateAll(events: List<EventEntity>)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("SELECT COUNT(*) FROM events WHERE categoryId = :categoryId")
    suspend fun countInCategory(categoryId: Long): Int

    /** 当前最大自定义顺序；全为 null（自动排序）时返回 null */
    @Query("SELECT MAX(sortOrder) FROM events")
    suspend fun maxSortOrder(): Int?
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    suspend fun all(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun byId(id: Long): CategoryEntity?

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Update
    suspend fun updateAll(categories: List<CategoryEntity>)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    /** 把该分类下的事件移到未分类（categoryId = null），再删除分类 */
    @Transaction
    suspend fun deleteAndDetach(category: CategoryEntity) {
        detachEvents(category.id)
        delete(category)
    }

    @Query("UPDATE events SET categoryId = NULL WHERE categoryId = :categoryId")
    suspend fun detachEvents(categoryId: Long)
}
