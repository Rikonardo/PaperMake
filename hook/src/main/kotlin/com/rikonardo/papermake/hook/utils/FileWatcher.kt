package com.rikonardo.papermake.hook.utils

import java.io.File
import java.nio.file.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class FileWatcher(private val file: File, private val callback: () -> Unit) : Thread() {
    val stop = AtomicBoolean(false)

    override fun run() {
        try {
            FileSystems.getDefault().newWatchService().use { watcher ->
                val path = file.toPath().parent
                path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
                while (!stop.get()) {
                    val key: WatchKey? = try {
                        watcher.poll(25, TimeUnit.MILLISECONDS)
                    } catch (e: InterruptedException) {
                        return
                    }
                    if (key == null) {
                        yield()
                        continue
                    }
                    for (event in key.pollEvents()) {
                        val kind = event.kind()
                        val ev = event as WatchEvent<*>
                        val filename = ev.context()
                        if (kind === StandardWatchEventKinds.OVERFLOW) {
                            yield()
                            continue
                        } else if (kind === StandardWatchEventKinds.ENTRY_MODIFY && filename.toString() == file.name
                        ) {
                            callback()
                        }
                        val valid = key.reset()
                        if (!valid) {
                            break
                        }
                    }
                    yield()
                }
            }
        } catch (_: Exception) {
            // ignore
        }
    }
}
