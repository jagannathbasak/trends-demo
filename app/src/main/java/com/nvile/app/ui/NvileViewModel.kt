package com.nvile.app.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.nvile.app.data.AlertCategory
import com.nvile.app.data.Horizon
import com.nvile.app.data.IdeaScore
import com.nvile.app.data.NvileRepository
import com.nvile.app.data.Role
import com.nvile.app.data.SeedRepository

/** App-wide UI state. Persisted bits live in SharedPreferences; the rest is session state. */
class NvileViewModel(app: Application) : AndroidViewModel(app) {
    val repo: NvileRepository = SeedRepository
    private val prefs = app.getSharedPreferences("nvile", Context.MODE_PRIVATE)

    var onboarded by mutableStateOf(prefs.getBoolean(KEY_ONBOARDED, false)); private set
    var role by mutableStateOf(Role.entries.getOrElse(prefs.getInt(KEY_ROLE, 0)) { Role.FOUNDER }); private set
    var tracked by mutableStateOf(
        prefs.getStringSet(KEY_TRACKED, null)?.toSet() ?: repo.markets().map { it.id }.toSet(),
    ); private set

    var readAlerts by mutableStateOf(setOf<String>()); private set
    var alertPrefs by mutableStateOf(mapOf("verdict" to true, "risk" to true, "digest" to false)); private set
    var connected by mutableStateOf(setOf("slack")); private set
    var trackedDecisions by mutableStateOf(setOf<String>()); private set
    var alertTab by mutableStateOf<AlertCategory?>(null)

    // Ask flow
    var question by mutableStateOf("")
    var deepAnswer by mutableStateOf(false)

    // Idea flow
    var ideaText by mutableStateOf("Agent-based workflow automation for mid-market firms in Germany and the Netherlands")
    var ideaBuyer by mutableStateOf(0)
    var ideaRegions by mutableStateOf(setOf("Germany", "Netherlands"))
    var ideaHorizon by mutableStateOf(Horizon.M12)
    var ideaResult by mutableStateOf<IdeaScore?>(null); private set
    var trackedIdea by mutableStateOf(false)

    val unreadCount: Int get() = repo.alerts().count { it.id !in readAlerts }

    fun completeOnboarding(role: Role, markets: Set<String>) {
        this.role = role
        if (markets.isNotEmpty()) tracked = markets
        onboarded = true
        prefs.edit()
            .putBoolean(KEY_ONBOARDED, true)
            .putInt(KEY_ROLE, role.ordinal)
            .putStringSet(KEY_TRACKED, tracked)
            .apply()
    }

    fun signOut() {
        onboarded = false
        prefs.edit().clear().apply()
        tracked = repo.markets().map { it.id }.toSet()
        readAlerts = emptySet()
    }

    fun toggleTracked(id: String) {
        tracked = if (id in tracked) tracked - id else tracked + id
        prefs.edit().putStringSet(KEY_TRACKED, tracked).apply()
    }

    fun toggleDecision(id: String) {
        trackedDecisions = if (id in trackedDecisions) trackedDecisions - id else trackedDecisions + id
    }

    fun markRead(id: String) { readAlerts = readAlerts + id }
    fun markAllRead() { readAlerts = repo.alerts().map { it.id }.toSet() }
    fun setAlertPref(key: String, on: Boolean) { alertPrefs = alertPrefs + (key to on) }
    fun toggleIntegration(id: String) { connected = if (id in connected) connected - id else connected + id }
    fun toggleRegion(r: String) { ideaRegions = if (r in ideaRegions) ideaRegions - r else ideaRegions + r }

    fun scoreIdea() {
        ideaResult = repo.scoreIdea(ideaText.ifBlank { "Agent-based workflow automation" }, ideaHorizon, ideaRegions)
        trackedIdea = false
    }

    private companion object {
        const val KEY_ONBOARDED = "onboarded"
        const val KEY_ROLE = "role"
        const val KEY_TRACKED = "tracked"
    }
}
