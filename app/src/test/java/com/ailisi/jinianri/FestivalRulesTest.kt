package com.ailisi.jinianri

import com.ailisi.jinianri.domain.DateEngine
import com.ailisi.jinianri.domain.FestivalGroups
import com.ailisi.jinianri.domain.FestivalRules
import com.ailisi.jinianri.domain.HolidayCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/** 规则型节日（节气 / 第 n 个星期几）与放假推算的边界测试 */
class FestivalRulesTest {

    // ---------- 二十四节气 ----------

    @Test
    fun `节气取当年实际交节日`() {
        val qingming = FestivalRules.termRule("清明")
        assertEquals(LocalDate.of(2026, 4, 5), FestivalRules.occurrenceInYear(qingming, 2026))
        assertEquals(LocalDate.of(2026, 12, 22), FestivalRules.occurrenceInYear(FestivalRules.termRule("冬至"), 2026))
        assertEquals(LocalDate.of(2026, 2, 4), FestivalRules.occurrenceInYear(FestivalRules.termRule("立春"), 2026))
        assertEquals(LocalDate.of(2026, 1, 5), FestivalRules.occurrenceInYear(FestivalRules.termRule("小寒"), 2026))
    }

    @Test
    fun `一年正好 24 个节气且都能取到日期`() {
        val dates = FestivalRules.SOLAR_TERMS.map { term ->
            FestivalRules.occurrenceInYear(FestivalRules.termRule(term), 2026)
        }
        assertEquals(24, dates.size)
        assertTrue(dates.none { it == null })
        // 按时间先后排列，且都在 2026 年内
        val days = dates.filterNotNull().map { it.toEpochDay() }
        assertEquals(days.sorted(), days)
        assertTrue(dates.filterNotNull().all { it.year == 2026 })
    }

    @Test
    fun `节气跨年时滚动到次年`() {
        val rule = FestivalRules.termRule("清明")
        // 当天算下一次（含今天）
        assertEquals(LocalDate.of(2026, 4, 5), FestivalRules.nextOccurrence(rule, LocalDate.of(2026, 4, 5)))
        // 过了清明就滚到明年
        val next = FestivalRules.nextOccurrence(rule, LocalDate.of(2026, 4, 6))
        assertNotNull(next)
        assertEquals(2027, next!!.year)
        assertTrue(next.monthValue == 4 && next.dayOfMonth in 4..6)
    }

    // ---------- 第 n 个星期几 ----------

    @Test
    fun `母亲节是五月第二个星期日`() {
        val rule = FestivalRules.nthWeekdayRule(5, DayOfWeek.SUNDAY, 2)
        assertEquals(LocalDate.of(2026, 5, 10), FestivalRules.occurrenceInYear(rule, 2026))
        assertEquals(LocalDate.of(2025, 5, 11), FestivalRules.occurrenceInYear(rule, 2025))
    }

    @Test
    fun `父亲节与感恩节`() {
        assertEquals(
            LocalDate.of(2026, 6, 21),
            FestivalRules.occurrenceInYear(FestivalRules.nthWeekdayRule(6, DayOfWeek.SUNDAY, 3), 2026),
        )
        assertEquals(
            LocalDate.of(2026, 11, 26),
            FestivalRules.occurrenceInYear(FestivalRules.nthWeekdayRule(11, DayOfWeek.THURSDAY, 4), 2026),
        )
    }

    @Test
    fun `坏规则返回空而不是抛异常`() {
        assertNull(FestivalRules.occurrenceInYear("", 2026))
        assertNull(FestivalRules.occurrenceInYear("term:不存在的节气", 2026))
        assertNull(FestivalRules.occurrenceInYear("nth:5,XXX,2", 2026))
        assertNull(FestivalRules.occurrenceInYear("nth:13,SUN,2", 2026))
        assertNull(FestivalRules.nextOccurrence("乱写的规则", LocalDate.of(2026, 1, 1)))
    }

    // ---------- DateEngine 接入规则 ----------

    @Test
    fun `规则型事件按规则滚动而不是按存下来的日期`() {
        // 存的是 2026 年清明，但今天是 2026-04-06，下一次应当滚到 2027 年
        val timing = DateEngine.timingOf(
            dateEpochDay = LocalDate.of(2026, 4, 5).toEpochDay(),
            repeatYearly = true,
            rule = FestivalRules.termRule("清明"),
            today = LocalDate.of(2026, 4, 6),
        )
        assertEquals(2027, timing.nextDate.year)
        assertTrue(timing.daysUntil > 300)
    }

    @Test
    fun `规则型事件在当天算今天`() {
        val timing = DateEngine.timingOf(
            dateEpochDay = LocalDate.of(2026, 5, 10).toEpochDay(),
            repeatYearly = true,
            rule = FestivalRules.nthWeekdayRule(5, DayOfWeek.SUNDAY, 2),
            today = LocalDate.of(2026, 5, 10),
        )
        assertEquals(0L, timing.daysUntil)
        assertEquals(LocalDate.of(2026, 5, 10), timing.nextDate)
    }

