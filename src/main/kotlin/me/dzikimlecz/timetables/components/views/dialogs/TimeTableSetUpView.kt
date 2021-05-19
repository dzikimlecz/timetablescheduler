package me.dzikimlecz.timetables.components.views.dialogs

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TextField
import javafx.scene.text.Font
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

class TimeTableSetUpView : View("Nowy Plan") {

    private val name = SimpleStringProperty("")
    private val rows = SimpleStringProperty("")
    private val columns = SimpleStringProperty("")
    private val date = SimpleObjectProperty<LocalDate>(now())
    private var _table: TimeTable? = null

    val table
        get() = _table

    override val root = form {
        paddingAll = 20
        val bigFont = Font.font(14.0)
        fieldset("Dane Planu") {
            field("Nazwa") {
                label.font = bigFont
                textfield(name) {
                    font = bigFont
                    promptText = now().format(ISO_LOCAL_DATE)
                }
            }
            field("Data Początku Planu") {
                label.font = bigFont
                datepicker(date) {
                    value = now()
                }
            }
        }
        fieldset("Początkowe wymiary") {
            field("L. rzędów") {
                label.font = bigFont
                textfield(rows) {
                    font = bigFont
                    promptText = "1"
                    filterNumbers()
                }
            }

            field("L. kolumn") {
                label.font = bigFont
                textfield(columns) {
                    font = bigFont
                    promptText = "1"
                    filterNumbers()
                }
            }
        }

        buttonbar {
            button("Ok") {
                isDefaultButton = true
                font = bigFont
                action {
                    val name = name.get().ifBlank { now().format(ISO_LOCAL_DATE) }
                    val rows = rows.get().toIntOrNull() ?: 1
                    val columns = columns.get().toIntOrNull() ?: 1
                    _table = TimeTable(columns, rows, date.get(), name)
                    close()
                }
            }
            button("Anuluj") {
                isCancelButton = true
                font = bigFont
                action(::close)
            }
        }


    }

    override fun onBeforeShow() {
        listOf(name, rows, columns).forEach { it.set("") }
        date.set(now())
        _table = null
    }
}

fun TextField.filterNumbers() = filterInput {
    it.controlNewText.isInt() && it.text.length + text.length <= 2
}
