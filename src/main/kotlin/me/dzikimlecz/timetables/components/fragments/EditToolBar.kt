package me.dzikimlecz.timetables.components.fragments

import javafx.application.Platform
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.ViewMode.VIEW
import tornadofx.*

class EditToolBar : TimeTableEditorToolBar() {

    private lateinit var valueToChange : ValueForChange

    override val root = toolbar {

        button("Zatwierdź") {
            action { parentEditor.viewMode = VIEW }
        }
        separator()

        stackpane {
            val box = choicebox<String> {
                isVisible = false
                items.addAll("Dodaj", "Usuń", "Wyczyść", "Więcej")
                setOnAction {

                    Platform.runLater {
                        selectionModel.clearSelection()
                        hide()
                        isVisible = false
                    }
                }
            }
            val label = button("Rzędy") {
                action { box.isVisible = true; box.show() }
            }
            box.prefWidthProperty().bind(label.widthProperty())
        }

        stackpane {
            val box = choicebox<String> {
                isVisible = false
                items.addAll("Dodaj", "Usuń", "Wyczyść", "Więcej")
                setOnAction {

                    Platform.runLater {
                        selectionModel.clearSelection()
                        hide()
                        isVisible = false
                    }
                }
            }
            val label = button("Kolumny") {
                action { box.isVisible = true; box.show() }
            }
            box.prefWidthProperty().bind(label.widthProperty())
        }

        stackpane {
            val box = choicebox<String> {
                isVisible = false
                val cleanKey = "Wyczyść"
                val divideKey = "Podziel"
                items.addAll(cleanKey, divideKey)
                setOnAction {
                    when (selectionModel.selectedItem) {
                        cleanKey -> parentEditor.cleanCells()
                        divideKey -> parentEditor.divideCells()
                        null -> {}
                        else -> throw AssertionError()
                    }
                    Platform.runLater {
                        selectionModel.clearSelection()
                        hide()
                        isVisible = false
                    }
                }
            }
            val label = button("Komórki") {
                action { box.isVisible = true; box.show() }
            }
            box.prefWidthProperty().bind(label.widthProperty())
        }
        separator()

        button("Szczegóły planu") {

        }
    }

    private enum class ValueForChange {
        ROW, COLUMN, CELL
    }
}