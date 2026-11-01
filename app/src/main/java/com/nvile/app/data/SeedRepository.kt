package com.nvile.app.data

/**
 * Demo dataset. AI Workflow Platforms carries the full report from the NVILE Mobile App
 * design; the other markets carry the row-level figures from the same design with the
 * remaining fields filled for demonstration. Replace with the engines API before launch.
 */
object SeedRepository : NvileRepository {

    private fun hz(
        h: Horizon, d: Double, lo: Double, hi: Double, comp: Double, window: String, conf: Int,
    ) = HorizonForecast(h, d, lo, hi, comp, window, conf)

    /** Derive the four horizons for a market from its 12-month call. */
    private fun derived(d12: Double, c12: Double, conf: Int, windows: List<String>): Map<Horizon, HorizonForecast> {
        val spread = (100 - conf) / 100.0
        fun band(d: Double, k: Double) = Pair(d - kotlin.math.abs(d) * spread * k - 0.01, d + kotlin.math.abs(d) * spread * k * 0.8 + 0.01)
        val d30 = d12 * 0.06; val d6 = d12 * 0.43; val d3 = d12 * 2.6
        val (l30, h30) = band(d30, 2.5); val (l6, h6) = band(d6, 2.0)
        val (l12, h12) = band(d12, 1.6); val (l3, h3) = band(d3, 1.4)
        return mapOf(
            Horizon.D30 to hz(Horizon.D30, d30, l30, h30, c12 * 0.15, windows[0], (conf + 8).coerceAtMost(95)),
            Horizon.M6 to hz(Horizon.M6, d6, l6, h6, c12 * 0.5, windows[1], (conf + 4).coerceAtMost(93)),
            Horizon.M12 to hz(Horizon.M12, d12, l12, h12, c12, windows[2], conf),
            Horizon.Y3 to hz(Horizon.Y3, d3, l3, h3, c12 * 2.8, windows[3], (conf - 14).coerceAtLeast(40)),
        )
    }

    private val aiWorkflow = Market(
        id = "ai-workflow",
        name = "AI Workflow Platforms",
        scope = "DACH mid-market",
        stage = Stage.TREND,
        momentum = "ACCELERATING",
        probability = 87,
        tone = Tone.UP,
        sources = 41,
        signals = "1.2M",
        updated = "4 MIN AGO",
        marketSize = "$14.2B",
        observed = listOf(71.0, 74.0, 79.0, 77.0, 85.0, 93.0, 100.0),
        horizons = mapOf(
            Horizon.D30 to hz(Horizon.D30, 0.04, 0.02, 0.06, 0.05, "Immediate", 90),
            Horizon.M6 to hz(Horizon.M6, 0.29, 0.19, 0.36, 0.17, "Q4 '26 – Q1 '27", 86),
            Horizon.M12 to hz(Horizon.M12, 0.68, 0.52, 0.79, 0.34, "Q1–Q2 2027", 82),
            Horizon.Y3 to hz(Horizon.Y3, 1.82, 1.40, 2.21, 0.96, "Before Q4 2028", 68),
        ),
        scenarios = listOf(
            Scenario("BEAR", 0.21, "$10.8B"),
            Scenario("BASE", 0.68, "$14.2B", isBase = true),
            Scenario("BULL", 1.12, "$18.6B"),
        ),
        regions = listOf(RegionGrowth("Germany", 0.74), RegionGrowth("Netherlands", 0.61), RegionGrowth("Nordics", 0.52)),
        regionMomentum = "Momentum ×2.4",
        catalysts = listOf(
            Catalyst("Agent tooling reached production reliability", 0.84, Evidence.VERIFIED, "12 filings cited"),
            Catalyst("Procurement budgets reallocated from RPA", 0.58, Evidence.SIGNAL, "Hiring and contract data"),
            Catalyst("Compliance frameworks stabilised in EU", 0.37, Evidence.ESTIMATE, "Method shown"),
        ),
        risks = listOf(Risk("Incumbent bundling", 31), Risk("Procurement cycles lengthen", 22), Risk("EU regulatory drag", 17)),
        evidenceMix = mapOf(Evidence.VERIFIED to 34, Evidence.SIGNAL to 41, Evidence.ESTIMATE to 14, Evidence.PREDICTION to 11),
        decision = Decision(
            happening = "Demand is up 41% over 18 months. Automation hiring in DACH has tripled since March.",
            why = "Agent tooling crossed production reliability, and budgets are moving out of legacy RPA contracts.",
            next = "Demand +68% in 12 months, band +52% to +79%, confidence 82%. Competition rises 34%.",
            action = "Enter in Q1 2027, before the window narrows.",
            recommendation = "Commit budget in Q4 2026, launch in Q1 2027, and target Germany and the Netherlands before the Nordics.",
            actNow = 87,
            waitTwoQuarters = 54,
        ),
        verdict = "Enter in Q1 2027.",
    )

