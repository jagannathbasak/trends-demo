package com.nvile.app.data

/**
 * Domain model for the NVILE opportunity report, mirroring the data contract:
 * no figure travels without its evidence class.
 */
enum class Evidence(val glyph: String, val label: String, val description: String) {
    VERIFIED("●", "Verified", "Filings, registries, procurement records, government data."),
    SIGNAL("◦", "Signal", "Observed behaviour: hiring, pricing, capital, search, shipping."),
    ESTIMATE("◇", "Estimate", "Derived figures with the method shown."),
    PREDICTION("◈", "Prediction", "Model output with band, confidence, named catalysts and risks."),
}

enum class Stage(val label: String) {
    TREND("TREND"), EMERGING("EMERGING"), WEAK_SIGNAL("WEAK SIGNAL")
}

enum class Horizon(val label: String) {
    D30("30D"), M6("6MO"), M12("12MO"), Y3("3YR")
}

/** Positive momentum, neutral, or a verdict under pressure. */
enum class Tone { UP, NEUTRAL, CAUTION }

data class HorizonForecast(
    val horizon: Horizon,
    /** Central demand change, e.g. 0.68 for +68%. */
    val demand: Double,
    val bandLow: Double,
    val bandHigh: Double,
    val competition: Double,
    val window: String,
    val confidence: Int,
)

data class Scenario(val name: String, val demand: Double, val size: String, val isBase: Boolean = false)

data class RegionGrowth(val name: String, val growth: Double)

data class Catalyst(val title: String, val weight: Double, val evidence: Evidence, val source: String)

data class Risk(val title: String, val probability: Int)

data class Decision(
    val happening: String,
    val why: String,
    val next: String,
    val action: String,
    val recommendation: String,
    val actNow: Int,
    val waitTwoQuarters: Int,
)

data class Market(
    val id: String,
    val name: String,
    val scope: String,
    val stage: Stage,
    val momentum: String,
    val probability: Int,
    val tone: Tone,
    val sources: Int,
    val signals: String,
    val updated: String,
    val marketSize: String,
    /** Observed demand index, oldest first, normalised so today = 100. */
    val observed: List<Double>,
    val horizons: Map<Horizon, HorizonForecast>,
    val scenarios: List<Scenario>,
    val regions: List<RegionGrowth>,
    val regionMomentum: String,
    val catalysts: List<Catalyst>,
    val risks: List<Risk>,
    val evidenceMix: Map<Evidence, Int>,
    val decision: Decision,
    /** One-line state used on the brief when the verdict moved. */
    val verdict: String,
) {
    val m12: HorizonForecast get() = horizons.getValue(Horizon.M12)
    val spark: List<Double> get() = observed.takeLast(6)
}

enum class AlertCategory(val label: String) { VERDICTS("Verdicts"), RISKS("Risks") }

data class Alert(
    val id: String,
    val category: AlertCategory,
    val kind: String,
    val tone: Tone,
    val whenLabel: String,
    val marketId: String?,
    val marketName: String,
    val body: String,
    val destination: AlertDestination,
)

enum class AlertDestination { MARKET, DRIVERS, TRACK_RECORD }

data class ResolvedCall(val claim: String, val bandLow: Double, val bandHigh: Double, val actual: Double)

data class TrackRecord(
    val landedInBand: Int,
    val resolved: Int,
    val leadTimeMonths: Double,
    val withdrawn: Int,
    val latest: ResolvedCall,
)

enum class Role(val title: String, val use: String) {
    FOUNDER("Founder", "Roadmap prioritisation"),
    INVESTOR("Investor", "Category-level conviction"),
    STRATEGY("Strategy", "Three-year scenario planning"),
    CORPDEV("Corporate development", "M&A timing decisions"),
}

data class Integration(val id: String, val name: String, val description: String, val enterpriseOnly: Boolean = false)

data class IdeaScore(
    val idea: String,
    val score: Int,
    val market: Market,
    val horizon: Horizon,
    val label: String,
    val verdict: String,
    val verdictDetail: String,
)

data class RecentScore(val title: String, val verdict: String, val score: Int, val tone: Tone)
