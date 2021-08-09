package me.dzikimlecz.timetables

import me.dzikimlecz.timetables.components.views.MainView

import tornadofx.launch
import java.io.File

fun main() {
    DefaultPaths.checkPaths()

    launch<App>()
}

class App : tornadofx.App(MainView::class)

enum class DefaultPaths(val value: String?, val isDirectory: Boolean) {
    SAVE("${System.getenv("APPDATA")}\\TimeTableScheduler", true),
    EXPORT("${System.getProperty("user.home")}\\Documents\\TimeTableScheduler", true),
    SERVER_EXECUTABLE(null, false),
    SERVER_ADDRESS("http://localhost:8080/timetableapi/", false),
    ;

    companion object {
        fun checkPaths() = values()
            .filter(DefaultPaths::isDirectory)
            .mapNotNull { it.value }
            .map { File(it) }
            .forEach { if (!it.exists()) it.mkdirs() }

    }
}

