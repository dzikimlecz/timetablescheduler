package me.dzikimlecz.timetables.components.views

import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.timetables.components.fragments.editors.TimeTableEditor
import me.dzikimlecz.timetables.components.fragments.editors.TimeTableEditor.Companion.ViewMode.EDIT
import me.dzikimlecz.timetables.managers.openDatabasePanel
import me.dzikimlecz.timetables.managers.openTable
import me.dzikimlecz.timetables.managers.setUpTable
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*
import me.dzikimlecz.timetables.components.views.DataBaseControlPanelView as DataBasePanel

// Main scene of the app
class MainView : View(defaultTitle) {

    private val editors = mutableListOf<TimeTableEditor>()

    init {
        addIcons()
        primaryStage.setOnCloseRequest {
            val countOfUnsaved = editors.count { editor -> editor.isUnsaved }
            if (countOfUnsaved != 0) {
                confirm(
                    title = "Niezapisane Plany",
                    header = "Niektóre plany ($countOfUnsaved) są niezapisane!",
                    content = "Zamknąć?",
                    confirmButton = ButtonType.YES,
                    cancelButton = ButtonType.CANCEL,
                    owner = primaryStage,
                ) { return@setOnCloseRequest }
                it.consume()
            }
        }
    }

    override val root: BorderPane =
        borderpane {
            left = vbox {
                button("Nowy Plan")
                    .setOnAction { setUpTable() }
                button("Otwórz Plan")
                    .setOnAction { openTable() }
                button("Otwórz Bazę Godzin")
                    .setOnAction { openDatabasePanel() }
                style()
            }
            center = tabpane {
                bindWindowAndSelectedTabTitle()
            }
        }

    override fun onBeforeShow() {
        super.onBeforeShow()
        setWindowMinSize(800, 400)
        primaryStage.isMaximized = true
    }

    fun displayTable(table: TimeTable, displayEditing: Boolean) =
        Tab().apply {
            injectEditorOf(table, displayEditing)
            addToTabs()
            select()
        }


    fun showDataBaseControlPane(panelProvider: () -> DataBasePanel) =
        selectOrCreateTab("databaseControlPanel", "Baza Planów") {
            panelProvider().root
        }


    fun displayLecturersWorkTime(items: Collection<Lecturer>) =
        selectOrCreateTab("lecturersWorkTime", "Czasy Pracy") {
            find<LecturerWorkTimeDisplay>().apply { refresh(items) }.root
        }

    // includes icon of the program in 5 different resolutions.
    private fun addIcons() {
        primaryStage.icons.addAll(
            Image("scheduler512.png"),
            Image("scheduler256.png"),
            Image("scheduler128.png"),
            Image("scheduler64.png"),
            Image("scheduler32.png"),
        )
    }

    private fun VBox.style() {
        setPrefSizeForButtons(180.0, 50.0)
        style { backgroundColor.add(Color.LIGHTGREY) }
        paddingTop = 15
        spacing = 30.0
    }

    private fun VBox.setPrefSizeForButtons(width: Double, height: Double) {
        children.filterIsInstance<Button>().forEach {
            it.prefWidth = width
            it.prefHeight = height
        }
    }

    private fun TabPane.bindWindowAndSelectedTabTitle() {
        selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            title = newValue?.text ?: defaultTitle
        }
    }

    private fun selectOrCreateTab(id: String, name: String, contentProducer: () -> Node) {
        val tab: Tab = findTab(id)
            ?: createTab(id, name, contentProducer).addToTabs()
        tab.select()
    }

    private fun findTab(id: String): Tab? =
        applyOnTabPane { tabs.firstOrNull { it.id == id } }

    private fun Tab.injectEditorOf(table: TimeTable, displayEditing: Boolean) {
        val editor = find<TimeTableEditor>(params = mapOf(
            TimeTableEditor::timeTable to table,
            TimeTableEditor::tab to this
        ))
        if (displayEditing) editor.viewMode = EDIT
        editors += editor
        setOnCloseRequest {
            if (editor.isUnsaved) {
                confirm(
                    title = "Niezapisany Plan",
                    header = "Plan nie jest zapisany!",
                    content = "Zamknąć?",
                    confirmButton = ButtonType.YES,
                    cancelButton = ButtonType.CANCEL,
                    owner = primaryStage,
                ) { return@setOnCloseRequest }
                it.consume()
            }
        }
        setOnClosed { editors -= editor }
    }

    private fun Tab.addToTabs(): Tab =
        applyOnTabPane {
            tabs += this@addToTabs
            this@addToTabs
        }

    private fun createTab(id: String, name: String, contentProducer: () -> Node): Tab =
        Tab().apply {
            content = contentProducer()
            this.id = id
            text = name
        }

    private inline fun <R> applyOnTabPane(applied: (TabPane.() -> R)): R =
        (root.center as TabPane).applied()

    companion object {
        private const val defaultTitle = "Tabelki"
    }
}
