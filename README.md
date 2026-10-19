# 纪念日（Jinianri）

一个简洁的 Android 纪念日/生日/考试倒计时应用。

- **UI**：Material 3 Expressive + 液态玻璃（kyant0 backdrop 官方组件移植：折射底栏 + 液滴滑动、玻璃 FAB、玻璃卡片，拖动卡片时悬浮玻璃实时折射下方内容）
- **仅日间模式**：白底、细浅描边、留白克制
- **主题配色**：13 套可选（Catppuccin Latte / Rosé Pine / Solarized / GitHub Primer / Gruvbox / Material 3），点选实时生效
- **数据**：Room 本地持久化，重启不丢
- **节日**：内置 56 个节日一键勾选导入，法定假期按连休天数在日历上标出
- **农历**：支持农历日期（闰月自动处理），农历生日/纪念日按农历每年滚动

## 功能

| 页面 | 说明 |
|---|---|
| 首页 | 双列卡片 / 列表两种视图（一键切换）、长按拖动卡片排序（顺序持久化）、大数字倒计时卡片带事件图标、搜索、分类标签栏（真实过滤、可与搜索叠加）、FAB 添加 |
| 添加/编辑 | 名称、公历/农历选择、事件图标选择器（109 个 Fluent Emoji，11 个分组）、分类选择器 + 页内新建分类、倒计时/正计时、每年重复、提前提醒、备注、完整校验 |
| 详情 | 大号天数玻璃卡片（含事件图标）、分类/日期/重复/提醒信息、编辑、分享、删除 |
| 分类管理 | 新建、重命名、换色换图标、排序、删除（事件移入「未分类」）、「未分类」系统保护 |
| 节日导入 | 内置 56 个节日（法定节假日 / 传统节日 / 公历节日 / 国际节日 / 二十四节气），按组勾选、全选，一键导入并归入「节日」分类；已导入的会标出，避免重复 |
| 农历页 | 今日农历与干支生肖、月历（农历日标注、今天高亮、有纪念日的日期格显示事件图标与分类色底、放假区间标「休」）、即将到来的假期（连休天数与起止日）、全部农历纪念日列表 |
| 我的 | 主题配色选择、分类管理入口、节日导入入口、通知权限、关于 |

### 节日日期怎么算

- **公历 / 农历固定日期**：按每年同月同日滚动（沿用日期引擎的公历 2/29、农历闰月与小月规则）
- **节气**（清明等 24 个）：用 lunar-java 的节气表取每年实际交节日（清明 4/4 或 4/5）
- **「第几个星期几」**（母亲节 5 月第 2 个星期日、父亲节、感恩节）：按年重算
- **放假天数**：不联网，按「法定天数 + 典型连休长度」推算（元旦/清明/端午/中秋 3 天、劳动节 5 天、春节 8 天、国庆 7 天）。
  推算的起止日与官方可能差一两天，也标不出调休补班日；`HolidayCalendar.OFFICIAL` 已留好按年补录官方通知的位置，补一行即可精确覆盖。

## 技术栈

- Kotlin 2.3.20 · Jetpack Compose（Compose BOM 2026.09.00）· Material 3
- AGP 9.4.0 · Gradle 9.6.1 · compileSdk 37 · minSdk 25
- Room 2.8.5（KSP，v3 迁移：事件图标 + 自定义排序字段）· WorkManager（提醒）· kotlinx-serialization
- 农历换算：[cn.6tail:lunar:1.7.7](https://github.com/6tail/lunar-java)
- 液态玻璃：[Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass)（Apache-2.0，组件移植到 `ui/liquid/`）+ `io.github.kyant0:backdrop:1.0.6`
- 玻璃兜底：[styropyr0/PrismalAGSL v1.0.3](https://github.com/styropyr0/PrismalAGSL)（AAR 已固化在 `app/libs/`，API 33+ 完整效果、低版本自动降级）
- 事件图标：[Microsoft Fluent Emoji](https://github.com/microsoft/fluentui-emoji)（MIT，3D 风格，已内置 109 个，见 `res/raw/fluent_emoji_license.txt`）

## 构建运行

```bash
# 需要 Android SDK（platforms;android-37.0 / build-tools;37.0.0）
./gradlew assembleDebug        # 生成 app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # 安装到已连接的设备/模拟器
./gradlew testDebugUnitTest    # 运行单元测试（日期引擎 + 节日规则，共 39 项）
```

## 测试

- `app/src/test/java/com/ailisi/jinianri/DateEngineTest.kt`（21 项）
  公历/农历下一次发生日、2 月 29 日在平年、农历小月无三十、跨年滚动、分类筛选与搜索叠加、未分类边界。
- `app/src/test/java/com/ailisi/jinianri/FestivalRulesTest.kt`（18 项）
  24 节气与跨年滚动、第 n 个星期几、坏规则不崩溃、规则型事件接入日期引擎、放假推算（含春节跟除夕走、跨年取元旦）、节日目录完整性。

> 注：工程若放在中文路径下，本机 Gradle 的测试 worker 会因 JVM 启动器按系统 ANSI 编码读取 classpath 参数文件而报
> `ClassNotFoundException`（代码与测试本身正常）。换到纯 ASCII 路径，或直接用 JUnitCore 运行即可。

## 目录

```
app/src/main/java/com/ailisi/jinianri/
├── data/        Room 实体 / DAO / 数据库（schema v4）/ 仓库
├── domain/      DateEngine（日期引擎）、FestivalRules（节气与第 n 个星期几）、
│                FestivalCatalog（节日目录）、HolidayCalendar（放假推算）、ReminderWorker（提醒）
└── ui/          theme（含 AppThemes 配色目录）、components（玻璃组件/事件图标目录）、
                 liquid（Kyant0 液态玻璃组件移植）、screens（七大页面，含节日导入）
```
