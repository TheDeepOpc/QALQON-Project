package com.qalqon.security.monitor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceClassifierTest {
    @Test
    fun marksTelegramAsUntrusted() {
        val verdict = SourceClassifier.classify("/storage/emulated/0/Telegram/evil.apk")
        assertFalse(verdict.trusted)
    }

    @Test
    fun marksPlayAsTrusted() {
        val verdict = SourceClassifier.classify("/data/data/com.android.vending/cache/app.apk")
        assertTrue(verdict.trusted)
    }
}