    private val energy = Market(
        id = "energy-storage",
        name = "Industrial Energy Storage",
        scope = "EU industrial",
        stage = Stage.EMERGING,
        momentum = "BUILDING",
        probability = 72,
        tone = Tone.NEUTRAL,
        sources = 36,
        signals = "840K",
        updated = "11 MIN AGO",
        marketSize = "$9.6B",
        observed = listOf(78.0, 80.0, 83.0, 85.0, 88.0, 94.0, 100.0),
        horizons = derived(0.41, 0.19, 78, listOf("Watch", "Q2 '27", "Q3 '27", "Before 2029")),
        scenarios = listOf(Scenario("BEAR", 0.14, "$7.9B"), Scenario("BASE", 0.41, "$9.6B", true), Scenario("BULL", 0.73, "$12.1B")),
        regions = listOf(RegionGrowth("Germany", 0.48), RegionGrowth("Spain", 0.44), RegionGrowth("Poland", 0.36)),
        regionMomentum = "Momentum ×1.7",
        catalysts = listOf(
            Catalyst("Battery cell costs fell 18% over two quarters", 0.61, Evidence.VERIFIED, "Supplier price filings"),
            Catalyst("Grid-fee reforms reward peak shaving", 0.44, Evidence.SIGNAL, "Tariff and tender data"),
            Catalyst("Manufacturers electrify process heat", 0.29, Evidence.ESTIMATE, "Method shown"),
        ),
        risks = listOf(Risk("Cell supply tightens", 26), Risk("Subsidy timing slips", 19), Risk("Interest rates stay high", 15)),
        evidenceMix = mapOf(Evidence.VERIFIED to 31, Evidence.SIGNAL to 39, Evidence.ESTIMATE to 18, Evidence.PREDICTION to 12),
        decision = Decision(
            happening = "Industrial storage tenders are up 27% year on year across the EU.",
            why = "Cell costs are falling while grid-fee reforms reward on-site peak shaving.",
            next = "Demand +41% in 12 months, confidence 78%. Competition rises 19%.",
            action = "Build partnerships now and launch in Q3 2027.",
            recommendation = "Secure cell supply in H1 2027 and lead with Germany and Spain, where tariffs reward storage first.",
            actNow = 72,
            waitTwoQuarters = 58,
        ),
        verdict = "Prepare to enter in Q3 2027.",
    )

