package me.dzikimlecz.timetables.components.views

import javafx.collections.FXCollections
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import me.dzikimlecz.timetables.managers.FilesManager
import java.io.File
import javafx.util.Callback
import tornadofx.*

class ImportView : View("Otwórz") {
    val filesManager: FilesManager by param()

    override val root = form {
        val files = FXCollections.observableList(filesManager.jsonFiles())
        fieldset("Plany w domyślnej lokalizacji") {
            listview(files) {
                maxHeight = 120.0
                cellFactory = Callback<ListView<File>, ListCell<File>> { object : ListCell<File>() {
                    override fun updateItem(item: File?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = item?.name?.removeSuffix(".json") ?: ""
                    }
                } }
            }
        }
        fieldset("Wybierz inny plan") {
            field("Lokalizacja") {
                textfield {

                }
            }
        }
        buttonbar {
            button("Ok")
        }
    }

}
