package me.dzikimlecz.timetables.components.fragments.toolbars

import javafx.application.Platform.runLater
import javafx.geometry.Orientation
import javafx.scene.control.ChoiceBox
import me.dzikimlecz.timetables.components.fragments.editors.TimeTableEditor
import me.dzikimlecz.timetables.components.fragments.toolbars.EditToolBar.Companion.ApplicableTo.*
import me.dzikimlecz.timetables.components.fragments.toolbars.EditToolBar.Companion.ChangedValue.*
import me.dzikimlecz.timetables.components.fragments.toolbars.EditToolBar.Companion.ChangedValue.Companion.toChangedValue
import tornadofx.*

class EditToolBar : TimeTableEditorToolBar() {

    override val root = toolbar {
        button("Zatwierdź")
            .setOnAction { parentEditor.viewMode = TimeTableEditor.Companion.ViewMode.VIEW }
        separator()
        this += pane(ROW) {
            when (value.toChangedValue()) {
                ADD -> parentEditor.timeTable.rows++
                REMOVE -> parentEditor.timeTable.rows--
                CLEAN -> parentEditor.cleanRows()
                DETAILS -> showDetails()
                else -> {}
            }
        }
        this += pane(COLUMN) {
            when (value.toChangedValue()) {
                ADD -> parentEditor.timeTable.columns++
                REMOVE -> parentEditor.timeTable.columns--
                CLEAN -> parentEditor.cleanColumns()
                DETAILS -> showDetails()
                TIME_SPANS -> parentEditor.adjustTimeSpans()
                TITLE -> parentEditor.adjustTitles()
                else -> {}
            }
        }
        this += pane(CELL) {
            when (value.toChangedValue()) {
                CLEAN -> parentEditor.cleanCells()
                HORIZONTAL_DIVIDE -> parentEditor.divideCells(Orientation.HORIZONTAL)
                VERTICAL_DIVIDE -> parentEditor.divideCells(Orientation.VERTICAL)
                else -> {}
            }
        }
        separator()
        button("Szczegóły planu")
            .setOnAction { showDetails() }
    }

    private fun showDetails() =
        parentEditor.openDetailsWindow()


    private fun pane(applicableTo: ApplicableTo, eventHandler: ChoiceBox<String>.() -> Unit) = stackpane {
        val box = choicebox<String> {
            isVisible = false
            val values = ChangedValue.values().filter { it.applicableTo.contains(applicableTo) }
            items.addAll(values.map { it.translation })

            setOnAction {
                runLater { eventHandler(); isVisible = false; value = null }
            }
        }

        val button = button(applicableTo.translation) {
            action { box.isVisible = true; box.show() }
        }
        box.prefWidthProperty().bind(button.widthProperty())
    }


    companion object {
        private enum class ChangedValue(
            val translation: String,
            vararg val applicableTo: ApplicableTo,
        ) {
            ADD("Dodaj", ROW, COLUMN),
            REMOVE("Usuń", ROW, COLUMN),
            CLEAN("Wyczyść", CELL, ROW, COLUMN),
            TIME_SPANS("Czas trwania zajęć", COLUMN),
            HORIZONTAL_DIVIDE("Podziel w pionie", CELL),
            VERTICAL_DIVIDE("Podziel w poziomie", CELL),
            TITLE("Nazwa Zajęć", COLUMN),
            DETAILS("Więcej", CELL, ROW, COLUMN),
            ;

            companion object {
                // TODO: 17.10.2021 rename to something meaningful
                fun String?.toChangedValue() = try {
                    values().filter { it.translation == this }[0]
                } catch (e: Exception) { null }
            }
        }

        private enum class ApplicableTo(val translation: String) {
            CELL("Komórki"),
            ROW("Rzędy"),
            COLUMN("Kolumny"),
        }
    }
}
