package me.dzikimlecz.timetables.components.views

import javafx.scene.control.Button
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.paint.Color
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor
import me.dzikimlecz.timetables.managers.Manager
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*
import me.dzikimlecz.timetables.components.views.DataBaseControlPanelView as DataBasePanel

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

    fun displayTable(table: TimeTable) = with(root) {
        if (center !is TabPane) center = tabpane {
            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                title = newValue?.text ?: defaultTitle
            }
        }
        with(center as TabPane) {
            val tab = Tab()
            find<TimeTableEditor>(mapOf(TimeTableEditor::timeTable to table, TimeTableEditor::tab to tab))
            tabs += tab
            selectionModel.select(tab)
        }
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        setWindowMinSize(800, 400)
        primaryStage.isMaximized = true
    }

    fun showDataBaseControlPane(lecturers: List<Lecturer>, tables: List<TimeTable>) {
        find<DataBasePanel>(params = mapOf(
            DataBasePanel::lecturers to lecturers,
            DataBasePanel::tables to tables,
        )).also {
            root.center = it.root
        }
    }
}
