package com.nvile.app.data

/**
 * The app's single source of truth. [SeedRepository] serves the demo dataset; a network
 * implementation backed by the NVILE engines API (reports, trends, forecast operations)
 * drops in behind this interface without touching the UI.
 */
interface NvileRepository {
    fun markets(): List<Market>
    fun market(id: String): Market
    fun alerts(): List<Alert>
    fun trackRecord(): TrackRecord
    fun integrations(): List<Integration>
    fun recentScores(): List<RecentScore>
    fun suggestions(): List<String>
    fun regions(): List<String>

    /** Resolve free text (a question or an idea) to the closest tracked market. */
    fun resolve(text: String): Market
    fun scoreIdea(idea: String, horizon: Horizon, regions: Set<String>): IdeaScore
}
