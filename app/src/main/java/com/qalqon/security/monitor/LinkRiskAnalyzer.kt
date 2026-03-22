package com.qalqon.security.monitor

import java.util.Locale

object LinkRiskAnalyzer {
    private val riskyTerms = listOf(
        "bit.ly",
        "tinyurl",
        "apkfree",
        "modapk",
        "crack",
        "freegift",
        "login-verify",
        "update-now",
    )

    fun analyze(url: String): Pair<Boolean, String> {
        val normalized = url.lowercase(Locale.US).trim()
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            return true to "URL format is unusual"
        }
        val risky = riskyTerms.firstOrNull { normalized.contains(it) }
        return if (risky != null) {
            true to "Suspicious token found: $risky"
        } else {
            false to "No obvious offline risk indicators found"
        }
    }
}
