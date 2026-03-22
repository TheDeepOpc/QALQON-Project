package com.qalqon.security.monitor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkRiskAnalyzerTest {
    @Test
    fun flagsShortenerAsRisky() {
        val (risky, _) = LinkRiskAnalyzer.analyze("https://bit.ly/demo")
        assertTrue(risky)
    }

    @Test
    fun acceptsNormalHttps() {
        val (risky, _) = LinkRiskAnalyzer.analyze("https://developer.android.com")
        assertFalse(risky)
    }
}
