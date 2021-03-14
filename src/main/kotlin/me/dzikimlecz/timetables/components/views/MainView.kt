package me.dzikimlecz.timetables.components.views

import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.paint.Color
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor
import me.dzikimlecz.timetables.managers.Manager
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*


private const val defaultTitle = "Układacz planów 3tysionce !!!"

class MainView : View(defaultTitle) {
    val manager by lazy { Manager() }

    override val root = borderpane {
        left {
            vbox {
                spacing = 3E1
                background = Background(BackgroundFill(Color.LIGHTGREY, null, null))
                paddingTop = 15
                button("Nowy Plan").setOnAction { manager.setUpTable() }
                button("Otwórz Plan").setOnAction { manager.importTable() }
                button("Dodaj Plan do Bazy Godzin").setOnAction { manager.includeTableToDB() }
                button("Otwórz Bazę Godzin").setOnAction { manager.openDB() }
                children.forEach {
                    if (it is Button) {
                        it.prefWidth = 1.8E2
                        it.prefHeight = 5E1
                    }
                }
            }
        }
    }

    fun displayTable(table: TimeTable?) {
        root.center = if (table != null){
            val editor = find<TimeTableEditor>(mapOf(TimeTableEditor::timeTable to table))
            title = table.name
            editor.root
        } else {
            title = defaultTitle
            null
        }
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        setWindowMinSize(800, 500)
    }
}
