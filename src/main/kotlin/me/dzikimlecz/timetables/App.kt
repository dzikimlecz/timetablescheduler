package me.dzikimlecz.timetables

import me.dzikimlecz.timetables.components.views.MainView

import tornadofx.launch
import java.io.File

fun main() {
    System.setProperty("defaultSavePath", "${System.getenv("APPDATA")}\\TimeTableScheduler")
    var file = File(System.getProperty("defaultSavePath"))
    if (!file.exists()) file.mkdirs()
    System.setProperty("defaultExportPath",
        "${System.getProperty("user.home")}\\Documents\\TimeTableScheduler")
    file = File(System.getProperty("defaultExportPath"))
    if (!file.exists()) file.mkdirs()
    launch<App>()
}

class App : tornadofx.App(MainView::class)