    private val clinical = Market(
        id = "clinical-ai",
        name = "Clinical Documentation AI",
        scope = "US hospitals",
        stage = Stage.TREND,
        momentum = "SLOWING",
        probability = 48,
        tone = Tone.CAUTION,
        sources = 44,
        signals = "1.6M",
        updated = "2 MIN AGO",
        marketSize = "$6.1B",
        observed = listOf(82.0, 90.0, 96.0, 99.0, 101.0, 100.0, 100.0),
        horizons = derived(0.12, 0.22, 64, listOf("Closing", "Closing", "Closing", "Closed")),
        scenarios = listOf(Scenario("BEAR", -0.04, "$4.9B"), Scenario("BASE", 0.12, "$6.1B", true), Scenario("BULL", 0.31, "$7.4B")),
        regions = listOf(RegionGrowth("US Midwest", 0.18), RegionGrowth("US South", 0.13), RegionGrowth("US West", 0.09)),
        regionMomentum = "Momentum ×0.8",
        catalysts = listOf(
            Catalyst("EHR vendors signal bundled documentation features", 0.72, Evidence.SIGNAL, "Vendor roadmaps and pricing"),
            Catalyst("Clinician burnout keeps budgets open", 0.41, Evidence.VERIFIED, "Hospital workforce filings"),
            Catalyst("Reimbursement rules favour structured notes", 0.22, Evidence.ESTIMATE, "Method shown"),
        ),
        risks = listOf(Risk("EHR bundling at no extra cost", 54), Risk("Hospital budgets freeze", 24), Risk("Liability rulings", 12)),
        evidenceMix = mapOf(Evidence.VERIFIED to 29, Evidence.SIGNAL to 44, Evidence.ESTIMATE to 15, Evidence.PREDICTION to 12),
        decision = Decision(
            happening = "Demand growth has flattened after two years of fast adoption.",
            why = "EHR vendors are signalling bundled documentation features that compress standalone pricing.",
            next = "Demand +12% in 12 months, confidence 64%. Competition rises 22%.",
            action = "Hold. Re-enter only with a specialty the EHR bundle cannot serve.",
            recommendation = "Pause broad hospital sales and test one specialty niche where bundled tools fall short.",
            actNow = 48,
            waitTwoQuarters = 41,
        ),
        verdict = "Moved to Hold.",
    )

    private val packaging = Market(
        id = "circular-packaging",
        name = "Circular Packaging",
        scope = "EU consumer goods",
        stage = Stage.WEAK_SIGNAL,
        momentum = "EARLY",
        probability = 61,
        tone = Tone.NEUTRAL,
        sources = 29,
        signals = "410K",
        updated = "18 MIN AGO",
        marketSize = "$4.3B",
        observed = listOf(86.0, 87.0, 89.0, 90.0, 93.0, 96.0, 100.0),
        horizons = derived(0.29, 0.11, 70, listOf("Too early", "Watch", "2028", "2028–2029")),
        scenarios = listOf(Scenario("BEAR", 0.08, "$3.5B"), Scenario("BASE", 0.29, "$4.3B", true), Scenario("BULL", 0.52, "$5.4B")),
        regions = listOf(RegionGrowth("France", 0.38), RegionGrowth("Germany", 0.31), RegionGrowth("Italy", 0.24)),
        regionMomentum = "Momentum ×1.3",
        catalysts = listOf(
            Catalyst("EU packaging regulation sets reuse targets", 0.52, Evidence.VERIFIED, "Official journal text"),
            Catalyst("Retailers pilot refill formats", 0.33, Evidence.SIGNAL, "Store and hiring data"),
            Catalyst("Recycled resin prices converge with virgin", 0.21, Evidence.ESTIMATE, "Method shown"),
        ),
        risks = listOf(Risk("Mandates delayed", 28), Risk("Consumer uptake stalls", 21), Risk("Logistics costs", 14)),
        evidenceMix = mapOf(Evidence.VERIFIED to 36, Evidence.SIGNAL to 33, Evidence.ESTIMATE to 19, Evidence.PREDICTION to 12),
        decision = Decision(
            happening = "Reuse pilots across EU retail doubled this year from a small base.",
            why = "Regulation now sets reuse targets, and retailers are testing refill formats ahead of them.",
            next = "Demand +29% in 12 months, confidence 70%. Competition rises 11%.",
            action = "Watch, and prepare to enter in 2028.",
            recommendation = "Run one retailer pilot in France in 2027 and scale once mandates are confirmed.",
            actNow = 61,
            waitTwoQuarters = 60,
        ),
        verdict = "Watch until 2028.",
    )

    private val all = listOf(aiWorkflow, energy, clinical, packaging)

    override fun markets() = all
    override fun market(id: String) = all.firstOrNull { it.id == id } ?: aiWorkflow

