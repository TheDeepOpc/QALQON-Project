package com.qalqon.security.monitor

import android.os.FileObserver
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class FileMonitorManager(
    private val onApkDetected: (String, String) -> Unit,
) {
    private val observers = ConcurrentHashMap<String, FileObserver>()

    fun start() {
        FileMonitorTargets.targets().forEach { directory ->
            watchRecursively(directory, maxDepth = 2)
        }
    }

    fun stop() {
        observers.values.forEach { it.stopWatching() }
        observers.clear()
    }

    private fun watchRecursively(directory: File, maxDepth: Int) {
        if (!directory.exists() || !directory.isDirectory) return
        attachObserver(directory)
        if (maxDepth <= 0) return
        directory.listFiles()
            ?.asSequence()
            ?.filter { it.isDirectory }
            ?.forEach { watchRecursively(it, maxDepth - 1) }
    }

    private fun attachObserver(directory: File) {
        if (observers.containsKey(directory.absolutePath)) return
        val observer = object : FileObserver(
            directory.absolutePath,
            CREATE or MOVED_TO or CLOSE_WRITE,
        ) {
            override fun onEvent(event: Int, path: String?) {
                if (path.isNullOrBlank()) return
                if (!path.endsWith(".apk", ignoreCase = true)) return
                val absolute = File(directory, path).absolutePath
                onApkDetected(absolute, "FILE_OBSERVER")
            }
        }
        observer.startWatching()
        observers[directory.absolutePath] = observer
    }
}
