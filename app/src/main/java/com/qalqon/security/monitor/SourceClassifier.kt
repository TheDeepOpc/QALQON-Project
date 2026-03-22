package com.qalqon.security.monitor

import java.io.File
import java.util.Locale

data class SourceVerdict(
    val sourceLabel: String,
    val trusted: Boolean,
)

object SourceClassifier {
    fun classify(filePath: String): SourceVerdict {
        val normalized = filePath.lowercase(Locale.US)
        return when {
            normalized.contains("com.android.vending") || normalized.contains("play") -> {
                SourceVerdict("Google Play (system)", trusted = true)
            }

            normalized.contains("telegram") -> SourceVerdict("Telegram", trusted = false)
            normalized.contains("download") -> SourceVerdict("Browser/Downloads", trusted = false)
            normalized.contains("bluetooth") -> SourceVerdict("Bluetooth transfer", trusted = false)
            normalized.contains("share") -> SourceVerdict("Shared files", trusted = false)
            else -> {
                val parentName = File(filePath).parentFile?.name ?: "Unknown source"
                SourceVerdict(parentName, trusted = false)
            }
        }
    }
}
