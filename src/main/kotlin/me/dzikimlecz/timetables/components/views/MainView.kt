package me.dzikimlecz.timetables.components.views

import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.paint.Color
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor
import me.dzikimlecz.timetables.managers.Manager
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*


class MainView : View("Układacz planów 3tysionce !!!") {
    private val manager : Manager by lazy { Manager() }

    override val root = borderpane {
        left {
            vbox {
                spacing = 3E1
                background = Background(BackgroundFill(Color.LIGHTGREY, null, null))
                paddingTop = 15
                val buttonWidth = 1.8E2
                val buttonHeight = 5E1
                button("Nowy Plan").setOnAction { manager.setUpTable() }
                button("Otwórz Plan").setOnAction { manager.importTable() }
                button("Zapisz Plan").setOnAction { manager.exportTable() }
                button("Dodaj Plan do Bazy Godzin").setOnAction {  }
                button("Otwórz Bazę Godzin").setOnAction {  }
                children.forEach {
                    if (it is Button) {
                        it.prefWidth = buttonWidth
                        it.prefHeight = buttonHeight
                    }
                }
            }
        }
    }

    fun displayTable(table: TimeTable) {
        val editor = find<TimeTableEditor>(mapOf(TimeTableEditor::timeTable to table))
        root.center = editor.root
        title = table.name
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        setWindowMinSize(800, 500)
    }
}
