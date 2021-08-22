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
    SERVER_ADDRESS("http://localhost:8080/timetableapi/", false),
}

private fun checkPaths() = DefaultPaths.values()
    .filter(DefaultPaths::isDirectory)
    .mapNotNull { it.value }
    .map { File(it) }
    .forEach { if (!it.exists()) it.mkdirs() }

private fun initServer(): Boolean {
    val file = File(DefaultPaths.SERVER_EXECUTABLE.value ?: return false)
    return try {
        khttp.get(DefaultPaths.SERVER_ADDRESS.value ?: return false)
        true
    } catch (e: ConnectException) {
        file.execute()
    }
}

private fun File.execute(): Boolean {
    val canExecute = canExecute()
    if (canExecute) Runtime.getRuntime().exec(path)
    return canExecute
}

