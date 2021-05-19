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
import kotlin.reflect.KProperty1

class TimeTableSetUpView : View("Nowy Plan") {

    private val name = SimpleStringProperty("")
    private val rows = SimpleStringProperty("")
    private val columns = SimpleStringProperty("")
    private val date = SimpleObjectProperty<LocalDate>(now())
    val tableProperties by param<MutableMap<KProperty1<TimeTable, Any>, String>>()
    var table: TimeTable? = null

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

        button("Ok") {
            font = bigFont
            action {
                tableProperties[TimeTable::name] = name.get().ifBlank { now().format(ISO_LOCAL_DATE) }
                tableProperties[TimeTable::rows] = rows.get().ifBlank { "1" }
                tableProperties[TimeTable::columns] = columns.get().ifBlank { "1" }
                tableProperties[TimeTable::date] = date.get().format(ISO_LOCAL_DATE)
                close()
            }
        }
    }

    override fun onBeforeShow() = try {
        listOf(name, rows, columns).forEach { it.set("") }
        date.set(now())
    } catch(_: Exception) {}
}

fun TextField.filterNumbers() = filterInput {
    it.controlNewText.isInt() && it.text.length + text.length <= 2
}
