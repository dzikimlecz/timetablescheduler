package me.dzikimlecz.timetables

import me.dzikimlecz.timetables.components.views.MainView

import tornadofx.launch
import java.io.File

fun main() {
    DefaultPaths.checkPaths()

    launch<App>()
}

class App : tornadofx.App(MainView::class)

enum class DefaultPaths(val value: String?) {
    SAVE("${System.getenv("APPDATA")}\\TimeTableScheduler"),
    EXPORT("${System.getProperty("user.home")}\\Documents\\TimeTableScheduler"),
    SERVER(null),
    ;

    companion object {
        fun checkPaths() = values()
            .mapNotNull { it.value }
            .map { File(it) }
            .forEach { if (!it.exists()) it.mkdirs() }

    }
}

