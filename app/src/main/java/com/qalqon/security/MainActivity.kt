package com.qalqon.security

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qalqon.security.data.local.SecurityEvent
import com.qalqon.security.monitor.ApkMonitorService
import com.qalqon.security.monitor.LinkRiskAnalyzer
import com.qalqon.security.settings.MonitoringSettings
import com.qalqon.security.settings.UserPreferences
import com.qalqon.security.ui.theme.QalqonTheme
import com.qalqon.security.ui.viewmodel.MainViewModel
import com.qalqon.security.ui.viewmodel.MainViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("qalqon_runtime", MODE_PRIVATE)
    }

    private val viewModel by viewModels<MainViewModel> {
        val app = application as QalqonApp
        MainViewModelFactory(app.repository, UserPreferences(this))
    }

    private val postNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showPermissionIntroIfNeeded()
        if (MonitoringSettings(this).isMonitoringEnabled()) {
            ApkMonitorService.start(this)
        }

        val sharedText = if (intent?.action == Intent.ACTION_SEND) {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }

        setContent {
            val darkMode by viewModel.darkModeEnabled.collectAsStateWithLifecycle()
            val monitoringEnabled by viewModel.monitoringEnabled.collectAsStateWithLifecycle()
            val alertsEnabled by viewModel.alertsEnabled.collectAsStateWithLifecycle()

            QalqonTheme(darkTheme = darkMode) {
                val events by viewModel.events.collectAsStateWithLifecycle()
                MainRoot(
                    events = events,
                    darkMode = darkMode,
                    monitoringEnabled = monitoringEnabled,
                    alertsEnabled = alertsEnabled,
                    sharedText = sharedText,
                    onDarkModeChanged = viewModel::setDarkMode,
                    onMonitoringChanged = { enabled ->
                        viewModel.setMonitoringEnabled(enabled)
                        if (enabled) {
                            ApkMonitorService.start(this)
                        } else {
                            stopService(Intent(this, ApkMonitorService::class.java))
                        }
                    },
                    onAlertsChanged = viewModel::setAlertsEnabled,
                    onOpenPermissionManager = ::openPermissionManager,
                    onLinkScanned = viewModel::logLinkEvent,
                )
            }
        }
    }

    private fun requestRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            openPermissionManager()
        }
    }

    private fun showPermissionIntroIfNeeded() {
        val key = "permission_intro_shown"
        if (prefs.getBoolean(key, false)) {
            requestRuntimePermissions()
            return
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("Qalqon permissions")
            .setMessage(
                "Qalqon needs storage and notification access to detect risky APK files right after download, keep 24/7 monitoring active, and show immediate security alerts offline.",
            )
            .setCancelable(false)
            .setPositiveButton("Continue") { _, _ ->
                prefs.edit().putBoolean(key, true).apply()
                requestRuntimePermissions()
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun openPermissionManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } else {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }
}

@Composable
private fun MainRoot(
    events: List<SecurityEvent>,
    darkMode: Boolean,
    monitoringEnabled: Boolean,
    alertsEnabled: Boolean,
    sharedText: String?,
    onDarkModeChanged: (Boolean) -> Unit,
    onMonitoringChanged: (Boolean) -> Unit,
    onAlertsChanged: (Boolean) -> Unit,
    onOpenPermissionManager: () -> Unit,
    onLinkScanned: (String, Boolean, String) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf("dashboard") }
    var linkText by rememberSaveable { mutableStateOf(sharedText.orEmpty()) }
    var linkResult by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrBlank()) {
            selectedTab = "links"
            linkText = sharedText
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "dashboard",
                    onClick = { selectedTab = "dashboard" },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Dashboard") },
                    label = { Text("Monitor") },
                )
                NavigationBarItem(
                    selected = selectedTab == "reports",
                    onClick = { selectedTab = "reports" },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Reports") },
                    label = { Text("Reports") },
                )
                NavigationBarItem(
                    selected = selectedTab == "links",
                    onClick = { selectedTab = "links" },
                    icon = { Icon(Icons.Default.Link, contentDescription = "Links") },
                    label = { Text("Link Scan") },
                )
                NavigationBarItem(
                    selected = selectedTab == "settings",
                    onClick = { selectedTab = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                )
            }
        },
    ) { padding ->
        when (selectedTab) {
            "dashboard" -> DashboardScreen(
                monitoringEnabled = monitoringEnabled,
                modifier = Modifier.padding(padding),
            )

            "reports" -> ReportsScreen(events = events, modifier = Modifier.padding(padding))
            "links" -> LinkScannerScreen(
                text = linkText,
                result = linkResult,
                onTextChange = { linkText = it },
                onScan = {
                    val (risky, reason) = LinkRiskAnalyzer.analyze(linkText)
                    onLinkScanned(linkText, risky, reason)
                    linkResult = if (risky) {
                        "Warning: This link may be unsafe. Proceed carefully. ($reason)"
                    } else {
                        "Link looks normal in offline checks. ($reason)"
                    }
                },
                modifier = Modifier.padding(padding),
            )

            "settings" -> SettingsScreen(
                darkMode = darkMode,
                monitoringEnabled = monitoringEnabled,
                alertsEnabled = alertsEnabled,
                onDarkModeChanged = onDarkModeChanged,
                onMonitoringChanged = onMonitoringChanged,
                onAlertsChanged = onAlertsChanged,
                onOpenPermissionManager = onOpenPermissionManager,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun DashboardScreen(monitoringEnabled: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Monitoring Status: ${if (monitoringEnabled) "ACTIVE" else "PAUSED"}")
                Text(if (monitoringEnabled) "24/7 offline monitor is running." else "Monitoring is paused by user.")
                Text("Watching Downloads, Telegram, shared and common transfer folders.")
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Protection Logic")
                Text("Red: danger, Orange: warning, Green: safe, Blue/Gray: system info.")
                Text("Untrusted APKs trigger full-screen warning immediately after detection.")
            }
        }
    }
}

