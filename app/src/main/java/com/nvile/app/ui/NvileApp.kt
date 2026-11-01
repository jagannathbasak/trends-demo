package com.nvile.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nvile.app.ui.components.AuroraBackground
import com.nvile.app.ui.components.FloatingTabBar
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.TabItem
import com.nvile.app.ui.screens.AlertsScreen
import com.nvile.app.ui.screens.AnswerScreen
import com.nvile.app.ui.screens.AskScreen
import com.nvile.app.ui.screens.DecisionScreen
import com.nvile.app.ui.screens.DriversScreen
import com.nvile.app.ui.screens.IdeaResultScreen
import com.nvile.app.ui.screens.MarketScreen
import com.nvile.app.ui.screens.OnboardingScreen
import com.nvile.app.ui.screens.ProfileScreen
import com.nvile.app.ui.screens.ScoreIdeaScreen
import com.nvile.app.ui.screens.SignalsScreen
import com.nvile.app.ui.screens.TodayScreen
import com.nvile.app.ui.screens.TrackRecordScreen
import com.nvile.app.ui.screens.WelcomeScreen
import com.nvile.app.ui.theme.NvMotion

object Routes {
    const val WELCOME = "welcome"
    const val ONBOARDING = "onboarding"
    const val TODAY = "today"
    const val SIGNALS = "signals"
    const val ASK = "ask"
    const val IDEAS = "ideas"
    const val YOU = "you"
    const val MARKET = "market/{id}"
    const val DRIVERS = "drivers/{id}"
    const val DECISION = "decision/{id}"
    const val ANSWER = "answer"
    const val IDEA_RESULT = "idea-result"
    const val ALERTS = "alerts"
    const val TRACK_RECORD = "track-record"

    fun market(id: String) = "market/$id"
    fun drivers(id: String) = "drivers/$id"
    fun decision(id: String) = "decision/$id"
}

private val tabs = listOf(
    TabItem(Routes.TODAY, "Today", NvIcons.Today),
    TabItem(Routes.SIGNALS, "Signals", NvIcons.Signals),
    TabItem(Routes.ASK, "Ask", NvIcons.Ask),
    TabItem(Routes.IDEAS, "Ideas", NvIcons.Ideas),
    TabItem(Routes.YOU, "You", NvIcons.You),
)

/** Which tab owns a route, so detail screens keep their parent tab lit. */
private fun tabFor(route: String?): Int = when (route) {
    Routes.TODAY, Routes.ALERTS -> 0
    Routes.SIGNALS, Routes.MARKET, Routes.DRIVERS, Routes.DECISION -> 1
    Routes.ASK, Routes.ANSWER -> 2
    Routes.IDEAS, Routes.IDEA_RESULT -> 3
    Routes.YOU, Routes.TRACK_RECORD -> 4
    else -> -1
}

private val detailRoutes = setOf(
    Routes.MARKET, Routes.DRIVERS, Routes.DECISION, Routes.ANSWER,
    Routes.IDEA_RESULT, Routes.ALERTS, Routes.TRACK_RECORD,
)

