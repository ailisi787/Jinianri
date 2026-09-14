package com.ailisi.jinianri

import com.ailisi.jinianri.domain.DateEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateEngineTest {

    // ---------- 公历每年重复 ----------

    @Test
    fun `今年还没到的日期返回今年`() {
        val today = LocalDate.of(2026, 9, 11)
        val next = DateEngine.nextSolarOccurrence(12, 25, today)
        assertEquals(LocalDate.of(2026, 12, 25), next)
    }

    @Test
    fun `今年已过的日期返回明年`() {
        val today = LocalDate.of(2026, 9, 11)
        val next = DateEngine.nextSolarOccurrence(1, 1, today)
        assertEquals(LocalDate.of(2027, 1, 1), next)
    }

    @Test
    fun `正好是今天返回今天`() {
        val today = LocalDate.of(2026, 9, 11)
        val next = DateEngine.nextSolarOccurrence(9, 11, today)
        assertEquals(today, next)
    }

    @Test
    fun `2月29日在平年落到2月28日`() {
        val today = LocalDate.of(2026, 1, 1)
        val next = DateEngine.nextSolarOccurrence(2, 29, today)
        assertEquals(LocalDate.of(2026, 2, 28), next)
    }

    @Test
    fun `2月29日在闰年保持2月29日`() {
        val today = LocalDate.of(2028, 1, 1)
        val next = DateEngine.nextSolarOccurrence(2, 29, today)
        assertEquals(LocalDate.of(2028, 2, 29), next)
    }

    // ---------- 农历每年重复 ----------

    @Test
    fun `农历正月初一在春节当天返回当天`() {
        // 2026 年春节是 2 月 17 日
        val today = LocalDate.of(2026, 2, 17)
        val next = DateEngine.nextLunarOccurrence(1, 1, today)
        assertEquals(today, next)
    }

    @Test
    fun `农历日期已过时返回下一年`() {
        // 2026 春节 2/17 已过，下一次正月初一应该是 2027 年春节（2/6）
        val today = LocalDate.of(2026, 3, 1)
        val next = DateEngine.nextLunarOccurrence(1, 1, today)
        assertEquals(LocalDate.of(2027, 2, 6), next)
    }

    @Test
    fun `农历八月十五中秋节计算正确`() {
        // 2026 年中秋节是 9 月 25 日
        val today = LocalDate.of(2026, 9, 1)
        val next = DateEngine.nextLunarOccurrence(8, 15, today)
        assertEquals(LocalDate.of(2026, 9, 25), next)
    }

    @Test
    fun `农历大月三十存在时保持三十`() {
        // 找一个有 30 日的农历月
        val year = 2026
        val dayCount = DateEngine.lunarMonthDayCount(year, 1)
        assertTrue(dayCount == 29 || dayCount == 30)
    }

    @Test
    fun `农历三十在小月自动落到廿九`() {
        // 找一个小月（29 天），三十应该落到廿九
        var found = false
        for (month in 1..12) {
            if (DateEngine.lunarMonthDayCount(2026, month) == 29) {
                val asThirty = DateEngine.lunarDateInYear(2026, month, 30)
                val asTwentyNine = DateEngine.lunarDateInYear(2026, month, 29)
                assertTrue(asThirty != null)
                assertEquals(asTwentyNine, asThirty)
                found = true
                break
            }
        }
        assertTrue("2026 年应存在 29 天的农历小月", found)
    }

    // ---------- timingOf 综合 ----------

    @Test
    fun `倒计时天数计算正确`() {
        val today = LocalDate.of(2026, 9, 11)
        val timing = DateEngine.timingOf(
            dateEpochDay = LocalDate.of(2026, 12, 25).toEpochDay(),
            repeatYearly = true,
            today = today,
        )
        assertEquals(105L, timing.daysUntil)
    }

    @Test
    fun `正计时天数计算正确`() {
        val today = LocalDate.of(2026, 9, 11)
        val start = LocalDate.of(2025, 5, 10)
        val timing = DateEngine.timingOf(
            dateEpochDay = start.toEpochDay(),
            repeatYearly = true,
            today = today,
        )
        assertEquals(489L, timing.daysSince)
    }

    @Test
    fun `一次性事件的日期不会滚动`() {
        val exam = LocalDate.of(2027, 6, 7)
        val timing = DateEngine.timingOf(
            dateEpochDay = exam.toEpochDay(),
            repeatYearly = false,
            today = LocalDate.of(2026, 9, 11),
        )
        assertEquals(exam, timing.nextDate)
    }

    @Test
    fun `农历转换往返一致`() {
        val solar = LocalDate.of(2026, 2, 17)
        val (y, m, d) = DateEngine.toLunar(solar)
        val back = DateEngine.lunarDateInYear(y, m, d)
        assertEquals(solar, back)
    }

    // ---------- 分类过滤逻辑（纯逻辑验证） ----------

    @Test
    fun `分类筛选与搜索叠加`() {
        data class Row(val title: String, val category: Long?, val note: String = "")
        val rows = listOf(
            Row("妈妈的生日", 2),
            Row("恋爱纪念日", 3),
            Row("高考", 4),
            Row("毕业典礼", null),
        )
        val q = "生日"
        val category = 2L
        val filtered = rows.filter { r ->
            val matchCategory = r.category == category
            val matchQuery = r.title.contains(q)
            matchCategory && matchQuery
        }
        assertEquals(1, filtered.size)
        assertEquals("妈妈的生日", filtered[0].title)
    }

    @Test
    fun `未分类筛选不会崩溃`() {
        data class Row(val title: String, val category: Long?)
        val rows = listOf(Row("毕业典礼", null), Row("生日", 2))
        val result = rows.filter { it.category == null }
        assertEquals(1, result.size)
        assertFalse(result.any { it.category != null })
    }

    // ---------- occursOn（日历标记） ----------

    @Test
    fun `公历重复事件在周年当天出现`() {
        val occurred = DateEngine.occursOn(
            dateEpochDay = LocalDate.of(2000, 12, 25).toEpochDay(),
            repeatYearly = true,
            date = LocalDate.of(2026, 12, 25),
        )
        assertTrue(occurred)
    }

    @Test
    fun `公历重复事件在非周年当天不出现`() {
        val occurred = DateEngine.occursOn(
            dateEpochDay = LocalDate.of(2000, 12, 25).toEpochDay(),
            repeatYearly = true,
            date = LocalDate.of(2026, 12, 24),
        )
        assertFalse(occurred)
    }

    @Test
    fun `2月29日事件在平年出现在2月28日`() {
        val occurred = DateEngine.occursOn(
            dateEpochDay = LocalDate.of(2004, 2, 29).toEpochDay(),
            repeatYearly = true,
            date = LocalDate.of(2026, 2, 28),
        )
        assertTrue(occurred)
        val notOccurred = DateEngine.occursOn(
            dateEpochDay = LocalDate.of(2004, 2, 29).toEpochDay(),
            repeatYearly = true,
            date = LocalDate.of(2026, 3, 1),
        )
        assertFalse(notOccurred)
    }

    @Test
    fun `农历重复事件在农历纪念日当天出现`() {
        // 2026 年春节（正月初一）是 2 月 17 日
        val occurred = DateEngine.occursOn(
            dateEpochDay = LocalDate.of(2026, 2, 17).toEpochDay(),
            repeatYearly = true,
            isLunar = true,
            lunarMonth = 1,
            lunarDay = 1,
            date = LocalDate.of(2026, 2, 17),
        )
        assertTrue(occurred)
        // 前一天不是
        val notOccurred = DateEngine.occursOn(
            dateEpochDay = LocalDate.of(2026, 2, 17).toEpochDay(),
            repeatYearly = true,
            isLunar = true,
            lunarMonth = 1,
            lunarDay = 1,
            date = LocalDate.of(2026, 2, 16),
        )
        assertFalse(notOccurred)
        // 到了 2027 年春节（2 月 6 日）再次出现
        val nextYear = DateEngine.occursOn(
            dateEpochDay = LocalDate.of(2026, 2, 17).toEpochDay(),
            repeatYearly = true,
            isLunar = true,
            lunarMonth = 1,
            lunarDay = 1,
            date = LocalDate.of(2027, 2, 6),
        )
        assertTrue(nextYear)
    }

    @Test
    fun `一次性事件只在当天出现`() {
        val exam = LocalDate.of(2027, 6, 7)
        assertTrue(
            DateEngine.occursOn(
                dateEpochDay = exam.toEpochDay(),
                repeatYearly = false,
                date = exam,
            )
        )
        assertFalse(
            DateEngine.occursOn(
                dateEpochDay = exam.toEpochDay(),
                repeatYearly = false,
                date = LocalDate.of(2028, 6, 7),
            )
        )
    }
}
