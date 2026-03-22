package com.qalqon.security.monitor

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

data class ApkTrustVerdict(
    val sourceLabel: String,
    val trusted: Boolean,
    val reason: String,
)

object ApkTrustAnalyzer {
    fun analyze(context: Context, apkPath: String): ApkTrustVerdict {
        val sourceVerdict = SourceClassifier.classify(apkPath)
        if (sourceVerdict.trusted) {
            return ApkTrustVerdict(
                sourceLabel = sourceVerdict.sourceLabel,
                trusted = true,
                reason = "Path is marked as trusted source",
            )
        }

        val archiveInfo = getArchivePackageInfo(context, apkPath)
        val packageName = archiveInfo?.packageName
        if (packageName.isNullOrBlank()) {
            return ApkTrustVerdict(sourceVerdict.sourceLabel, false, "Package metadata not readable")
        }

        val installedInfo = getInstalledPackageInfo(context.packageManager, packageName) ?: return ApkTrustVerdict(
            sourceLabel = sourceVerdict.sourceLabel,
            trusted = false,
            reason = "Package is not installed on device",
        )

        val archiveSignatures = signatureDigests(archiveInfo)
        val installedSignatures = signatureDigests(installedInfo)
        val trustedBySignature = archiveSignatures.isNotEmpty() && archiveSignatures == installedSignatures
        return if (trustedBySignature) {
            ApkTrustVerdict(
                sourceLabel = "${sourceVerdict.sourceLabel} (known app update)",
                trusted = true,
                reason = "Signing certificate matches installed package",
            )
        } else {
            ApkTrustVerdict(
                sourceLabel = sourceVerdict.sourceLabel,
                trusted = false,
                reason = "Signature mismatch with installed package",
            )
        }
    }

    private fun getArchivePackageInfo(context: Context, apkPath: String): PackageInfo? {
        val packageManager = context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageArchiveInfo(
                apkPath,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNATURES)
        }
    }

    private fun getInstalledPackageInfo(packageManager: PackageManager, packageName: String): PackageInfo? {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }
        }.getOrNull()
    }

    private fun signatureDigests(packageInfo: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = packageInfo.signingInfo ?: return emptySet()
            if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
        } else {
            @Suppress("DEPRECATION")
            packageInfo.signatures
        } ?: return emptySet()
        return signatures.map { digestSha256(it.toByteArray()) }.toSet()
    }

    private fun digestSha256(input: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(input)
            .joinToString("") { byte -> "%02x".format(byte) }
    }
}
