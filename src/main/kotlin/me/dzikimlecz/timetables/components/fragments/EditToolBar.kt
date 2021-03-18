package me.dzikimlecz.timetables.components.fragments

import javafx.application.Platform
import javafx.geometry.Orientation
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.ViewMode.VIEW
import me.dzikimlecz.timetables.components.views.DetailsView
import tornadofx.*

class EditToolBar : TimeTableEditorToolBar() {

    override val root = toolbar {

        button("Zatwierdź") {
            action { parentEditor.viewMode = VIEW }
        }
        separator()

        stackpane {
            val box = choicebox<String> {
                isVisible = false
                val addKey = "Dodaj"
                val removeKey = "Usuń"
                val cleanKey = "Wyczyść"
                val detailsKey = "Więcej"
                items.addAll(addKey, removeKey, cleanKey, detailsKey)
                setOnAction {
                    when (value) {
                        addKey -> parentEditor.timeTable.rows++
                        removeKey -> parentEditor.timeTable.rows--
                        cleanKey -> parentEditor.cleanRows()
                        detailsKey -> showDetails()
                    }

                    Platform.runLater { isVisible = false; value = null }
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
                val addKey = "Dodaj"
                val removeKey = "Usuń"
                val cleanKey = "Wyczyść"
                val detailsKey = "Więcej"
                items.addAll(addKey, removeKey, cleanKey, detailsKey)
                setOnAction {
                    when (value) {
                        addKey -> parentEditor.timeTable.columns++
                        removeKey -> parentEditor.timeTable.columns--
                        cleanKey -> parentEditor.cleanColumns()
                        detailsKey -> showDetails()
                    }

                    Platform.runLater { isVisible = false; value = null }
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
                val horizontalDivideKey = "Podziel w pionie"
                val verticalDivideKey = "Podziel w poziomie"
                items.addAll(cleanKey, horizontalDivideKey, verticalDivideKey)
                setOnAction {
                    when (value) {
                        cleanKey -> parentEditor.cleanCells()
                        horizontalDivideKey -> parentEditor.divideCells(Orientation.HORIZONTAL)
                        verticalDivideKey -> parentEditor.divideCells(Orientation.VERTICAL)
                    }
                    Platform.runLater { isVisible = false; value = null }
                }
            }
            val label = button("Komórki") {
                action { box.isVisible = true; box.show() }
            }
            box.prefWidthProperty().bind(label.widthProperty())
        }
        separator()

        button("Szczegóły planu").setOnAction { showDetails() }
    }

    private fun showDetails() {
        parentEditor.openInternalWindow<DetailsView>(
            params = mapOf(DetailsView::table to parentEditor.timeTable)
        )
    }
}