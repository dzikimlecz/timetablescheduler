package me.dzikimlecz.timetables.components.views

import javafx.application.Platform.runLater
import javafx.collections.FXCollections.observableArrayList
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.control.Alert.AlertType.WARNING
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.BorderPane
import javafx.scene.text.Font.font
import javafx.stage.StageStyle.UTILITY
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.timetables.components.NoSelectionModel
import me.dzikimlecz.timetables.components.views.dialogs.LecturerSetUpView
import me.dzikimlecz.timetables.components.views.dialogs.MissingLecturers
import me.dzikimlecz.timetables.managers.*
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*
import java.lang.Thread.sleep
import java.time.format.DateTimeFormatter
import javafx.util.Callback as Factory


class DataBaseControlPanelView: View() {
    private var lecturersList by singleAssign<ListView<Lecturer>>()
    private var tablesList by singleAssign<ListView<TimeTable>>()
    val db by param<DataBaseConnectionManager>()

    override val root = borderpane {
        paddingHorizontalProperty.bind(primaryStage.widthProperty() * 0.3)
        paddingVerticalProperty.bind(primaryStage.heightProperty() * 0.5)
        left = borderpane {
            top = label("Wykładowcy") {
                font = labelFont
            }
            left {
                lecturersList = listview(observableArrayList()) {
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
            }
            right = flowpane {
                orientation = VERTICAL
                vgap = 10.0
                button("Pokaż czasy Pracy").setOnAction {
                    refresh()
                    find<MainView>().displayLecturersWorkTime(lecturersList.items)
                }
                button("Dodaj Wykładowcę").setOnAction {
                    val lecturerSetUpView = find<LecturerSetUpView>()
                    lecturerSetUpView.openModal(block = true, resizable = false)
                    val lecturer = lecturerSetUpView.lecturerContainer.get()!!
                    runAsync { tryToUpload(lecturer) }
                    refresh()
                }
                val deleteLecturerLabel = "Usuń Wykładowcę"
                button(deleteLecturerLabel).setOnAction {
                    TextInputDialog().apply {
                        initStyle(UTILITY)
                        headerText = deleteLecturerLabel
                        contentText = "Podaj Kod Wykładowcy do usunięcia"
                    }.showAndWait().ifPresent { runAsync { tryToDeleteLecturer(it) } }
                    refresh()
                }
                button("Odśwież").setOnAction { refresh(0) }
            }
        }
        right = borderpane {
            top = label("Plany w Bazie Danych") {
                font = labelFont
            }
            left {
                tablesList = listview(observableArrayList()) {
                cellFactory = Factory {
                    object : ListCell<TimeTable>() {
                        override fun updateItem(table: TimeTable?, empty: Boolean) {
                            super.updateItem(table, empty)
                            graphic = if (empty || table === null) null
                            else borderpane {
                                left = label(table.name) { font = listFont }
                                val text = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(table.date)
                                right = label(text) { font = listFont }
                            }
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
                        displayTable(it)
                        refresh()
                    }
                }
                button("Pobierz Plan").setOnAction {
                    withSelectedTable {
                        activeTable = it
                        saveTable()
                        refresh()
                    }
                }
                button("Usuń Plan").setOnAction {
                    withSelectedTable {
                        runAsync { tryToDeleteTable(it.name) }
                        refresh()
                    }
                }
                button("Dodaj Plan").setOnAction {
                    val table = importTable() ?: return@setOnAction
                    runAsync { sendTable(table) }
                    refresh(250)
                }
            }
        }
    refresh()
    }

    fun refresh(sleepTime: Long = 100) = runAsync {
        sleep(sleepTime)
        val timeTables = db.getTimeTables()
        val lecturers = db.getLecturers()
        runLater {
            tablesList.items.clear()
            tablesList.items += timeTables
            lecturersList.items.clear()
            lecturersList.items += lecturers
        }
    }

    private fun sendTable(data: TimeTable) = try {
        db.sendTable(data)
    } catch (e: ResponseException) {
        if (e.code == 424) {
            val text = e.reason
            val missingCodes = Json.decodeFromString(ListSerializer(String.serializer()), text).distinct()
            runLater {
                val missingCodesView = find<MissingLecturers>(
                    params = mapOf(MissingLecturers::missingCodes to missingCodes)
                )
                missingCodesView.openModal(stageStyle = UTILITY)
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
        } catch (e: ResponseException) {
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
    } catch (e: ResponseException) {
        runLater { e.handle("Nie udało się usunąć wykładowcy!") }
    } catch (e: Exception) {
        runLater { alert(ERROR, "Nie można usunąć wykładowcy $code", e.message) }
    }

    private fun tryToDeleteTable(name: String) = try {
        db.removeTable(name)
    } catch (e: ResponseException) {
        runLater { e.handle("Nie udało się usunąć planu!") }
    } catch (e: Exception) {
        runLater { alert(ERROR, "Nie można usunąć planu $name", e.message) }
    }

    private fun ResponseException.handle(header: String) {
        alert(
            ERROR,
            header,
            """
                Kod błędu $code
                Odpowiedź serwera: $reason""".trimIndent()
        )
    }

    companion object {
        private val labelFont = font(20.0)
        private val listFont = font(14.0)
    }
}
