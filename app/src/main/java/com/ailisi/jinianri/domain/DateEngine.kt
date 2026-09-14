package com.ailisi.jinianri.domain

import com.nlf.calendar.Lunar
import com.nlf.calendar.LunarMonth
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * 日期引擎：计算纪念日的下一次发生日、正/倒计时天数。
 *
 * 规则：
 * - 公历每年重复：取本年同月同日；若已过，取次年。2 月 29 日在平年按 2 月 28 日。
 * - 农历每年重复：取农历同月同日；闰月日期在无闰月的年份落到普通月；
 *   小月无 30 日时落到该月最后一天。
 */
object DateEngine {

    /** 事件的展示信息 */
    data class EventTiming(
        /** 下一次发生日（含今天） */
        val nextDate: LocalDate,
        /** 距离下一次发生的天数（0 = 就是今天） */
        val daysUntil: Long,
        /** 从起始日期到今天的天数（正计时用） */
        val daysSince: Long,
    )

    /**
     * 计算事件的时间信息。
     *
     * @param dateEpochDay 公历起始日（农历事件为对应公历日）
     * @param repeatYearly 每年重复
     * @param isLunar 是否农历
     * @param lunarMonth 农历月（闰月为负）
     * @param lunarDay 农历日
     */
    fun timingOf(
        dateEpochDay: Long,
        repeatYearly: Boolean,
        isLunar: Boolean = false,
        lunarMonth: Int = 0,
        lunarDay: Int = 0,
        today: LocalDate = LocalDate.now(),
    ): EventTiming {
        val start = LocalDate.ofEpochDay(dateEpochDay)
        val next = if (repeatYearly) {
            if (isLunar) {
                nextLunarOccurrence(lunarMonth, lunarDay, today)
            } else {
                nextSolarOccurrence(start.monthValue, start.dayOfMonth, today)
            }
        } else {
            start
        }
        return EventTiming(
            nextDate = next,
            daysUntil = ChronoUnit.DAYS.between(today, next),
            daysSince = ChronoUnit.DAYS.between(start, today),
        )
    }

    /** 公历每年重复的下一次发生日（含今天） */
    fun nextSolarOccurrence(month: Int, day: Int, today: LocalDate): LocalDate {
        val thisYear = safeSolarDate(today.year, month, day)
        if (!thisYear.isBefore(today)) return thisYear
        return safeSolarDate(today.year + 1, month, day)
    }

    /**
     * 事件是否发生于 [date] 这一天（用于日历标记）。
     * - 一次性事件：精确匹配起始日
     * - 每年重复（公历）：月日匹配；2 月 29 日在平年落到 2 月 28 日
     * - 每年重复（农历）：农历月日匹配；闰月不存在的年份落到普通月；
     *   小月无三十时落到该月最后一天
     */
    fun occursOn(
        dateEpochDay: Long,
        repeatYearly: Boolean,
        isLunar: Boolean = false,
        lunarMonth: Int = 0,
        lunarDay: Int = 0,
        date: LocalDate,
    ): Boolean {
        val start = LocalDate.ofEpochDay(dateEpochDay)
        if (!repeatYearly) return start == date
        if (!isLunar) {
            if (start.monthValue == date.monthValue && start.dayOfMonth == date.dayOfMonth) return true
            // 2 月 29 日在平年按 2 月 28 日
            if (start.monthValue == 2 && start.dayOfMonth == 29 &&
                date.monthValue == 2 && date.dayOfMonth == 28 &&
                !java.time.Year.isLeap(date.year.toLong())
            ) return true
            return false
        }
        // 农历每年重复
        val (ly, lm, ld) = toLunar(date)
        if (lm == lunarMonth) {
            val maxDay = lunarMonthDayCount(ly, lunarMonth)
            if (ld == lunarDay.coerceAtMost(maxDay)) return true
        } else if (lunarMonth < 0 && lm == -lunarMonth) {
            // 该年没有这个闰月（或该天为普通月）：回退到普通月
            val maxDay = lunarMonthDayCount(ly, lm)
            if (ld == lunarDay.coerceAtMost(maxDay)) return true
        }
        return false
    }

    /** 2 月 29 日在平年落到 2 月 28 日 */
    private fun safeSolarDate(year: Int, month: Int, day: Int): LocalDate {
        val maxDay = YearMonth.of(year, month).lengthOfMonth()
        return LocalDate.of(year, month, day.coerceAtMost(maxDay))
    }

    /** 农历每年重复的下一次发生日（含今天） */
    fun nextLunarOccurrence(lunarMonth: Int, lunarDay: Int, today: LocalDate): LocalDate {
        val todayLunarYear = Lunar.fromDate(today.toDate()).year
        // 最多往后找 5 年（闰月等边界足够）
        for (year in todayLunarYear..todayLunarYear + 5) {
            val date = lunarDateInYear(year, lunarMonth, lunarDay) ?: continue
            if (!date.isBefore(today)) return date
        }
        return lunarDateInYear(todayLunarYear + 1, lunarMonth, lunarDay) ?: today
    }

