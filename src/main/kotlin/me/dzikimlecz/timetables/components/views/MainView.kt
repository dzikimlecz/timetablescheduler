package me.dzikimlecz.timetables.components.views

import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor
import me.dzikimlecz.timetables.managers.Manager
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*
import me.dzikimlecz.timetables.components.views.DataBaseControlPanelView as DataBasePanel

private const val defaultTitle = "Układacz planów 3tysionce !!!"

class MainView : View(defaultTitle) {
    val manager: Manager by lazy { Manager() }

    override val root: BorderPane = borderpane {
        left = vbox {
            spacing = 3E1
            background = Background(BackgroundFill(Color.LIGHTGREY, null, null))
            paddingTop = 15
            button("Nowy Plan").setOnAction { manager.setUpTable() }
            button("Otwórz Plan").setOnAction { manager.openTable() }
            button("Otwórz Bazę Godzin").setOnAction { manager.openDB() }
            children.filterIsInstance<Button>().forEach {
                it.prefWidth = 1.8E2
                it.prefHeight = 5E1
            }
        }
        center = tabpane {
            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                title = newValue?.text ?: defaultTitle
            }
        }

    }

    fun displayTable(table: TimeTable) = with(root.center as TabPane) {
        val tab = Tab()
        find<TimeTableEditor>(mapOf(TimeTableEditor::timeTable to table, TimeTableEditor::tab to tab))
        tabs += tab
        selectionModel.select(tab)
    }

    fun showDataBaseControlPane(panelProvider: () -> DataBasePanel) =
        injectSingletonTab("databaseControlPanel", "Baza Planów") {
            panelProvider().root
        }

    override fun onBeforeShow() {
        super.onBeforeShow()
        setWindowMinSize(800, 400)
        primaryStage.isMaximized = true
    }

    fun displayLecturersWorkTime(items: Collection<Lecturer>) =
        injectSingletonTab("lecturersWorkTime", "Czasy Pracy") {
            val panel = find<LecturerWorkTimeDisplay>()
            panel.refresh(items)
            panel.root
        }

    private fun injectSingletonTab(id: String, name: String, contentProducer: () -> Node)  =
        with(root.center as TabPane) {
            val tab = tabs.firstOrNull { it.id == id }
                ?: Tab().apply {
                    content = contentProducer()
                    this.id = id
                    text = name
                    tabs += this
                }
            selectionModel.select(tab)
        }
}