fun NavHostController.switchTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
fun NvileApp(vm: NvileViewModel = viewModel()) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val selected = tabFor(route)

    Box(Modifier.fillMaxSize()) {
        AuroraBackground(intensity = if (route == Routes.WELCOME) 1.6f else 1f)

        NavHost(
            navController = nav,
            startDestination = if (vm.onboarded) Routes.TODAY else Routes.WELCOME,
            enterTransition = {
                if (targetState.destination.route in detailRoutes) {
                    slideInHorizontally(tween(460, easing = NvMotion.Ease)) { it / 3 } + fadeIn(tween(320))
                } else {
                    fadeIn(tween(340)) + scaleIn(tween(420, easing = NvMotion.Ease), initialScale = 0.975f)
                }
            },
            exitTransition = {
                if (targetState.destination.route in detailRoutes) {
                    slideOutHorizontally(tween(460, easing = NvMotion.Ease)) { -it / 8 } + fadeOut(tween(260))
                } else {
                    fadeOut(tween(200))
                }
            },
            popEnterTransition = {
                if (initialState.destination.route in detailRoutes) {
                    slideInHorizontally(tween(460, easing = NvMotion.Ease)) { -it / 8 } + fadeIn(tween(320))
                } else {
                    fadeIn(tween(300))
                }
            },
            popExitTransition = {
                if (initialState.destination.route in detailRoutes) {
                    slideOutHorizontally(tween(420, easing = NvMotion.Ease)) { it / 3 } + fadeOut(tween(260))
                } else {
                    fadeOut(tween(200)) + scaleOut(tween(300), targetScale = 0.975f)
                }
            },
        ) {
            composable(Routes.WELCOME, exitTransition = { fadeOut(tween(420)) + scaleOut(tween(500), targetScale = 1.04f) }) {
                WelcomeScreen(
                    onStart = { nav.navigate(Routes.ONBOARDING) },
                    onSignIn = {
                        vm.completeOnboarding(vm.role, vm.tracked)
                        nav.navigate(Routes.TODAY) { popUpTo(Routes.WELCOME) { inclusive = true } }
                    },
                )
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    vm = vm,
                    onDone = { role, markets ->
                        vm.completeOnboarding(role, markets)
                        nav.navigate(Routes.TODAY) { popUpTo(0) { inclusive = true } }
                    },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Routes.TODAY) {
                TodayScreen(
                    vm = vm,
                    openMarket = { nav.navigate(Routes.market(it)) },
                    openDrivers = { nav.navigate(Routes.drivers(it)) },
                    openAlerts = { nav.navigate(Routes.ALERTS) },
                    openAsk = { nav.switchTab(Routes.ASK) },
                    openTrackRecord = { nav.navigate(Routes.TRACK_RECORD) },
                )
            }
            composable(Routes.SIGNALS) {
                SignalsScreen(vm = vm, openMarket = { nav.navigate(Routes.market(it)) })
            }
            composable(Routes.MARKET, arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                val id = it.arguments?.getString("id").orEmpty()
                MarketScreen(
                    vm = vm, marketId = id,
                    onBack = { nav.popBackStack() },
                    openDecision = { nav.navigate(Routes.decision(id)) },
                    openDrivers = { nav.navigate(Routes.drivers(id)) },
                )
            }
            composable(Routes.DRIVERS, arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                DriversScreen(vm = vm, marketId = it.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() })
            }
            composable(Routes.DECISION, arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                DecisionScreen(vm = vm, marketId = it.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() })
            }
            composable(Routes.ASK) {
                AskScreen(vm = vm, onAsk = { nav.navigate(Routes.ANSWER) })
            }
            composable(Routes.ANSWER) {
                AnswerScreen(
                    vm = vm,
                    onBack = { nav.popBackStack() },
                    openDecision = { nav.navigate(Routes.decision(it)) },
                    openDrivers = { nav.navigate(Routes.drivers(it)) },
                    openMarket = { nav.navigate(Routes.market(it)) },
                )
            }
            composable(Routes.IDEAS) {
                ScoreIdeaScreen(vm = vm, onScored = { nav.navigate(Routes.IDEA_RESULT) })
            }
            composable(Routes.IDEA_RESULT) {
                IdeaResultScreen(vm = vm, onBack = { nav.popBackStack() }, openMarket = { nav.navigate(Routes.market(it)) })
            }
            composable(Routes.ALERTS) {
                AlertsScreen(
                    vm = vm,
                    onBack = { nav.popBackStack() },
                    openMarket = { nav.navigate(Routes.market(it)) },
                    openDrivers = { nav.navigate(Routes.drivers(it)) },
                    openTrackRecord = { nav.navigate(Routes.TRACK_RECORD) },
                )
            }
            composable(Routes.TRACK_RECORD) {
                TrackRecordScreen(vm = vm, onBack = { nav.popBackStack() })
            }
            composable(Routes.YOU) {
                ProfileScreen(
                    vm = vm,
                    openTrackRecord = { nav.navigate(Routes.TRACK_RECORD) },
                    openAlerts = { nav.navigate(Routes.ALERTS) },
                    onSignOut = {
                        vm.signOut()
                        nav.navigate(Routes.WELCOME) { popUpTo(0) { inclusive = true } }
                    },
                )
            }
        }

        AnimatedVisibility(
            visible = selected >= 0,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(NvMotion.settle()) { it } + fadeIn(),
            exit = slideOutVertically(tween(300)) { it } + fadeOut(),
        ) {
            FloatingTabBar(items = tabs, selected = selected, onSelect = { nav.switchTab(tabs[it].route) })
        }
    }
}

