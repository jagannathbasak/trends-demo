package com.nvile.app

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.nvile.app.data.Role
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.AuroraBackground
import com.nvile.app.ui.components.FloatingTabBar
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.TabItem
import com.nvile.app.ui.screens.AlertsScreen
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
import com.nvile.app.ui.theme.NvileTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders every screen on the JVM and writes PNGs to app/build/screenshots, so the design
 * can be reviewed from CI without a device.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h1500dp-xhdpi")
class ScreenshotTest {
    @get:Rule val compose = createComposeRule()

    private val vm by lazy { NvileViewModel(ApplicationProvider.getApplicationContext<Application>()) }

    private val tabs = listOf(
        TabItem("today", "Today", NvIcons.Today),
        TabItem("signals", "Signals", NvIcons.Signals),
        TabItem("ask", "Ask", NvIcons.Ask),
        TabItem("ideas", "Ideas", NvIcons.Ideas),
        TabItem("you", "You", NvIcons.You),
    )

    private fun shot(name: String, tab: Int = -1, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            NvileTheme {
                Box(Modifier.fillMaxSize()) {
                    AuroraBackground()
                    content()
                    if (tab >= 0) {
                        Box(Modifier.align(Alignment.BottomCenter)) { FloatingTabBar(tabs, tab, {}) }
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(6_000)
        compose.onRoot().captureRoboImage("build/screenshots/$name.png")
    }

    @Test fun s01_welcome() = shot("01-welcome") { WelcomeScreen({}, {}) }
    @Test fun s02_onboarding() = shot("02-onboarding") { OnboardingScreen(vm, { _: Role, _: Set<String> -> }, {}) }
    @Test fun s03_today() = shot("03-today", 0) { TodayScreen(vm, {}, {}, {}, {}, {}) }
    @Test fun s04_signals() = shot("04-signals", 1) { SignalsScreen(vm) {} }
    @Test fun s05_market() = shot("05-market", 1) { MarketScreen(vm, "ai-workflow", {}, {}, {}) }
    @Test fun s06_drivers() = shot("06-drivers", 1) { DriversScreen(vm, "ai-workflow") {} }
    @Test fun s07_decision() = shot("07-decision", 1) { DecisionScreen(vm, "ai-workflow") {} }
    @Test fun s08_ask() = shot("08-ask", 2) { AskScreen(vm) {} }
    @Test fun s10_score_idea() = shot("10-score-idea", 3) { ScoreIdeaScreen(vm) {} }
    @Test fun s11_idea_result() {
        vm.scoreIdea()
        shot("11-idea-result", 3) { IdeaResultScreen(vm, {}, {}) }
    }
    @Test fun s12_alerts() = shot("12-alerts", 0) { AlertsScreen(vm, {}, {}, {}, {}) }
    @Test fun s13_track_record() = shot("13-track-record", 4) { TrackRecordScreen(vm) {} }
    @Test fun s14_profile() = shot("14-profile", 4) { ProfileScreen(vm, {}, {}, {}) }
    @Test fun s15_market_caution() = shot("15-market-clinical", 1) { MarketScreen(vm, "clinical-ai", {}, {}, {}) }
}
