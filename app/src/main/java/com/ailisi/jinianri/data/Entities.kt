package com.ailisi.jinianri.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 纪念日事件。
 *
 * [dateEpochDay] 保存公历日期；农历事件同时保存 [lunarMonth]/[lunarDay]，
 * 用于计算农历每年的下一次发生日。
 */
@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** 公历日期（epochDay），农历事件为对应公历日 */
    val dateEpochDay: Long,
    /** 是否农历 */
    val isLunar: Boolean = false,
    /** 农历月（1-12，闰月为负值，如 -6 表示闰六月） */
    val lunarMonth: Int = 0,
    /** 农历日（1-30） */
    val lunarDay: Int = 0,
    /**
     * 规则型节日的日期规则（空 = 普通事件），如 `term:清明`、`nth:5,SUN,2`。
     * 存了规则的事件，下一次发生日由规则重算，不受 [dateEpochDay] 影响。
     */
    val rule: String = "",
    /** 每年重复 */
    val repeatYearly: Boolean = true,
    /** 计时方式：true = 正计时（已经 N 天），false = 倒计时（还有 N 天） */
    val countUp: Boolean = false,
    /** 分类 id，null = 未分类 */
    val categoryId: Long? = null,
    /** 自定义排序：null = 自动（按最近日期）；用户拖动排序后统一写入 0..n-1 */
    val sortOrder: Int? = null,
    /** 事件图标 key（Fluent Emoji，空 = 未设置，回退分类图标） */
    val iconKey: String = "",
    /** 提前提醒天数：null = 不提醒；0 = 当天；1/3/7/30 */
    val remindDaysBefore: Int? = null,
    /** 备注 */
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * 分类。系统分类「未分类」不可删除、不可重命名。
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** ARGB 颜色 */
    val color: Long,
    /** Material 图标名 */
    val icon: String,
    val sortOrder: Int = 0,
    /** 系统内置（未分类） */
    val isSystem: Boolean = false,
)
