package me.dzikimlecz.timetables.components.views

import javafx.collections.FXCollections.emptyObservableList
import javafx.collections.FXCollections.observableArrayList
import javafx.collections.ObservableList
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.control.ListCell
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.TextInputDialog
import javafx.scene.text.Font.font
import javafx.stage.StageStyle.UTILITY
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*
import javafx.util.Callback as Factory


class DataBaseControlPanelView: View() {
    val lecturers by param<List<Lecturer>>()
    val tables by param<List<TimeTable>>()

    override val root = borderpane {
            paddingVertical = 500
            paddingHorizontal = 1000
            left = borderpane {
                top = label("Wykładowcy") {
                    font = labelFont
                }
                left = listview(observableArrayList(lecturers)) {
                    selectionModel.clearSelection()
                    selectionModel = NoSelectionModel()
                    cellFactory = Factory {
                        object : ListCell<Lecturer>() {
                            override fun updateItem(item: Lecturer?, empty: Boolean) {
                                super.updateItem(item, empty)
                                graphic = if (empty || item === null) null
                                else borderpane {
                                    left = label(item.name) { font = listFont }
                                    right = label(item.code) { font = listFont }
                                }
                            }
                        }
                    }
                }
                right = flowpane {
                    orientation = VERTICAL
                    vgap = 10.0
                    button("Pokaż czasy Pracy") {

                    }
                    button("Dodaj Wykładowcę") {

                    }
                    val deleteLecturerLabel = "Usuń Wykładowcę"
                    button(deleteLecturerLabel) {
                        action {
                            TextInputDialog().apply {
                                initStyle(UTILITY)
                                title = deleteLecturerLabel
                                contentText = "Podaj Kod Wykładowcy do usunięcia"
                            }.showAndWait().ifPresent {
                                try {
                                    find<MainView>().manager.db.removeLecturer(it)
                                } catch (e: Exception) {
                                    alert(ERROR, "Nie można usunąć wykładowcy $it", e.message)
                                }
                            }
                        }
                    }
                }
            }
            right = borderpane {
                top = label("Plany w Bazie Danych") {
                    font = labelFont
                }
                left = listview(observableArrayList(tables)) {
                    cellFactory = Factory {
                        object : ListCell<TimeTable>() {
                            override fun updateItem(table: TimeTable?, empty: Boolean) {
                                super.updateItem(table, empty)
                                graphic = if (empty || table === null) null
                                else borderpane {
                                    left = label(table.name) { font = listFont }
                                    val text = "${table.date.dayOfMonth}.${table.date.monthValue}.${table.date.year}"
                                    right = label(text) { font = listFont }
                                }
                            }
                        }
                    }
                }
                right = flowpane {
                    orientation = VERTICAL
                    vgap = 10.0
                    button("Otwórz Plan") {
                    }
                    button("Pobierz Plan") {
                    }
                    button("Usuń Plan") {
                    }
                }
            }
        }

    companion object {
        private val labelFont = font(20.0)
        private val listFont = font(14.0)
    }
}

/**
 * ## Selection Model, that doesn't allow user to select anything.
 * Used in ListViews, which are used solely for displaying purpose
 */
private class NoSelectionModel<T> : MultipleSelectionModel<T>() {
    override fun getSelectedIndices(): ObservableList<Int> = emptyObservableList()
    override fun getSelectedItems(): ObservableList<T> = emptyObservableList()
    override fun selectIndices(index: Int, vararg indices: Int) {}
    override fun selectAll() {}
    override fun selectFirst() {}
    override fun selectLast() {}
    override fun clearAndSelect(index: Int) {}
    override fun select(index: Int) {}
    override fun select(obj: T) {}
    override fun clearSelection(index: Int) {}
    override fun clearSelection() {}
    override fun isSelected(index: Int): Boolean = false
    override fun isEmpty(): Boolean = true
    override fun selectPrevious() {}
    override fun selectNext() {}
}