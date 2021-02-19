package me.dzikimlecz.timetables

import me.dzikimlecz.timetables.components.views.MainView
import me.dzikimlecz.timetables.components.views.TimeTableSetUpView
import tornadofx.launch
import tornadofx.App as Application

fun main() = launch<App>()

class App : Application(MainView::class)

