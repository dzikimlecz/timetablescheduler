package me.dzikimlecz.timetables.components.views.dialogs

import javafx.collections.FXCollections
import me.dzikimlecz.timetables.components.NoSelectionModel
import tornadofx.*

class MissingLecturers: View("Brakujący Wykładowcy!") {
    val missingCodes by param<List<String>>()
    private val items = FXCollections.observableArrayList<String>()

    override val root = form {
        fieldset("W bazie nie ma części wykładowców z tego planu") {
            label("Brakujący wykładowcy:")
            listview(items) {
                selectionModel = NoSelectionModel()
            }
            label("Dodaj ich, a następnie ponownie prześlij plan") {
                isWrapText = true
            }
            buttonbar {
                button("Ok") {
                    isDefaultButton = true
                    action(::close)
                }
            }
        }
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        items.clear()
        items += missingCodes
    }
}