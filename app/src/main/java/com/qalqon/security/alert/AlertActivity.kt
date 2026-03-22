package com.qalqon.security.alert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.qalqon.security.QalqonApp
import com.qalqon.security.ui.theme.QalqonTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AlertActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        val apkPath = intent.getStringExtra(EXTRA_APK_PATH).orEmpty()
        val source = intent.getStringExtra(EXTRA_SOURCE).orEmpty()

        setContent {
            QalqonTheme {
                val scope = rememberCoroutineScope()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF140B0B))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Security Warning",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFFF8A80),
                    )
                    Text(
                        text = "This APK was downloaded from an untrusted source and may contain malware. It is recommended to remove it before installation.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                    Text(
                        text = "Source: $source\nFile: $apkPath",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFCCBC),
                    )
                    Button(
                        onClick = {
                            val deleted = File(apkPath).delete()
                            scope.launch(Dispatchers.IO) {
                                (application as QalqonApp).repository.markAction(
                                    eventId,
                                    if (deleted) "DELETED" else "DELETE_FAILED",
                                )
                            }
                            finish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Delete APK")
                    }
                    OutlinedButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                (application as QalqonApp).repository.markAction(eventId, "KEPT_ANYWAY")
                            }
                            finish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Keep Anyway")
                    }
                    OutlinedButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                (application as QalqonApp).repository.markAction(eventId, "MORE_INFO_OPENED")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("More Info")
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_EVENT_ID = "extra_event_id"
        private const val EXTRA_APK_PATH = "extra_apk_path"
        private const val EXTRA_SOURCE = "extra_source"

        fun createIntent(context: Context, eventId: Long, apkPath: String, source: String): Intent {
            return Intent(context, AlertActivity::class.java).apply {
                putExtra(EXTRA_EVENT_ID, eventId)
                putExtra(EXTRA_APK_PATH, apkPath)
                putExtra(EXTRA_SOURCE, source)
            }
        }
    }
}
