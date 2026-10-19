package com.ailisi.jinianri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ailisi.jinianri.ui.MainViewModel
import com.ailisi.jinianri.ui.liquid.LiquidBottomBar
import com.ailisi.jinianri.ui.screens.CategoriesScreen
import com.ailisi.jinianri.ui.screens.DetailScreen
import com.ailisi.jinianri.ui.screens.EditorScreen
import com.ailisi.jinianri.ui.screens.HolidayImportScreen
import com.ailisi.jinianri.ui.screens.HomeScreen
import com.ailisi.jinianri.ui.screens.LunarScreen
import com.ailisi.jinianri.ui.screens.MeScreen
import com.ailisi.jinianri.ui.theme.JinianriTheme
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val initialEventId = intent.getLongExtra("eventId", -1L)

        setContent {
            val themeKey by viewModel.themeKey.collectAsState()
            JinianriTheme(themeKey = themeKey) {
                JinianriNav(viewModel, initialEventId)
            }
        }
    }
}

@Serializable
object HomeRoute

@Serializable
object LunarRoute

@Serializable
object MeRoute

@Serializable
object CategoriesRoute

@Serializable
object HolidayImportRoute

@Serializable
data class EditorRoute(val eventId: Long = -1L, val presetCategoryId: Long = -1L)

@Serializable
data class DetailRoute(val eventId: Long)

@Composable
private fun JinianriNav(viewModel: MainViewModel, initialEventId: Long) {
    val navController = rememberNavController()

    /**
     * 底部标签切换：
     * 1. 已在目标标签时直接忽略（连点零操作，杜绝白屏）
     * 2. 否则 popUpTo(起始页) + saveState/restoreState + launchSingleTop 原子切换
     */
    val openTab: (Any) -> Unit = { route ->
        val alreadyThere = navController.currentBackStackEntry?.destination?.hasRoute(route::class) == true
        if (!alreadyThere) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    LaunchedEffect(initialEventId) {
        if (initialEventId > 0) {
            navController.navigate(DetailRoute(initialEventId))
        }
    }

    // 回到前台时刷新「今天」：跨过午夜后首页倒计时不会停在昨天
    LifecycleResumeEffect(Unit) {
        viewModel.refreshToday()
        onPauseOrDispose { }
    }

    // 液态玻璃底栏在导航宿主层常驻（不随页面切换销毁），
    // 点击标签时液滴会滑动到目标位置，和官方 demo 的行为一致。
    val bottomBackdrop = rememberLayerBackdrop()
    val backStackEntryState = navController.currentBackStackEntryAsState()
    val backStackEntry by backStackEntryState
    val destination = backStackEntry?.destination
    val tabIndex = when {
        destination?.hasRoute(HomeRoute::class) == true -> 0
        destination?.hasRoute(LunarRoute::class) == true -> 1
        destination?.hasRoute(MeRoute::class) == true -> 2
        else -> null
    }

    var activeTabIndex by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(tabIndex) {
        if (tabIndex != null) {
            activeTabIndex = tabIndex
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(bottomBackdrop)
        ) {
            NavHost(navController = navController, startDestination = HomeRoute) {
                composable<HomeRoute> {
                    HomeScreen(
                        viewModel = viewModel,
                        onAddEvent = { navController.navigate(EditorRoute()) { launchSingleTop = true } },
                        onOpenEvent = { id -> navController.navigate(DetailRoute(id)) { launchSingleTop = true } },
                    )
                }
                composable<LunarRoute> {
                    LunarScreen(
                        viewModel = viewModel,
                        onOpenEvent = { id -> navController.navigate(DetailRoute(id)) { launchSingleTop = true } },
                        onOpenHolidayImport = { navController.navigate(HolidayImportRoute) },
                    )
                }
                composable<MeRoute> {
                    MeScreen(
                        viewModel = viewModel,
                        onOpenCategories = { navController.navigate(CategoriesRoute) },
                        onOpenHolidayImport = { navController.navigate(HolidayImportRoute) },
                    )
                }
                composable<CategoriesRoute> {
                    CategoriesScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<HolidayImportRoute> {
                    HolidayImportScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<EditorRoute> { entry ->
                    val route = entry.toRoute<EditorRoute>()
                    EditorScreen(
                        viewModel = viewModel,
                        eventId = route.eventId,
                        presetCategoryId = route.presetCategoryId.takeIf { it > 0 },
                        onBack = { navController.popBackStack() },
                        onSaved = { id ->
                            navController.popBackStack()
                            if (id > 0) {
                                navController.navigate(DetailRoute(id))
                            }
                        },
                    )
                }
                composable<DetailRoute> { entry ->
                    val route = entry.toRoute<DetailRoute>()
                    DetailScreen(
                        viewModel = viewModel,
                        eventId = route.eventId,
                        onBack = { navController.popBackStack() },
                        onEdit = { id -> navController.navigate(EditorRoute(id)) },
                    )
                }
            }
        }
        // 仅在三个主标签页显示底栏；编辑/详情/分类页隐藏
        if (tabIndex != null) {
            LiquidBottomBar(
                backdrop = bottomBackdrop,
                selectedIndex = { activeTabIndex },
                onSelected = { index ->
                    when (index) {
                        0 -> openTab(HomeRoute)
                        1 -> openTab(LunarRoute)
                        else -> openTab(MeRoute)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 36.dp)
                    .padding(bottom = 14.dp),
            )
        }
    }
}
