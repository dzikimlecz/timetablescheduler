package me.dzikimlecz.timetables.components.views

import javafx.geometry.Pos
import javafx.scene.control.DatePicker
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.text.Font
import tornadofx.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TimeTableSetUpView : View("Nowy Plan") {

    private var nameField: TextField by singleAssign()
    private var rowsField: TextField by singleAssign()
    private var columnsField: TextField by singleAssign()
    private var datePicker: DatePicker by singleAssign()
    val tableProperties: MutableMap<String, String> by param()


    override val root = gridpane {
        hgap = 1E1
        vgap = 1E1
        alignment = Pos.CENTER
        paddingHorizontal = 7E1
        paddingVertical = 5E1

        val bigFont = Font.font(14.0)
        row {
            label("Dane Planu:") {
                font = bigFont
            }
        }
        row {
            label("Nazwa:") {
                font = bigFont
            }
            nameField = textfield {
                font = bigFont
                promptText = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            }
        }
        row {
            label("Data:") {
                font = bigFont
            }
            datePicker = datepicker() {
                value = LocalDate.now()
            }
        }
        row {
            label("L. rzędów:") {
                font = bigFont
            }
            rowsField = textfield {
                font = bigFont
                promptText = "1"
                filterContent()
            }
        }
        row {
            label("L. kolumn:") {
                font = bigFont
            }
            columnsField = textfield {
                font = bigFont
                promptText = "1"
                filterContent()
            }
        }
        row {
            button("Ok") {
                font = bigFont
                action {
                    tableProperties["name"] = nameField.text.ifBlank { nameField.promptText }
                    tableProperties["rows"] = rowsField.text.ifBlank { rowsField.promptText }
                    tableProperties["columns"] = columnsField.text.ifBlank { columnsField.promptText }
                    tableProperties["date"] = datePicker.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    close()
                    listOf(nameField, rowsField, columnsField).forEach { it.clear() }
                    datePicker.value = LocalDate.now()
                }
            }
        }

    }

    private fun TextField.filterContent() {
        textFormatter = TextFormatter<String> {
            if (it.text.matches(Regex("\\D")) || this.text.length >= 2)
                it.text = ""
            it
        }
    }
}