@Composable
private fun ReportsScreen(events: List<SecurityEvent>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        item {
            Text("Security Reports", modifier = Modifier.padding(vertical = 14.dp))
        }
        if (events.isEmpty()) {
            item { Text("No events yet.") }
        } else {
            items(events) { event ->
                ReportItem(event = event)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun ReportItem(event: SecurityEvent) {
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(event.createdAt))
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("${event.eventType} | ${event.severity}")
        Text(event.message)
        Text("Source: ${event.sourceLabel}")
        if (!event.filePath.isNullOrBlank()) Text("Path: ${event.filePath}")
        Text("Action: ${event.userAction} | $date")
    }
}

@Composable
private fun LinkScannerScreen(
    text: String,
    result: String?,
    onTextChange: (String) -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Suspicious Link Warning")
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text("Paste URL") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = onScan, modifier = Modifier.fillMaxWidth()) {
            Text("Analyze Link")
        }
        if (!result.isNullOrBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(result, modifier = Modifier.padding(12.dp))
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    darkMode: Boolean,
    monitoringEnabled: Boolean,
    alertsEnabled: Boolean,
    onDarkModeChanged: (Boolean) -> Unit,
    onMonitoringChanged: (Boolean) -> Unit,
    onAlertsChanged: (Boolean) -> Unit,
    onOpenPermissionManager: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Theme")
                Switch(checked = darkMode, onCheckedChange = onDarkModeChanged)
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Monitoring")
                Switch(checked = monitoringEnabled, onCheckedChange = onMonitoringChanged)
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Alerts")
                Switch(checked = alertsEnabled, onCheckedChange = onAlertsChanged)
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Permissions")
                OutlinedButton(onClick = onOpenPermissionManager, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Permission Management")
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Monitoring Status")
                Text("Foreground monitor should remain active for instant APK detection.")
            }
        }
    }
}
