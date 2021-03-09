package me.dzikimlecz.timetables.components.fragments

import javafx.event.EventHandler
import javafx.stage.StageStyle
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.ViewMode.*
import tornadofx.*

class EditToolBar : TimeTableEditorToolBar() {

    private lateinit var valueToChange : ValueForChange

    override val root = toolbar {

        button("Zatwierdź") {
            action { parentEditor.viewMode = VIEW }
        }
        separator()
        button("Dodaj") {
            onMouseEntered = EventHandler {
                find<ModifierChoiceStage>()
            }
            onMouseExited = EventHandler {
                find<ModifierChoiceStage>().close()
            }
        }
        button("Usuń") {

        }
        button("Wyczyść") {

        }
        button("Podziel") {

        }
        separator()
        button("Szczegóły planu") {

        }
    }

    private enum class ValueForChange {
        ROW, COLUMN, CELL
    }
}