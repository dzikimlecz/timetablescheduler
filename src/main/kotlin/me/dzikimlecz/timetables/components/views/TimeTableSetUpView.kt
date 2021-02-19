package me.dzikimlecz.timetables.components.views

import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.text.Font
import tornadofx.*
import java.lang.Integer.parseInt
import java.util.concurrent.atomic.AtomicInteger

class TimeTableSetUpView : Fragment("Nowy Plan") {

    private var rowsField: TextField by singleAssign()
    private var columnsField: TextField by singleAssign()
    val dimensions : Pair<AtomicInteger, AtomicInteger> by param()

    override val root = gridpane {
        hgap = 1E1
        vgap = 1E1
        alignment = Pos.CENTER
        paddingHorizontal = 7E1
        paddingVertical = 5E1

        val bigFont = Font.font(14.0)
        row {
            label("Początkowe wymiary siatki godzin:") {
                font = bigFont
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
                    dimensions.first.set(parseInt(rowsField.text.ifBlank { "1" }))
                    dimensions.second.set(parseInt(columnsField.text.ifBlank { "1" }))
                    close()
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
