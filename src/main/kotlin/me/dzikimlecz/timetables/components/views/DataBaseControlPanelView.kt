package me.dzikimlecz.timetables.components.views

import javafx.application.Platform.runLater
import javafx.collections.FXCollections.emptyObservableList
import javafx.collections.FXCollections.observableArrayList
import javafx.collections.ObservableList
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.control.Alert.AlertType.WARNING
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.BorderPane
import javafx.scene.text.Font.font
import javafx.stage.StageStyle.UTILITY
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.timetables.components.views.dialogs.ImportView
import me.dzikimlecz.timetables.components.views.dialogs.LecturerSetUpView
import me.dzikimlecz.timetables.managers.ServerAccessException
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*
import java.io.File
import javafx.util.Callback as Factory


class DataBaseControlPanelView: View() {
    val lecturers by param<List<Lecturer>>()
    val tables by param<List<TimeTable>>()
    private val manager = find<MainView>().manager
    private val db = manager.db

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
                    button("Pokaż czasy Pracy").setOnAction {
                        TODO()
                    }
                    button("Dodaj Wykładowcę").setOnAction {
                        val lecturerSetUpView = find<LecturerSetUpView>()
                        lecturerSetUpView.openModal(block = true, resizable = false)
                        val lecturer = lecturerSetUpView.lecturerContainer.get()!!
                        runAsync() { tryToUpload(lecturer) }
                    }
                    val deleteLecturerLabel = "Usuń Wykładowcę"
                    button(deleteLecturerLabel).setOnAction {
                        TextInputDialog().apply {
                            initStyle(UTILITY)
                            headerText = deleteLecturerLabel
                            contentText = "Podaj Kod Wykładowcy do usunięcia"
                        }.showAndWait().ifPresent { runAsync { tryToDeleteLecturer(it) } }
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
                    button("Otwórz Plan").setOnAction {
                        withSelectedTable {
                            manager.displayTable(it)
                        }
                    }
                    button("Pobierz Plan").setOnAction {
                        withSelectedTable {
                            manager.activeTable = it
                            manager.saveTable()
                        }
                    }
                    button("Usuń Plan") {
                        withSelectedTable {
                            runAsync { tryToDeleteTable(it.name) }
                        }
                    }
                    button("Dodaj Plan").setOnAction {
                        val importView = find<ImportView>()
                        importView.openModal(block = true, resizable = false)
                        val file = importView.chosenFile ?: return@setOnAction
                        runAsync { sendTable(file) }
                    }
                }
            }
        }

    private fun sendTable(file: File) = try {
        db.sendTable(file.readText())
    } catch (e: ServerAccessException) {
        if (e.code == 424) {
            val text = e.reason
            val missingCodes = Json.decodeFromString(ListSerializer(String.serializer()), text)
            runLater {
                openInternalWindow<MissingLecturers>(params = mapOf(MissingLecturers::missingCodes to missingCodes))
            }
        } else runLater {
            e.handle("Nie udało się przesłać planu.")
        }
    } catch (e: Exception) {
        runLater { alert(ERROR, "Błąd przesyłania: ${e.message}") }
    }

    private fun tryToUpload(lecturer: Lecturer) {
        val lookForLecturer = db.lookForLecturer(lecturer.code)
        if (lookForLecturer !== null) {
            runLater {
                alert(
                    WARNING,
                    "Wykładowca o tym kodzie już istnieje!",
                    """Nie można utworzyć 2 wykładowców o kodzie ${lecturer.code} 
                        Istnieje już wykładowca o tym kodzie: ${lookForLecturer.name}""".trimIndent()
                )
            }
            return
        }
        try {
            db.sendLecturer(lecturer)
        } catch (e: ServerAccessException) {
            runLater { e.handle("Nie udało się przesłać wykładowcy!") }
        } catch (e: Exception) {
            runLater { alert(ERROR, "Nie można dodać wykładowcy $lecturer", e.message) }
        }
    }

    private fun BorderPane.withSelectedTable(action: (TimeTable) -> Unit) {
        if (left is ListView<*>) {
            @Suppress("UNCHECKED_CAST")
            val listView = left as ListView<TimeTable>
            val selectedItem: TimeTable? = listView.selectionModel.selectedItem
            if (selectedItem !== null)
                action(selectedItem)
            else alert(WARNING, "Nie wybrano Planu")
        }
    }

    private fun tryToDeleteLecturer(code: String) = try {
        db.removeLecturer(code)
    } catch (e: ServerAccessException) {
        runLater { e.handle("Nie udało się usunąć wykładowcy!") }
    } catch (e: Exception) {
        runLater { alert(ERROR, "Nie można usunąć wykładowcy $code", e.message) }
    }

    private fun tryToDeleteTable(name: String) = try {
        db.removeTable(name)
    } catch (e: ServerAccessException) {
        runLater { e.handle("Nie udało się usunąć planu!") }
    } catch (e: Exception) {
        runLater { alert(ERROR, "Nie można usunąć planu $name", e.message) }
    }

    private fun ServerAccessException.handle(header: String) {
        alert(
            ERROR,
            header,
            """Kod błędu $code
               Odpowiedź serwera: $reason""".trimIndent()
        )
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

internal class MissingLecturers: View("Brakujący Wykładowcy!") {
    val missingCodes by param<List<String>>()

    override val root = form {
        fieldset("W bazie nie ma części wykładowców z tego planu") {
            label("Brakujący wykładowcy:")
            listview(missingCodes.asObservable()) {
                selectionModel = NoSelectionModel()
            }
            label("Dodaj ich, a następnie ponownie prześlij plan") {
                isWrapText = true
            }
            button("Ok") {
                isDefaultButton = true
                action(::close)
            }
        }
    }
}