    @Test
    fun `规则型事件只在规则算出的那天出现在日历上`() {
        val rule = FestivalRules.termRule("清明")
        val epoch = LocalDate.of(2026, 4, 5).toEpochDay()
        assertTrue(DateEngine.occursOn(epoch, true, rule = rule, date = LocalDate.of(2026, 4, 5)))
        assertFalse(DateEngine.occursOn(epoch, true, rule = rule, date = LocalDate.of(2026, 4, 4)))

        // 次年清明若不是 4 月 5 日，就不能还按存下来的 4 月 5 日标记
        val qingming2027 = FestivalRules.occurrenceInYear(rule, 2027)!!
        assertTrue(DateEngine.occursOn(epoch, true, rule = rule, date = qingming2027))
        if (qingming2027 != LocalDate.of(2027, 4, 5)) {
            assertFalse(DateEngine.occursOn(epoch, true, rule = rule, date = LocalDate.of(2027, 4, 5)))
        }
    }

    // ---------- 放假推算 ----------

    @Test
    fun `2026 年放假安排`() {
        val breaks = HolidayCalendar.breaksInYear(2026)
        assertEquals(7, breaks.size)

        val chunjie = breaks.first { it.name == "春节" }
        assertEquals(LocalDate.of(2026, 2, 16), chunjie.start) // 除夕
        assertEquals(LocalDate.of(2026, 2, 23), chunjie.end)
        assertEquals(8, chunjie.days)

        assertEquals(5, breaks.first { it.name == "劳动节" }.days)
        assertEquals(7, breaks.first { it.name == "国庆节" }.days)
        assertEquals(LocalDate.of(2026, 4, 5), breaks.first { it.name == "清明节" }.start)
        assertEquals(LocalDate.of(2026, 6, 19), breaks.first { it.name == "端午节" }.start)
        assertEquals(LocalDate.of(2026, 9, 25), breaks.first { it.name == "中秋节" }.start)
    }

    @Test
    fun `查某天是不是放假`() {
        val may3 = HolidayCalendar.breakOn(LocalDate.of(2026, 5, 3))
        assertEquals("劳动节", may3?.name)
        assertEquals(5, may3?.days)

        assertNull(HolidayCalendar.breakOn(LocalDate.of(2026, 5, 6)))
        assertNull(HolidayCalendar.breakOn(LocalDate.of(2026, 3, 1)))
        assertEquals("国庆节", HolidayCalendar.breakOn(LocalDate.of(2026, 10, 7))?.name)
    }

    @Test
    fun `接下来的假期按时间排序且跳过已结束的`() {
        val upcoming = HolidayCalendar.upcoming(LocalDate.of(2026, 5, 6), count = 3)
        assertEquals(listOf("端午节", "中秋节", "国庆节"), upcoming.map { it.name })
    }

    @Test
    fun `年末能跨年取到元旦`() {
        val upcoming = HolidayCalendar.upcoming(LocalDate.of(2026, 12, 20), count = 1)
        assertEquals("元旦", upcoming.first().name)
        assertEquals(2027, upcoming.first().start.year)
    }

    @Test
    fun `春节假期跟着除夕走`() {
        // 2027 年春节是 2 月 6 日，除夕 2 月 5 日
        val chunjie = HolidayCalendar.breaksInYear(2027).first { it.name == "春节" }
        assertEquals(LocalDate.of(2027, 2, 5), chunjie.start)
        assertEquals(8, chunjie.days)
    }

    // ---------- 节日目录 ----------

    @Test
    fun `目录里每个节日都能算出日期`() {
        val all = FestivalGroups.flatMap { it.festivals }
        assertTrue("节日数量应当足够覆盖常见节日", all.size >= 50)
        all.forEach { festival ->
            assertNotNull("${festival.name} 没有算出日期", festival.dateInYear(2026))
        }
        assertEquals("节日名不应重复", all.size, all.map { it.name }.distinct().size)
    }

    @Test
    fun `除夕按腊月最后一天算`() {
        val chuxi = FestivalGroups.flatMap { it.festivals }.first { it.name == "除夕" }
        // 2026 农历年腊月只有 29 天，除夕落在腊月廿九（2027-02-05）
        assertEquals(LocalDate.of(2027, 2, 5), chuxi.dateInYear(2026))
    }

    @Test
    fun `导入的事件带规则且能回认`() {
        val qingming = FestivalGroups.flatMap { it.festivals }.first { it.name == "清明节" }
        val date = qingming.nextDate(LocalDate.of(2026, 1, 1))!!
        val event = qingming.toEvent(date)

        assertEquals(FestivalRules.termRule("清明"), event.rule)
        assertEquals(date.toEpochDay(), event.dateEpochDay)
        assertTrue(event.repeatYearly)
        assertTrue(qingming.matches(event))
        // 改了名字就不该再算「已导入」
        assertFalse(qingming.matches(event.copy(title = "我的清明")))
    }

    @Test
    fun `下次发生日优先取今年`() {
        val nationalDay = FestivalGroups.flatMap { it.festivals }.first { it.name == "国庆节" }
        assertEquals(LocalDate.of(2026, 10, 1), nationalDay.nextDate(LocalDate.of(2026, 9, 11)))
        assertEquals(LocalDate.of(2027, 10, 1), nationalDay.nextDate(LocalDate.of(2026, 10, 2)))
    }
}
