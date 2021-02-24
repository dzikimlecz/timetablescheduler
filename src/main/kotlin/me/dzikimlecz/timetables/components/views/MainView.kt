package me.dzikimlecz.timetables.components.views

import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor
import me.dzikimlecz.timetables.managers.Manager
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*
import me.dzikimlecz.timetables.components.views.TimeTableSetUpView as SetUpView


class MainView : View("Układacz planów 3tysionce !!!") {
    private val manager : Manager by lazy { Manager() }

    override val root = borderpane {
        left {
            vbox {
                spacing = 3E1
                background = Background(BackgroundFill(Color.LIGHTGREY, null, null))
                padding = Insets(1.5E1, 0.0, 0.0, 0.0)
                val buttonWidth = 1.8E2
                val buttonHeight = 5E1
                button("Nowy Plan") {
                    action { setUpTable() }
                }
                button("Otwórz Plan")
                button("Zapisz Plan")
                button("Dodaj Plan do Bazy Godzin")
                button("Otwórz Bazę Godzin")
                children.forEach {
                    if (it is Button) {
                        it.prefWidth = buttonWidth
                        it.prefHeight = buttonHeight
                    }
                }
            }
        }
    }

    private fun setUpTable() {
        val map = mutableMapOf<String, String>()
        find<SetUpView>(mapOf(SetUpView::tableProperties to map))
            .openModal(StageStyle.UTILITY, resizable = false, block = true)
        try { displayTable(manager.newTable(map)) } catch (ignore : Exception) {}
    }

    private fun displayTable(table: TimeTable) {
        val editor = find<TimeTableEditor>(mapOf(TimeTableEditor::timeTable to table))
        root.center = editor.root
        title = table.name
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        setWindowMinSize(800, 500)
    }
}
