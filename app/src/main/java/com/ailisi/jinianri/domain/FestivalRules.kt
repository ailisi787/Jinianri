package com.ailisi.jinianri.domain

import com.nlf.calendar.Lunar
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

/**
 * 规则型节日的日期规则。
 *
 * 「节气」「第 n 个星期几」这两类节日每年的公历日期都会变（清明可能是 4 月 4 日或 5 日，
 * 母亲节永远是 5 月的第二个星期日），没法用「月 + 日」表示，所以单独存一条规则字符串，
 * 存在 [com.ailisi.jinianri.data.EventEntity.rule] 里（空串 = 普通事件）：
 *
 * - `term:清明`    二十四节气，按当年实际交节日
 * - `nth:5,SUN,2`  5 月的第 2 个星期日（母亲节 / 父亲节 / 感恩节）
 */
object FestivalRules {

    private const val TERM_PREFIX = "term:"
    private const val NTH_PREFIX = "nth:"

    /** 二十四节气（按公历年内先后顺序） */
    val SOLAR_TERMS: List<String> = listOf(
        "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
        "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
        "小暑", "大暑", "立秋", "处暑", "白露", "秋分",
        "寒露", "霜降", "立冬", "小雪", "大雪", "冬至",
    )

    /** 节气规则，如 `term:清明` */
    fun termRule(term: String): String = TERM_PREFIX + term

    /** 第 [nth] 个 [dayOfWeek] 的规则，如 5 月的第 2 个星期日 = `nth:5,SUN,2` */
    fun nthWeekdayRule(month: Int, dayOfWeek: DayOfWeek, nth: Int): String =
        "$NTH_PREFIX$month,${dayOfWeek.name.take(3)},$nth"

    /** [rule] 在 [year]（公历年）的日期；规则为空、无法解析或该年没有时返回 null */
    fun occurrenceInYear(rule: String, year: Int): LocalDate? = when {
        rule.startsWith(TERM_PREFIX) -> termTable(year)[rule.removePrefix(TERM_PREFIX)]
        rule.startsWith(NTH_PREFIX) -> nthWeekdayDate(rule.removePrefix(NTH_PREFIX), year)
        else -> null
    }

    /** [rule] 从 [today]（含当天）起的下一次发生日 */
    fun nextOccurrence(rule: String, today: LocalDate): LocalDate? {
        occurrenceInYear(rule, today.year)?.let { if (!it.isBefore(today)) return it }
        return occurrenceInYear(rule, today.year + 1)
    }

    /** 规则的中文说明，如「二十四节气」「5 月的第 2 个星期日」 */
    fun describe(rule: String): String = when {
        rule.startsWith(TERM_PREFIX) -> "二十四节气"
        rule.startsWith(NTH_PREFIX) -> {
            val spec = nthSpec(rule.removePrefix(NTH_PREFIX))
            if (spec == null) "" else "${spec.first} 月的第 ${spec.third} 个${weekdayName(spec.second)}"
        }
        else -> ""
    }

    private fun weekdayName(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
        DayOfWeek.MONDAY -> "星期一"
        DayOfWeek.TUESDAY -> "星期二"
        DayOfWeek.WEDNESDAY -> "星期三"
        DayOfWeek.THURSDAY -> "星期四"
        DayOfWeek.FRIDAY -> "星期五"
        DayOfWeek.SATURDAY -> "星期六"
        DayOfWeek.SUNDAY -> "星期日"
    }

    /**
     * lunar-java 节气表里跨年节气的英文别名。
     * 表覆盖前后约 15 个月（从上一年的 DA_XUE 到下一年的 JING_ZHE），
     * 边界这 7 个为避免与当年同名节气冲突而改用大写别名。
     */
    private val TERM_ALIAS: Map<String, String> = mapOf(
        "DA_XUE" to "大雪", "DONG_ZHI" to "冬至", "XIAO_HAN" to "小寒", "DA_HAN" to "大寒",
        "LI_CHUN" to "立春", "YU_SHUI" to "雨水", "JING_ZHE" to "惊蛰",
    )

    /** 节气表按年缓存：日历与列表会反复查同一年，而算一次节气表并不便宜 */
    private val termTables = ConcurrentHashMap<Int, Map<String, LocalDate>>()

    /**
     * 某公历年的节气表。
     * 取该农历年六月初一的节气表（覆盖前后 15 个月），按公历年过滤后正好剩这一年的 24 个节气。
     */
    private fun termTable(year: Int): Map<String, LocalDate> = termTables.getOrPut(year) {
        buildMap {
            Lunar.fromYmd(year, 6, 1).jieQiTable.forEach { (key, solar) ->
                if (solar.year == year) {
                    put(TERM_ALIAS[key] ?: key, LocalDate.of(solar.year, solar.month, solar.day))
                }
            }
        }
    }

    private val WEEKDAYS: Map<String, DayOfWeek> = mapOf(
        "MON" to DayOfWeek.MONDAY, "TUE" to DayOfWeek.TUESDAY, "WED" to DayOfWeek.WEDNESDAY,
        "THU" to DayOfWeek.THURSDAY, "FRI" to DayOfWeek.FRIDAY, "SAT" to DayOfWeek.SATURDAY,
        "SUN" to DayOfWeek.SUNDAY,
    )

    /** 解析 `月,星期,序号`（如 `5,SUN,2`） */
    private fun nthSpec(spec: String): Triple<Int, DayOfWeek, Int>? {
        val parts = spec.split(",")
        if (parts.size != 3) return null
        val month = parts[0].trim().toIntOrNull() ?: return null
        val weekday = WEEKDAYS[parts[1].trim().uppercase()] ?: return null
        val nth = parts[2].trim().toIntOrNull() ?: return null
        if (month !in 1..12 || nth !in 1..5) return null
        return Triple(month, weekday, nth)
    }

    /** 「某月第 n 个星期几」在该年的日期 */
    private fun nthWeekdayDate(spec: String, year: Int): LocalDate? {
        val (month, weekday, nth) = nthSpec(spec) ?: return null
        val first = LocalDate.of(year, month, 1)
        val offset = (weekday.value - first.dayOfWeek.value + 7) % 7
        return first.plusDays((offset + 7 * (nth - 1)).toLong())
    }
}
