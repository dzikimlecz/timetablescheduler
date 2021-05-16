package me.dzikimlecz.timetables.components.views

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections.observableArrayList
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import javafx.scene.text.Font
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.lecturers.SettlingPeriod
import tornadofx.*
import java.time.LocalDate
import javafx.util.Callback as Factory

class LecturerWorkTimeDisplay: View() {
    private var lecturersView by singleAssign<ListView<Lecturer>>()
    private val filterStart = SimpleObjectProperty(LocalDate.now())
    private val filterEnd = SimpleObjectProperty(LocalDate.now().minusDays(1))
    private var timeFilter: (Map<SettlingPeriod, Int>) -> Map<SettlingPeriod, Int> = { it }


    override val root: BorderPane = borderpane {
        left {
            lecturersView = listview(observableArrayList()) {
                cellFactory = Factory {
                    object : ListCell<Lecturer>() {
                        override fun updateItem(item: Lecturer?, empty: Boolean) {
                            super.updateItem(item, empty)
                            graphic = if (empty || item === null) null
                            else borderpane {
                                left = label(item.name) { font = listFont }
                                center = label(item.code) { font = listFont }
                                right = label {
                                    text = item.getHoursWorkedText()
                                    font = listFont
                                }
                            }
                        }
                    }
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
                                lecturersView.refresh()
                            }
                        }
                    }
                }
            }
        }
    }

    fun refresh(lecturers: Collection<Lecturer>) {
        timeFilter = { it }
        lecturersView.items.clear()
        lecturersView.items += lecturers
        filterStart.set(LocalDate.now())
        filterEnd.set(LocalDate.now().minusDays(1))
    }

    private fun Lecturer.getHoursWorkedText(): String {
        val allMinutes = timeFilter(hoursWorked).values.sum()
        val hours = allMinutes % 60
        val minutes = allMinutes / 60
        return "$hours h $minutes min"
    }


    companion object {
        private val labelFont = Font.font(20.0)
        private val listFont = Font.font(14.0)
    }
}