    override fun alerts() = listOf(
        Alert("a1", AlertCategory.VERDICTS, "VERDICT MOVED ◈", Tone.CAUTION, "TODAY", clinical.id, clinical.name,
            "Moved to Hold. Opportunity probability fell to 48% as EHR vendors signal bundled features.", AlertDestination.MARKET),
        Alert("a2", AlertCategory.VERDICTS, "RE-SCORED", Tone.UP, "TODAY", aiWorkflow.id, aiWorkflow.name,
            "Confidence holds at 82%. Entry window unchanged: Q1–Q2 2027.", AlertDestination.MARKET),
        Alert("a3", AlertCategory.RISKS, "RISK WATCH", Tone.CAUTION, "MON", aiWorkflow.id, aiWorkflow.name,
            "Incumbent bundling is the risk to watch at 31%. The verdict recalculates if it triggers.", AlertDestination.DRIVERS),
        Alert("a4", AlertCategory.VERDICTS, "NEW CATALYST ◦", Tone.NEUTRAL, "SUN", energy.id, energy.name,
            "Battery cell costs fell 18% over two quarters. Weighted 0.61 in the forecast.", AlertDestination.MARKET),
        Alert("a5", AlertCategory.VERDICTS, "FORECAST RESOLVED ●", Tone.UP, "SAT", null, "Agent tooling demand",
            "Called +40% to +55% in 12 months. Actual: +47%, inside the band.", AlertDestination.TRACK_RECORD),
    )

    override fun trackRecord() = TrackRecord(
        landedInBand = 78,
        resolved = 412,
        leadTimeMonths = 5.2,
        withdrawn = 31,
        latest = ResolvedCall("Agent tooling demand rises 40–55% within 12 months.", 0.40, 0.55, 0.47),
    )

    override fun integrations() = listOf(
        Integration("slack", "Slack", "Verdict moves and risk alerts"),
        Integration("notion", "Notion", "Opportunity Reports as pages"),
        Integration("sheets", "Sheets", "Scores synced daily"),
        Integration("api", "REST API", "Scoring engine as a data feed", enterpriseOnly = true),
    )

    override fun recentScores() = listOf(
        RecentScore("Workflow automation · DACH", "ENTER · Q1 2027", 87, Tone.UP),
        RecentScore("Documentation AI for hospitals · US", "HOLD · WINDOW CLOSING", 48, Tone.CAUTION),
    )

    override fun suggestions() = listOf(
        "What markets are about to take off?",
        "Which industries will grow fastest?",
        "When should I enter this market?",
        "What could kill this opportunity?",
        "What will this market look like in 12 months?",
    )

    override fun regions() = listOf("Germany", "Netherlands", "Nordics", "United States", "India", "UK")

    override fun resolve(text: String): Market {
        val t = text.lowercase()
        return when {
            listOf("clinical", "hospital", "health", "documentation", "ehr").any { it in t } -> clinical
            listOf("energy", "battery", "storage", "grid").any { it in t } -> energy
            listOf("packaging", "circular", "refill", "recycl").any { it in t } -> packaging
            else -> aiWorkflow
        }
    }

    override fun scoreIdea(idea: String, horizon: Horizon, regions: Set<String>): IdeaScore {
        val m = resolve(idea)
        val score = m.probability
        val label = when {
            score >= 80 -> "STRONG"
            score >= 65 -> "PROMISING"
            score >= 55 -> "EARLY"
            else -> "UNDER PRESSURE"
        }
        val verdict = when (m.tone) {
            Tone.UP -> "Build it. ${m.verdict}"
            Tone.NEUTRAL -> m.verdict
            Tone.CAUTION -> "Hold. The window is closing."
        }
        val lead = regions.firstOrNull()
        val detail = buildString {
            append("The window is ${m.horizons.getValue(horizon).window} at the ${horizon.label} horizon")
            append(if (m.tone == Tone.CAUTION) ", and competition is rising faster than demand." else " and closes as competition rises.")
            if (lead != null) append(" $lead first")
            val second = regions.drop(1).firstOrNull()
            if (second != null) append(", then $second.") else if (lead != null) append(".")
        }
        return IdeaScore(idea, score, m, horizon, label, verdict, detail)
    }
}
