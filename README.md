# 纪念日（Jinianri）

一个简洁的 Android 纪念日/生日/考试倒计时应用。

- **UI**：Material 3 Expressive + 液态玻璃（kyant0 backdrop 官方组件移植：折射底栏 + 液滴滑动、玻璃 FAB、玻璃卡片，拖动卡片时悬浮玻璃实时折射下方内容）
- **仅日间模式**：白底、细浅描边、留白克制
- **主题配色**：13 套可选（Catppuccin Latte / Rosé Pine / Solarized / GitHub Primer / Gruvbox / Material 3），点选实时生效
- **数据**：Room 本地持久化，重启不丢
- **农历**：支持农历日期（闰月自动处理），农历生日/纪念日按农历每年滚动

## 功能

| 页面 | 说明 |
|---|---|
| 首页 | 双列卡片 / 列表两种视图（一键切换）、长按拖动卡片排序（顺序持久化）、大数字倒计时卡片带事件图标、搜索、分类标签栏（真实过滤、可与搜索叠加）、FAB 添加 |
| 添加/编辑 | 名称、公历/农历选择、事件图标选择器（109 个 Fluent Emoji，11 个分组）、分类选择器 + 页内新建分类、倒计时/正计时、每年重复、提前提醒、备注、完整校验 |
| 详情 | 大号天数玻璃卡片（含事件图标）、分类/日期/重复/提醒信息、编辑、分享、删除 |
| 分类管理 | 新建、重命名、换色换图标、排序、删除（事件移入「未分类」）、「未分类」系统保护 |
| 农历页 | 今日农历与干支生肖、月历（农历日标注、今天高亮、有纪念日的日期格显示事件图标与分类色底）、全部农历纪念日列表 |
| 我的 | 主题配色选择、分类管理入口、通知权限、关于 |

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
./gradlew testDebugUnitTest    # 运行单元测试（日期引擎边界等 16 项）
```

## 测试

`app/src/test/java/com/ailisi/jinianri/DateEngineTest.kt`

覆盖：公历/农历下一次发生日、2 月 29 日在平年、农历小月无三十、跨年滚动、分类筛选与搜索叠加、未分类边界。

## 目录

```
app/src/main/java/com/ailisi/jinianri/
├── data/        Room 实体 / DAO / 数据库 / 仓库
├── domain/      DateEngine（日期引擎）、ReminderWorker（提醒）
└── ui/          theme（含 AppThemes 配色目录）、components（玻璃组件/事件图标目录）、
                 liquid（Kyant0 液态玻璃组件移植）、screens（六大页面）
```
