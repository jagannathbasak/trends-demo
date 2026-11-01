package com.nvile.app

import android.app.Application
import androidx.compose.foundation.background
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
import com.nvile.app.ui.AppTabs
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.TabBar
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
import com.nvile.app.ui.theme.NvileTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders every screen on the JVM and writes PNGs to app/build/screenshots, so the design
 * can be reviewed from CI without a device. Phone-sized frames show the first viewport;
 * the tall frames show whole scrolling pages.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class ScreenshotTest {
    @get:Rule val compose = createComposeRule()

    private val vm by lazy { NvileViewModel(ApplicationProvider.getApplicationContext<Application>()) }

    private fun shot(name: String, tab: Int = -1, ms: Long = 6_000, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            NvileTheme {
                Box(Modifier.fillMaxSize().background(Nv.Black)) {
                    content()
                    if (tab >= 0) Box(Modifier.align(Alignment.BottomCenter)) { TabBar(AppTabs, tab, {}) }
                }
            }
        }
        compose.mainClock.advanceTimeBy(ms)
        compose.onRoot().captureRoboImage("build/screenshots/$name.png")
    }

    @Test fun s01_welcome() = shot("01-welcome") { WelcomeScreen({}, {}) }
    @Test fun s02_onboarding() = shot("02-onboarding") { OnboardingScreen(vm, { _: Role, _: Set<String> -> }, {}) }
    @Test fun s03_pulse() = shot("03-pulse", 0) { PulseScreen(vm, {}, {}, {}, {}) }
    @Test fun s04_markets() = shot("04-markets", 1) { MarketsScreen(vm) {} }
    @Test fun s05_market() = shot("05-market", 1) { MarketScreen(vm, "ai-workflow", {}, {}) }
    @Test fun s06_decision() = shot("06-decision", ms = 1_500) { DecisionScreen(vm, "ai-workflow") {} }
    @Test fun s07_ask() = shot("07-ask", 2) { AskScreen(vm, {}, {}) }
    @Test fun s08_ideas() = shot("08-ideas", 3) { IdeasScreen(vm) {} }
    @Test fun s09_idea_result() {
        vm.scoreIdea()
        shot("09-idea-result", 3) { IdeaResultScreen(vm, {}, {}) }
    }
    @Test fun s10_alerts() = shot("10-alerts", 0) { AlertsScreen(vm, {}, {}, {}) }
    @Test fun s11_track_record() = shot("11-track-record", 4) { TrackRecordScreen(vm) {} }
    @Test fun s12_you() = shot("12-you", 4) { ProfileScreen(vm, {}, {}, {}) }
    @Test fun s13_market_clinical() = shot("13-market-clinical", 1) { MarketScreen(vm, "clinical-ai", {}, {}) }

    @Config(qualifiers = "w393dp-h2400dp-xhdpi")
    @Test fun t01_market_full() = shot("t01-market-full", 1) { MarketScreen(vm, "ai-workflow", {}, {}) }

    @Config(qualifiers = "w393dp-h1700dp-xhdpi")
    @Test fun t02_pulse_full() = shot("t02-pulse-full", 0) { PulseScreen(vm, {}, {}, {}, {}) }
}
