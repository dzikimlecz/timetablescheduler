package me.dzikimlecz.timetables.components.fragments.toolbars

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.control.ChoiceBox
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.Companion.ViewMode.VIEW
import me.dzikimlecz.timetables.components.fragments.toolbars.EditToolBar.Companion.Applicable.*
import me.dzikimlecz.timetables.components.fragments.toolbars.EditToolBar.Companion.ChangedValue.*
import me.dzikimlecz.timetables.components.fragments.toolbars.EditToolBar.Companion.ChangedValue.Companion.toChangedValue
import me.dzikimlecz.timetables.components.views.dialogs.DetailsView
import tornadofx.*

class EditToolBar : TimeTableEditorToolBar() {

    override val root = toolbar {

        button("Zatwierdź").setOnAction { parentEditor.viewMode = VIEW }

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

        button("Szczegóły planu").setOnAction { showDetails() }
    }

    private fun showDetails() = parentEditor.openInternalWindow<DetailsView>(
        params = mapOf(DetailsView::table to parentEditor.timeTable)
    )


    private fun pane(applicable: Applicable, eventHandler: ChoiceBox<String>.() -> Unit) = stackpane {
        val box = choicebox<String> {
            isVisible = false
            val values = ChangedValue.values().filter { it.applicable.contains(applicable) }
            items.addAll(values.map { it.translation })

            setOnAction {
                Platform.runLater { eventHandler(); isVisible = false; value = null }
            }
        }

        val label = button(applicable.translation) {
            action { box.isVisible = true; box.show() }
        }
        box.prefWidthProperty().bind(label.widthProperty())
    }


    companion object {
        private enum class ChangedValue(
            val translation: String,
            vararg val applicable: Applicable,
        ) {
            ADD("Dodaj", ROW, COLUMN),
            REMOVE("Usuń", ROW, COLUMN),
            CLEAN("Wyczyść", CELL, ROW, COLUMN),
            DETAILS("Więcej", CELL, ROW, COLUMN),
            TIME_SPANS("Czas trwania zajęć", COLUMN),
            HORIZONTAL_DIVIDE("Podziel w pionie", CELL),
            VERTICAL_DIVIDE("Podziel w poziomie", CELL),
            TITLE("Nazwa Zajęć", COLUMN),
            ;

            companion object {
                fun String?.toChangedValue() = try {
                    values().filter { it.translation == this }[0]
                } catch (e: Exception) { null }
            }
        }

        private enum class Applicable(val translation: String) {
            CELL("Komórki"),
            ROW("Rzędy"),
            COLUMN("Kolumny"),
        }
    }
}