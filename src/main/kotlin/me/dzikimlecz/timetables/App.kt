package me.dzikimlecz.timetables

import me.dzikimlecz.timetables.components.views.MainView
import me.dzikimlecz.timetables.components.views.TimeTableSetUpView
import tornadofx.launch
import tornadofx.App as Application

fun main() {
    System.setProperty("defaultSavePath", "${System.getenv("APPDATA")}\\TimeTableScheduler")
    launch<App>()
}

class App : Application(MainView::class)

