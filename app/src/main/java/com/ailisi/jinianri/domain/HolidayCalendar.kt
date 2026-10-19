package com.ailisi.jinianri.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

/**
 * 法定节假日放假安排。
 *
 * 精确的放假起止日与调休补班日由国务院每年发文公布，本应用不联网，
 * 因此按「法定天数 + 典型连休长度」推算：元旦 / 清明 / 端午 / 中秋 3 天、
 * 劳动节 5 天、春节 8 天（除夕起）、国庆 7 天。
 *
 * 推算出的起止日与官方可能差一两天，也标不出调休补班日；表结构已为官方数据留好位置——
 * 以后拿到通知，在 [OFFICIAL] 里补一年即可精确覆盖，界面无需改动。
 */
object HolidayCalendar {

    /** 一段放假区间（含首尾两天） */
    data class Break(
        val name: String,
        val start: LocalDate,
        val end: LocalDate,
        /** 调休补班日（规则推算时为空） */
        val workdays: List<LocalDate> = emptyList(),
        /** 是否来自官方通知（false = 按规则推算） */
        val official: Boolean = false,
    ) {
        /** 连休天数 */
        val days: Int = (ChronoUnit.DAYS.between(start, end) + 1).toInt()

        fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)

        /** 如「10月1日 – 10月7日」 */
        val rangeText: String
            get() = "${start.monthValue}月${start.dayOfMonth}日 – ${end.monthValue}月${end.dayOfMonth}日"
    }

    private class Rule(
        val name: String,
        /** 连休天数 */
        val days: Int,
        /** 该年放假首日 */
        val firstDay: (Int) -> LocalDate?,
    )

    private val RULES: List<Rule> = listOf(
        Rule("元旦", 3) { year -> LocalDate.of(year, 1, 1) },
        // 春节假期从除夕开始，除夕即正月初一的前一天
        Rule("春节", 8) { year -> DateEngine.lunarDateInYear(year, 1, 1)?.minusDays(1) },
        Rule("清明节", 3) { year -> FestivalRules.occurrenceInYear(FestivalRules.termRule("清明"), year) },
        Rule("劳动节", 5) { year -> LocalDate.of(year, 5, 1) },
        Rule("端午节", 3) { year -> DateEngine.lunarDateInYear(year, 5, 5) },
        Rule("中秋节", 3) { year -> DateEngine.lunarDateInYear(year, 8, 15) },
        Rule("国庆节", 7) { year -> LocalDate.of(year, 10, 1) },
    )

    /**
     * 官方精确安排（国务院通知）：`年份 -> 放假区间`，优先于规则推算。
     *
     * 补数据时照这个格式加一行即可（含调休补班日）：
     * ```
     * 2026 to listOf(
     *     Break("春节", LocalDate.of(2026, 2, 15), LocalDate.of(2026, 2, 22),
     *           workdays = listOf(LocalDate.of(2026, 2, 14), LocalDate.of(2026, 2, 28)), official = true),
     * )
     * ```
     */
    private val OFFICIAL: Map<Int, List<Break>> = emptyMap()

    private val cache = ConcurrentHashMap<Int, List<Break>>()

    /** [year] 年的放假区间（按日期先后排序） */
    fun breaksInYear(year: Int): List<Break> = cache.getOrPut(year) { OFFICIAL[year] ?: derive(year) }

    /** [date] 落在哪段假期里；不在假期中返回 null */
    fun breakOn(date: LocalDate): Break? = breaksInYear(date.year).firstOrNull { it.contains(date) }

    /** 从 [from]（含当天）起的接下来 [count] 段假期，跨年自动续取 */
    fun upcoming(from: LocalDate, count: Int = 3): List<Break> {
        val result = mutableListOf<Break>()
        var year = from.year
        while (result.size < count && year <= from.year + 1) {
            for (item in breaksInYear(year)) {
                if (result.size >= count) break
                if (!item.end.isBefore(from)) result.add(item)
            }
            year++
        }
        return result
    }

    /** 该节日名对应的连休天数（不是放假节日返回 null） */
    fun breakDaysOf(name: String): Int? = RULES.firstOrNull { it.name == name }?.days

    /** 按规则推算一年的放假区间；重叠的假期（中秋撞国庆）合并为一段 */
    private fun derive(year: Int): List<Break> {
        val raw = RULES
            .mapNotNull { rule ->
                rule.firstDay(year)?.let { start -> Break(rule.name, start, start.plusDays(rule.days - 1L)) }
            }
            .sortedBy { it.start }

        val merged = mutableListOf<Break>()
        for (item in raw) {
            val last = merged.lastOrNull()
            if (last != null && !item.start.isAfter(last.end)) {
                merged[merged.lastIndex] = last.copy(
                    name = "${last.name}·${item.name}",
                    end = if (item.end.isAfter(last.end)) item.end else last.end,
                )
            } else {
                merged.add(item)
            }
        }
        return merged
    }
}
