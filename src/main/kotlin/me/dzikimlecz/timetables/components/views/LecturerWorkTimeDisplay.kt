package me.dzikimlecz.timetables.components.views

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections.observableArrayList
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.lecturers.SettlingPeriod
import tornadofx.*
import java.time.LocalDate

class LecturerWorkTimeDisplay: View() {
    private val lecturers = observableArrayList<Lecturer>()
    private var table by singleAssign<TableView<Lecturer>>()
    private val filterStart = SimpleObjectProperty(LocalDate.now())
    private val filterEnd = SimpleObjectProperty(LocalDate.now().minusDays(1))
    private var timeFilter: (Map<SettlingPeriod, Int>) -> Map<SettlingPeriod, Int> = { it }


    override val root: BorderPane = borderpane {
        left {
            table = tableview(lecturers) {
                isEditable = false
                column<Lecturer, String>("Imię i Nazwisko") {
                    val lecturer = it.value
                    SimpleStringProperty(lecturer.name)
                }
                column<Lecturer, String>("Kod") {
                    val lecturer = it.value
                    SimpleStringProperty(lecturer.code)
                }
                column<Lecturer, String>("Czas Pracy") {
                    val lecturer = it.value
                    SimpleStringProperty(lecturer.getHoursWorkedText())
                }

            }
        }
        right {
            form {
                fieldset("Filtruj daty") {
                    field("Początek") {
                        datepicker(filterStart) {

                        }
                    }
                    field("Koniec") {
                        datepicker(filterEnd) {

                        }
                    }
                    buttonbar {
                        button("Ok") {
                            isDefaultButton = true
                            action {
                                timeFilter = {
                                    it.filterKeys { period ->
                                        !period.end.isBefore(filterStart.get()) || !period.start.isAfter(filterEnd.get())
                                    }
                                }
                                table.refresh()
                            }
                        }
                    }
                }
            }
        }
    }

    fun refresh(lecturers: Collection<Lecturer>) {
        timeFilter = { it }
        this.lecturers.clear()
        this.lecturers += lecturers
        filterStart.set(LocalDate.now())
        filterEnd.set(LocalDate.now().minusDays(1))
    }

    private fun Lecturer.getHoursWorkedText(): String {
        val allMinutes = timeFilter(hoursWorked).values.sum()
        val hours = allMinutes % 60
        val minutes = allMinutes / 60
        return "$hours h $minutes min"
    }

}