    /**
     * 指定农历年里的农历日期对应的公历日。
     * - 闰月不存在时用普通月代替
     * - 该月没有 30 日时用最后一天代替
     * - 返回 null 表示该月不存在（异常情况）
     */
    fun lunarDateInYear(lunarYear: Int, lunarMonth: Int, lunarDay: Int): LocalDate? {
        val month = resolveLunarMonth(lunarYear, lunarMonth) ?: return null
        val day = lunarDay.coerceIn(1, month.dayCount)
        return try {
            Lunar.fromYmd(lunarYear, month.month, day).solar.toLocalDate()
        } catch (e: Exception) {
            null
        }
    }

    /** 找该农历年可用的月份：优先精确月（含闰月），闰月不存在则退回普通月 */
    private fun resolveLunarMonth(lunarYear: Int, lunarMonth: Int): LunarMonth? {
        LunarMonth.fromYm(lunarYear, lunarMonth)?.let { return it }
        // 闰月不存在：退回同数字的普通月
        if (lunarMonth < 0) return LunarMonth.fromYm(lunarYear, -lunarMonth)
        return null
    }

    /** 农历日的中文描述，如「正月初一」「闰六月初五」 */
    fun lunarText(lunarYear: Int, lunarMonth: Int, lunarDay: Int): String {
        val day = lunarDay.coerceAtLeast(1)
        return try {
            val lunar = Lunar.fromYmd(lunarYear, lunarMonth, day)
            lunar.monthInChinese + lunar.dayInChinese
        } catch (e: Exception) {
            ""
        }
    }

    /** 某个公历日对应的农历描述，如「二〇二六年八月初一」 */
    fun lunarTextOf(date: LocalDate): String =
        Lunar.fromDate(date.toDate()).toString()

    /** 农历日期（月/日）的展示文本，不依赖具体年份 */
    fun lunarMonthDayText(lunarMonth: Int, lunarDay: Int, refYear: Int): String {
        val monthName = resolveLunarMonth(refYear, lunarMonth)?.let { m ->
            val leap = if (m.isLeap) "闰" else ""
            leap + chineseMonthName(m.month)
        } ?: chineseMonthName(if (lunarMonth < 0) -lunarMonth else lunarMonth)
        return monthName + chineseDayName(lunarDay)
    }

    private fun chineseMonthName(month: Int): String {
        val names = listOf(
            "正月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "冬月", "腊月"
        )
        val m = when {
            month < 0 -> -month
            month == 0 -> 1
            else -> month
        }
        return names.getOrElse(m - 1) { "${m}月" }
    }

    private fun chineseDayName(day: Int): String {
        val names = listOf(
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
        )
        return names.getOrElse(day - 1) { "$day" }
    }

    /** 生肖，如「马」 */
    fun zodiacOf(date: LocalDate): String = Lunar.fromDate(date.toDate()).yearShengXiao

    /** 干支年，如「丙午」 */
    fun ganZhiOf(date: LocalDate): String = Lunar.fromDate(date.toDate()).yearInGanZhi

    /** 公历 -> 农历：返回 (农历年, 农历月(闰为负), 农历日) */
    fun toLunar(date: LocalDate): Triple<Int, Int, Int> {
        val lunar = Lunar.fromDate(date.toDate())
        return Triple(lunar.year, lunar.month, lunar.day)
    }

    /** 农历月的可用天数（用于选择器） */
    fun lunarMonthDayCount(lunarYear: Int, lunarMonth: Int): Int =
        resolveLunarMonth(lunarYear, lunarMonth)?.dayCount ?: 30

    /** 某农历年所有月份（含闰月，闰月为负），用于选择器 */
    fun lunarMonthsOf(lunarYear: Int): List<Int> {
        val months = mutableListOf<Int>()
        for (m in 1..12) {
            LunarMonth.fromYm(lunarYear, m)?.let { months.add(it.month) }
        }
        // 闰月
        for (m in 1..12) {
            LunarMonth.fromYm(lunarYear, -m)?.let { months.add(it.month) }
        }
        return months.sortedBy { if (it < 0) -it + 0.5 else it.toDouble() }
    }

    /** 今天的公历日 */
    fun today(): LocalDate = LocalDate.now()

    // java.util.Date 转换
    private fun LocalDate.toDate(): java.util.Date =
        java.util.Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())

    private fun com.nlf.calendar.Solar.toLocalDate(): LocalDate =
        LocalDate.of(year, month, day)
}
