package me.dzikimlecz.timetables.managers

import javafx.stage.Stage
import javafx.stage.StageStyle
import me.dzikimlecz.timetables.components.scenes.TimeTableSetUpScene
import java.util.concurrent.atomic.AtomicInteger

class WindowsManager(private val mainStage: Stage) {
    init {
        mainStage.title = "Układacz planów 3tysionce !!!"
        mainStage.minHeight = 300.0
        mainStage.minWidth = 400.0
    }

    private val dialogStage = Stage()
    init {
        dialogStage.isResizable = false
        dialogStage.initStyle(StageStyle.UTILITY)
        dialogStage.initOwner(mainStage)
    }

    fun showMainMenu() {

    }

    fun showTimeTableSetUp(dimensionsContainer: Pair<AtomicInteger, AtomicInteger>) {
        dialogStage.title = "Nowy Plan"
        val timeTableSetUpScene = TimeTableSetUpScene(dimensionsContainer)
        dialogStage.scene = timeTableSetUpScene
        dialogStage.sizeToScene()
        dialogStage.centerOnScreen()
        dialogStage.showAndWait()
    }


}
