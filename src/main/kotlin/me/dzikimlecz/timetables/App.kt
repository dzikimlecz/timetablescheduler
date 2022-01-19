package me.dzikimlecz.timetables

import me.dzikimlecz.timetables.components.views.MainView

import tornadofx.launch
import java.io.File
import java.net.ConnectException

fun main() {
    checkPaths()
    initServer()
    launch<App>()
}

class App : tornadofx.App(MainView::class)

enum class DefaultPaths(val value: String?, val isDirectory: Boolean) {
    SAVE("${System.getenv("APPDATA")}\\Plany", true),
    EXPORT("${System.getProperty("user.home")}\\Documents\\Plany", true),
    SERVER_EXECUTABLE("${System.getenv("APPDATA")}\\TabelkiSerwer\\TabelkiSerwer.exe", false),
    SERVER_ADDRESS("http://localhost:8080/timetableapi", false),
}

// Creates folders, with paths specified in DefaultPaths enum, if they don't exist
private fun checkPaths() =
    DefaultPaths.values()
        // not all paths are directories. some are files, some are web urls
        .filter(DefaultPaths::isDirectory)
        // path may be null (temporary lack of the implementation)
        .mapNotNull { it.value }
        .map { File(it) }
        .forEach { if (!it.exists()) it.mkdirs() }

// starts up TimeTable server if it's off
private fun initServer(): Boolean {
    val file = File(DefaultPaths.SERVER_EXECUTABLE.value ?: return false)
    return try {
        khttp.get(DefaultPaths.SERVER_ADDRESS.value ?: return false)
        true
    } catch (e: ConnectException) {
        // runs server's executable if connecting to it wasn't possible.
        file.execute()
    }
}

// executes file if it's possible returns true if exe was started. false otherwise.
private fun File.execute(): Boolean =
    if (canExecute()) {
        Runtime.getRuntime().exec(path)
        true
    } else false
