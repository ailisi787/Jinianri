package com.ailisi.jinianri.domain

import com.ailisi.jinianri.data.EventEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * 一个可导入的节日。
 *
 * 只描述「日期怎么算」，导入时按当天算出下一次发生日、生成一条纪念日事件（归入「节日」分类），
 * 之后每年由 [DateEngine] 按同一套规则滚动。
 */
data class Festival(
    val name: String,
    /** Fluent Emoji 图标 key（见 EventIcons） */
    val icon: String,
    /** 公历固定日期（月, 日） */
    val solar: Pair<Int, Int>? = null,
    /** 农历固定日期（月, 日） */
    val lunar: Pair<Int, Int>? = null,
    /** 规则型日期（节气 / 第 n 个星期几），见 [FestivalRules] */
    val rule: String = "",
    /** 日期文案覆盖：默认按公历/农历生成，特殊日子（如除夕）可用它说清楚 */
    val label: String = "",
) {
    /** 该节日在 [year] 年的公历日期（农历按同序号的农历年算） */
    fun dateInYear(year: Int): LocalDate? = when {
        rule.isNotEmpty() -> FestivalRules.occurrenceInYear(rule, year)
        lunar != null -> DateEngine.lunarDateInYear(year, lunar.first, lunar.second)
        solar != null -> {
            val maxDay = YearMonth.of(year, solar.first).lengthOfMonth()
            LocalDate.of(year, solar.first, solar.second.coerceAtMost(maxDay))
        }
        else -> null
    }

    /** 从 [today] 起的下一次发生日（今年已过则取明年） */
    fun nextDate(today: LocalDate): LocalDate? =
        dateInYear(today.year)?.takeIf { !it.isBefore(today) } ?: dateInYear(today.year + 1)

    /** 日期文案，如「公历 5月1日」「农历五月初五」「二十四节气」 */
    fun dateText(year: Int): String = when {
        label.isNotEmpty() -> label
        rule.isNotEmpty() -> FestivalRules.describe(rule)
        lunar != null -> "农历 " + DateEngine.lunarMonthDayText(lunar.first, lunar.second, year)
        solar != null -> "公历 ${solar.first}月${solar.second}日"
        else -> ""
    }

    /** 生成导入用的事件；[date] 为已算好的下一次发生日 */
    fun toEvent(date: LocalDate): EventEntity = EventEntity(
        title = name,
        dateEpochDay = date.toEpochDay(),
        isLunar = lunar != null,
        lunarMonth = lunar?.first ?: 0,
        lunarDay = lunar?.second ?: 0,
        rule = rule,
        repeatYearly = true,
        iconKey = icon,
    )

    /** 既有事件是否就是这个节日（用于显示「已导入」，避免重复导入） */
    fun matches(event: EventEntity): Boolean =
        event.title == name &&
            event.rule == rule &&
            event.isLunar == (lunar != null) &&
            event.lunarMonth == (lunar?.first ?: 0) &&
            event.lunarDay == (lunar?.second ?: 0)
}

/** 导入页里的一个分组 */
data class FestivalGroup(
    val name: String,
    val note: String,
    val festivals: List<Festival>,
)

/** 内置节日目录：分组展示，可整组勾选 */
val FestivalGroups: List<FestivalGroup> = listOf(
    FestivalGroup(
        "法定节假日",
        "放假安排的 7 个节日，导入后日历会标出连休",
        listOf(
            Festival("元旦", "fireworks", solar = 1 to 1),
            Festival("春节", "firecracker", lunar = 1 to 1),
            Festival("清明节", "kite", rule = FestivalRules.termRule("清明")),
            Festival("劳动节", "briefcase", solar = 5 to 1),
            Festival("端午节", "bento", lunar = 5 to 5),
            Festival("中秋节", "mooncake", lunar = 8 to 15),
            Festival("国庆节", "flag", solar = 10 to 1),
        ),
    ),
    FestivalGroup(
        "中国传统节日",
        "农历与节气里的传统日子",
        listOf(
            Festival("除夕", "dumpling", lunar = 12 to 30, label = "农历腊月最后一天"),
            Festival("元宵节", "moon", lunar = 1 to 15),
            Festival("龙抬头", "sun", lunar = 2 to 2),
            Festival("七夕节", "two_hearts", lunar = 7 to 7),
            Festival("中元节", "sparkler", lunar = 7 to 15),
            Festival("重阳节", "crown", lunar = 9 to 9),
            Festival("腊八节", "curry", lunar = 12 to 8),
            Festival("小年", "fire", lunar = 12 to 23),
        ),
    ),
    FestivalGroup(
        "公历节日",
        "每年固定公历日期的节日",
        listOf(
            Festival("情人节", "heart", solar = 2 to 14),
            Festival("妇女节", "star_glow", solar = 3 to 8),
            Festival("植树节", "rainbow", solar = 3 to 12),
            Festival("消费者权益日", "receipt", solar = 3 to 15),
            Festival("愚人节", "party", solar = 4 to 1),
            Festival("青年节", "rocket", solar = 5 to 4),
            Festival("儿童节", "balloon", solar = 6 to 1),
            Festival("建党节", "flag", solar = 7 to 1),
            Festival("建军节", "trophy", solar = 8 to 1),
            Festival("教师节", "book", solar = 9 to 10),
            Festival("万圣节", "halloween", solar = 10 to 31),
            Festival("光棍节", "shopping", solar = 11 to 11),
            Festival("平安夜", "star", solar = 12 to 24),
            Festival("圣诞节", "christmas", solar = 12 to 25),
        ),
    ),
    FestivalGroup(
        "国际 / 西方节日",
        "按「第几个星期几」计算，每年公历日期都不同",
        listOf(
            Festival("母亲节", "heart_sparkle", rule = FestivalRules.nthWeekdayRule(5, DayOfWeek.SUNDAY, 2)),
            Festival("父亲节", "handshake", rule = FestivalRules.nthWeekdayRule(6, DayOfWeek.SUNDAY, 3)),
            Festival("感恩节", "hotpot", rule = FestivalRules.nthWeekdayRule(11, DayOfWeek.THURSDAY, 4)),
        ),
    ),
    FestivalGroup(
        "二十四节气",
        "按太阳黄经计算，每年公历日期略有浮动",
        FestivalRules.SOLAR_TERMS.map { term ->
            Festival(term, termIcon(term), rule = FestivalRules.termRule(term))
        },
    ),
)

/** 节日总数（「我的」页展示用） */
val festivalCount: Int = FestivalGroups.sumOf { it.festivals.size }

/** 节气图标按季节取：春放风筝、夏晒太阳、秋赏月、冬数星星 */
private fun termIcon(term: String): String = when (term) {
    "立春", "雨水", "惊蛰", "春分", "清明", "谷雨" -> "kite"
    "立夏", "小满", "芒种", "夏至", "小暑", "大暑" -> "sun"
    "立秋", "处暑", "白露", "秋分", "寒露", "霜降" -> "moon"
    else -> "star"
}
