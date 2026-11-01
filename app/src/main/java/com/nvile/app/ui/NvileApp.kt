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
import androidx.compose.foundation.background
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
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.TabBar
import com.nvile.app.ui.components.TabItem
import com.nvile.app.ui.screens.AlertsScreen
import com.nvile.app.ui.screens.AskScreen
import com.nvile.app.ui.screens.DecisionScreen
import com.nvile.app.ui.screens.IdeaResultScreen
import com.nvile.app.ui.screens.IdeasScreen
import com.nvile.app.ui.screens.MarketScreen
import com.nvile.app.ui.screens.MarketsScreen
import com.nvile.app.ui.screens.OnboardingScreen
import com.nvile.app.ui.screens.ProfileScreen
import com.nvile.app.ui.screens.PulseScreen
import com.nvile.app.ui.screens.TrackRecordScreen
import com.nvile.app.ui.screens.WelcomeScreen
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvMotion

object Routes {
    const val WELCOME = "welcome"
    const val ONBOARDING = "onboarding"
    const val PULSE = "pulse"
    const val MARKETS = "markets"
    const val ASK = "ask"
    const val IDEAS = "ideas"
    const val YOU = "you"
    const val MARKET = "market/{id}"
    const val DECISION = "decision/{id}"
    const val IDEA_RESULT = "idea-result"
    const val ALERTS = "alerts"
    const val TRACK_RECORD = "track-record"

    fun market(id: String) = "market/$id"
    fun decision(id: String) = "decision/$id"
}

val AppTabs = listOf(
    TabItem(Routes.PULSE, "Pulse", NvIcons.Pulse),
    TabItem(Routes.MARKETS, "Markets", NvIcons.Markets),
    TabItem(Routes.ASK, "Ask", null),
    TabItem(Routes.IDEAS, "Ideas", NvIcons.Ideas),
    TabItem(Routes.YOU, "You", NvIcons.You),
)

private fun tabFor(route: String?): Int = when (route) {
    Routes.PULSE, Routes.ALERTS -> 0
    Routes.MARKETS, Routes.MARKET -> 1
    Routes.ASK -> 2
    Routes.IDEAS, Routes.IDEA_RESULT -> 3
    Routes.YOU, Routes.TRACK_RECORD -> 4
    else -> -1 // welcome, onboarding, decision story: no tab bar
}

private val pushRoutes = setOf(Routes.MARKET, Routes.IDEA_RESULT, Routes.ALERTS, Routes.TRACK_RECORD)

fun NavHostController.switchTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
fun NvileApp(vm: NvileViewModel = viewModel()) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val selected = tabFor(entry?.destination?.route)

    Box(Modifier.fillMaxSize().background(Nv.Black)) {
        NavHost(
            navController = nav,
            startDestination = if (vm.onboarded) Routes.PULSE else Routes.WELCOME,
            enterTransition = {
                when (targetState.destination.route) {
                    in pushRoutes -> slideInHorizontally(tween(520, easing = NvMotion.Ease)) { it } + fadeIn(tween(300))
                    Routes.DECISION -> slideInVertically(tween(600, easing = NvMotion.Ease)) { it } + fadeIn(tween(300))
                    else -> fadeIn(tween(380)) + scaleIn(tween(500, easing = NvMotion.Ease), initialScale = 0.96f)
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    in pushRoutes -> slideOutHorizontally(tween(520, easing = NvMotion.Ease)) { -it / 4 } + fadeOut(tween(400))
                    Routes.DECISION -> scaleOut(tween(600, easing = NvMotion.Ease), targetScale = 0.92f) + fadeOut(tween(500))
                    else -> fadeOut(tween(220))
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    in pushRoutes -> slideInHorizontally(tween(520, easing = NvMotion.Ease)) { -it / 4 } + fadeIn(tween(400))
                    Routes.DECISION -> scaleIn(tween(600, easing = NvMotion.Ease), initialScale = 0.92f) + fadeIn(tween(400))
                    else -> fadeIn(tween(300))
                }
            },
            popExitTransition = {
                when (initialState.destination.route) {
                    in pushRoutes -> slideOutHorizontally(tween(480, easing = NvMotion.Ease)) { it } + fadeOut(tween(300))
                    Routes.DECISION -> slideOutVertically(tween(520, easing = NvMotion.Ease)) { it } + fadeOut(tween(300))
                    else -> fadeOut(tween(220)) + scaleOut(tween(320), targetScale = 0.96f)
                }
            },
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onStart = { nav.navigate(Routes.ONBOARDING) },
                    onSignIn = {
                        vm.completeOnboarding(vm.role, vm.tracked)
                        nav.navigate(Routes.PULSE) { popUpTo(Routes.WELCOME) { inclusive = true } }
                    },
                )
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    vm = vm,
                    onDone = { role, markets ->
                        vm.completeOnboarding(role, markets)
                        nav.navigate(Routes.PULSE) { popUpTo(0) { inclusive = true } }
                    },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Routes.PULSE) {
                PulseScreen(
                    vm = vm,
                    openMarket = { nav.navigate(Routes.market(it)) },
                    openAlerts = { nav.navigate(Routes.ALERTS) },
                    openAsk = { nav.switchTab(Routes.ASK) },
                    openTrackRecord = { nav.navigate(Routes.TRACK_RECORD) },
                )
            }
            composable(Routes.MARKETS) { MarketsScreen(vm) { nav.navigate(Routes.market(it)) } }
            composable(Routes.MARKET, arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                val id = it.arguments?.getString("id").orEmpty()
                MarketScreen(vm, id, onBack = { nav.popBackStack() }, openDecision = { nav.navigate(Routes.decision(id)) })
            }
            composable(Routes.DECISION, arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                DecisionScreen(vm, it.arguments?.getString("id").orEmpty(), onClose = { nav.popBackStack() })
            }
            composable(Routes.ASK) {
                AskScreen(vm, openMarket = { nav.navigate(Routes.market(it)) }, openDecision = { nav.navigate(Routes.decision(it)) })
            }
            composable(Routes.IDEAS) { IdeasScreen(vm) { nav.navigate(Routes.IDEA_RESULT) } }
            composable(Routes.IDEA_RESULT) {
                IdeaResultScreen(vm, onBack = { nav.popBackStack() }, openMarket = { nav.navigate(Routes.market(it)) })
            }
            composable(Routes.ALERTS) {
                AlertsScreen(vm, onBack = { nav.popBackStack() }, openMarket = { nav.navigate(Routes.market(it)) }, openTrackRecord = { nav.navigate(Routes.TRACK_RECORD) })
            }
            composable(Routes.TRACK_RECORD) { TrackRecordScreen(vm) { nav.popBackStack() } }
            composable(Routes.YOU) {
                ProfileScreen(
                    vm,
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
            exit = slideOutVertically(tween(320)) { it } + fadeOut(),
        ) {
            TabBar(AppTabs, selected, onSelect = { nav.switchTab(AppTabs[it].route) })
        }
    }
}
