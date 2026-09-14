package com.ailisi.jinianri.ui.components

import androidx.annotation.DrawableRes
import com.ailisi.jinianri.R

/**
 * 事件图标（Microsoft Fluent Emoji，MIT 许可，见 res/raw/fluent_emoji_license.txt）。
 * 每个事件可任选一个图标，key 存在 EventEntity.iconKey。
 */
data class EventIcon(
    val key: String,
    val label: String,
    @DrawableRes val resId: Int,
)

/** 图标分组：组名 -> 图标列表（编辑器选择器按组展示） */
val EventIconGroups: List<Pair<String, List<EventIcon>>> = listOf(
    "学习 / 考试" to listOf(
        EventIcon("exam", "考试", R.drawable.ic_fe_exam),
        EventIcon("book", "书本", R.drawable.ic_fe_book),
        EventIcon("book_open", "阅读", R.drawable.ic_fe_book_open),
        EventIcon("notebook", "笔记本", R.drawable.ic_fe_notebook),
        EventIcon("memo", "备忘", R.drawable.ic_fe_memo),
        EventIcon("pencil", "书写", R.drawable.ic_fe_pencil),
        EventIcon("ruler", "尺子", R.drawable.ic_fe_ruler),
        EventIcon("abacus", "数学", R.drawable.ic_fe_abacus),
        EventIcon("microscope", "科研", R.drawable.ic_fe_microscope),
        EventIcon("laptop", "电脑", R.drawable.ic_fe_laptop),
        EventIcon("stopwatch", "计时", R.drawable.ic_fe_stopwatch),
        EventIcon("alarm", "提醒", R.drawable.ic_fe_alarm),
    ),
    "工作" to listOf(
        EventIcon("briefcase", "工作", R.drawable.ic_fe_briefcase),
        EventIcon("office", "公司", R.drawable.ic_fe_office),
        EventIcon("chart_up", "业绩", R.drawable.ic_fe_chart_up),
        EventIcon("calendar", "日程", R.drawable.ic_fe_calendar),
        EventIcon("pushpin", "待办", R.drawable.ic_fe_pushpin),
        EventIcon("handshake", "合作", R.drawable.ic_fe_handshake),
        EventIcon("megaphone", "通知", R.drawable.ic_fe_megaphone),
    ),
    "生活" to listOf(
        EventIcon("house", "家庭", R.drawable.ic_fe_house),
        EventIcon("bed", "睡眠", R.drawable.ic_fe_bed),
        EventIcon("coffee", "咖啡", R.drawable.ic_fe_coffee),
        EventIcon("shopping", "购物", R.drawable.ic_fe_shopping),
        EventIcon("receipt", "账单", R.drawable.ic_fe_receipt),
        EventIcon("creditcard", "记账", R.drawable.ic_fe_creditcard),
        EventIcon("money", "存钱", R.drawable.ic_fe_money),
        EventIcon("gift", "礼物", R.drawable.ic_fe_gift),
        EventIcon("phone", "通讯", R.drawable.ic_fe_phone),
        EventIcon("key", "钥匙", R.drawable.ic_fe_key),
        EventIcon("lightbulb", "灵感", R.drawable.ic_fe_lightbulb),
        EventIcon("lotion", "护肤", R.drawable.ic_fe_lotion),
        EventIcon("toothbrush", "洗漱", R.drawable.ic_fe_toothbrush),
        EventIcon("tshirt", "衣物", R.drawable.ic_fe_tshirt),
    ),
    "节日 / 庆祝" to listOf(
        EventIcon("birthday", "生日", R.drawable.ic_fe_birthday),
        EventIcon("party", "派对", R.drawable.ic_fe_party),
        EventIcon("balloon", "气球", R.drawable.ic_fe_balloon),
        EventIcon("christmas", "圣诞", R.drawable.ic_fe_christmas),
        EventIcon("halloween", "万圣节", R.drawable.ic_fe_halloween),
        EventIcon("fireworks", "烟花", R.drawable.ic_fe_fireworks),
        EventIcon("firecracker", "春节", R.drawable.ic_fe_firecracker),
        EventIcon("red_envelope", "红包", R.drawable.ic_fe_red_envelope),
        EventIcon("mooncake", "中秋", R.drawable.ic_fe_mooncake),
        EventIcon("dumpling", "饺子", R.drawable.ic_fe_dumpling),
        EventIcon("wedding", "婚礼", R.drawable.ic_fe_wedding),
    ),
    "健康 / 运动" to listOf(
        EventIcon("pill", "服药", R.drawable.ic_fe_pill),
        EventIcon("hospital", "就医", R.drawable.ic_fe_hospital),
        EventIcon("stethoscope", "体检", R.drawable.ic_fe_stethoscope),
        EventIcon("syringe", "疫苗", R.drawable.ic_fe_syringe),
        EventIcon("tooth", "牙科", R.drawable.ic_fe_tooth),
        EventIcon("soccer", "足球", R.drawable.ic_fe_soccer),
        EventIcon("basketball", "篮球", R.drawable.ic_fe_basketball),
        EventIcon("badminton", "羽毛球", R.drawable.ic_fe_badminton),
        EventIcon("pingpong", "乒乓", R.drawable.ic_fe_pingpong),
        EventIcon("bicycle", "骑行", R.drawable.ic_fe_bicycle),
        EventIcon("running", "跑步", R.drawable.ic_fe_running),
        EventIcon("trophy", "成就", R.drawable.ic_fe_trophy),
    ),
    "情感" to listOf(
        EventIcon("heart", "爱意", R.drawable.ic_fe_heart),
        EventIcon("heart_arrow", "心动", R.drawable.ic_fe_heart_arrow),
        EventIcon("two_hearts", "恋爱", R.drawable.ic_fe_two_hearts),
        EventIcon("heart_sparkle", "纪念", R.drawable.ic_fe_heart_sparkle),
        EventIcon("ring", "求婚", R.drawable.ic_fe_ring),
        EventIcon("kiss", "亲吻", R.drawable.ic_fe_kiss),
        EventIcon("broken_heart", "失恋", R.drawable.ic_fe_broken_heart),
    ),
    "娱乐" to listOf(
        EventIcon("music", "音乐", R.drawable.ic_fe_music),
        EventIcon("movie", "电影", R.drawable.ic_fe_movie),
        EventIcon("game", "游戏", R.drawable.ic_fe_game),
        EventIcon("camera", "摄影", R.drawable.ic_fe_camera),
        EventIcon("palette", "绘画", R.drawable.ic_fe_palette),
        EventIcon("guitar", "吉他", R.drawable.ic_fe_guitar),
        EventIcon("ticket", "演出", R.drawable.ic_fe_ticket),
        EventIcon("popcorn", "观影", R.drawable.ic_fe_popcorn),
        EventIcon("headphone", "听歌", R.drawable.ic_fe_headphone),
        EventIcon("chess", "棋类", R.drawable.ic_fe_chess),
    ),
    "旅行" to listOf(
        EventIcon("airplane", "旅行", R.drawable.ic_fe_airplane),
        EventIcon("train", "火车", R.drawable.ic_fe_train),
        EventIcon("car", "自驾", R.drawable.ic_fe_car),
        EventIcon("luggage", "出行", R.drawable.ic_fe_luggage),
        EventIcon("compass", "方向", R.drawable.ic_fe_compass),
        EventIcon("tent", "露营", R.drawable.ic_fe_tent),
        EventIcon("map", "地图", R.drawable.ic_fe_map),
        EventIcon("rocket", "梦想", R.drawable.ic_fe_rocket),
    ),
    "宠物" to listOf(
        EventIcon("dog", "狗狗", R.drawable.ic_fe_dog),
        EventIcon("cat", "猫咪", R.drawable.ic_fe_cat),
        EventIcon("hamster", "仓鼠", R.drawable.ic_fe_hamster),
        EventIcon("fish", "小鱼", R.drawable.ic_fe_fish),
        EventIcon("paw", "宠物", R.drawable.ic_fe_paw),
        EventIcon("chick", "小鸡", R.drawable.ic_fe_chick),
    ),
    "美食" to listOf(
        EventIcon("burger", "快餐", R.drawable.ic_fe_burger),
        EventIcon("pizza", "披萨", R.drawable.ic_fe_pizza),
        EventIcon("noodle", "面食", R.drawable.ic_fe_noodle),
        EventIcon("sushi", "日料", R.drawable.ic_fe_sushi),
        EventIcon("bento", "便当", R.drawable.ic_fe_bento),
        EventIcon("beer", "喝酒", R.drawable.ic_fe_beer),
        EventIcon("wine", "红酒", R.drawable.ic_fe_wine),
        EventIcon("shortcake", "甜点", R.drawable.ic_fe_shortcake),
        EventIcon("curry", "咖喱", R.drawable.ic_fe_curry),
        EventIcon("hotpot", "火锅", R.drawable.ic_fe_hotpot),
    ),
    "其他" to listOf(
        EventIcon("star", "星星", R.drawable.ic_fe_star),
        EventIcon("star_glow", "许愿", R.drawable.ic_fe_star_glow),
        EventIcon("sun", "晴天", R.drawable.ic_fe_sun),
        EventIcon("moon", "夜晚", R.drawable.ic_fe_moon),
        EventIcon("umbrella", "雨伞", R.drawable.ic_fe_umbrella),
        EventIcon("rainbow", "彩虹", R.drawable.ic_fe_rainbow),
        EventIcon("crown", "心愿", R.drawable.ic_fe_crown),
        EventIcon("fire", "热度", R.drawable.ic_fe_fire),
        EventIcon("anchor", "稳定", R.drawable.ic_fe_anchor),
        EventIcon("flag", "目标", R.drawable.ic_fe_flag),
        EventIcon("kite", "风筝", R.drawable.ic_fe_kite),
        EventIcon("sparkler", "手持烟花", R.drawable.ic_fe_sparkler),
    ),
)

private val eventIconMap: Map<String, EventIcon> =
    EventIconGroups.flatMap { it.second }.associateBy { it.key }

/** 按 key 查事件图标；未设置或未知返回 null */
fun eventIconOf(key: String?): EventIcon? =
    if (key.isNullOrEmpty()) null else eventIconMap[key]
