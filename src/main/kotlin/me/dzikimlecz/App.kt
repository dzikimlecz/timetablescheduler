package me.dzikimlecz

import java.util.Properties
import javafx.application.Application
import javafx.stage.Stage

fun main() {
    Application.launch(App().javaClass)
}

class App : Application() {
    private val properties = Properties()
    private lateinit var manager: ScenesManager

    override fun start(stage: Stage) {
        manager = ScenesManager(stage)
        manager.showTimeTableSetUp(properties)
    }
}

