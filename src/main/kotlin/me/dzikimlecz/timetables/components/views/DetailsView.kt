package me.dzikimlecz.timetables.components.views

import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import kotlin.reflect.KProperty1

class DetailsView : View("Szczegóły Planu") {
    val table by param<TimeTable>()
    private val tableProperties = mutableMapOf<KProperty1<TimeTable, Any>, String>()

    override val root = form {
        fieldset("Plan ${table.name}") {
            field("Nazwa") {
                textfield {
                    text = table.name
                    textProperty().addListener { _, _, newValue ->
                        tableProperties[TimeTable::name] = newValue
                    }
                }
            }
            field("Data") {
                datepicker {
                    value = LocalDate.now()
                    valueProperty().addListener {_, _, newValue ->
                        tableProperties[TimeTable::date] = newValue.format(ISO_LOCAL_DATE)

                    }
                }
            }
            field("Kolumny") {
                textfield {
                    text = table.columns.toString()
                    textProperty().addListener { _, _, newValue ->
                        tableProperties[TimeTable::columns] = newValue
                    }
                }
            }
            field("Rzędy") {
                textfield {
                    text = table.rows.toString()
                    textProperty().addListener { _, _, newValue ->
                        tableProperties[TimeTable::rows] = newValue
                    }
                }
            }
            buttonbar {
                button("Ok").setOnAction { commit() }
            }
        }
    }

    private fun commit() {
        table.name = tableProperties[TimeTable::name] ?: table.name
        table.rows = tableProperties[TimeTable::rows]?.toIntOrNull() ?: table.rows
        table.columns = tableProperties[TimeTable::columns]?.toIntOrNull() ?: table.columns
        val dateString = tableProperties[TimeTable::date]
        if (dateString !== null) table.date = LocalDate.parse(dateString)
        close()
    }
}