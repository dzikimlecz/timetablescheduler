package me.dzikimlecz.timetables.components.views

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections.observableArrayList
import javafx.geometry.Insets
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.lecturers.SettlingPeriod
import me.dzikimlecz.timetables.components.fragments.margin
import tornadofx.*
import java.time.LocalDate

class LecturerWorkTimeDisplay: View() {
    private val lecturers = observableArrayList<Lecturer>()
    private var table by singleAssign<TableView<Lecturer>>()
    private val filterStart = SimpleObjectProperty(LocalDate.now())
    private val filterEnd = SimpleObjectProperty(LocalDate.now().plusDays(1))
    private var timeFilter: (SettlingPeriod) -> Boolean = { true }

    private val lecturerToTimeWorkedProperty = mutableMapOf<Lecturer, SimpleStringProperty>()

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
                    lecturer.updateHoursWorkedText()
                    lecturerToTimeWorkedProperty[lecturer]!!
                }

            }
        }
        right = form {
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
                                val filterRange = (filterStart.get())..(filterEnd.get())
                                it.end in filterRange || it.start in filterRange
                            }
                            lecturerToTimeWorkedProperty.keys.forEach { it.updateHoursWorkedText() }
                            table.refresh()
                        }
                    }
                }
            }
        }


        left.margin = Insets(100.0, 100.0, 300.0, 500.0, )
        right.margin = Insets(100.0, 500.0, 300.0, 100.0, )
    }

    fun refresh(lecturers: Collection<Lecturer>) {
        timeFilter = { true }
        this.lecturers.clear()
        this.lecturers += lecturers
        filterStart.set(LocalDate.now())
        filterEnd.set(LocalDate.now().plusDays(1))
    }

    private fun Lecturer.updateHoursWorkedText(): String {
        val allMinutes = hoursWorked.filterKeys(timeFilter).values.sum()
        val hours = allMinutes / 60
        val minutes = allMinutes % 60
        val time = "$hours h $minutes min"
        val timeWorkedProperty = lecturerToTimeWorkedProperty[this]
        if (timeWorkedProperty != null) timeWorkedProperty.set(time)
        else lecturerToTimeWorkedProperty[this] = SimpleStringProperty(time)
        return time
    }



}