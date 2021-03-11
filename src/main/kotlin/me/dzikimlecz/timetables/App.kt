package me.dzikimlecz.timetables

import me.dzikimlecz.timetables.components.views.MainView

import tornadofx.launch

fun main() {
    System.setProperty("defaultSavePath", "${System.getenv("APPDATA")}\\TimeTableScheduler")
    launch<App>()
}

class App : tornadofx.App(MainView::class)

