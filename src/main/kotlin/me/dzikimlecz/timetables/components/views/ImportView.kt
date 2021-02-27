package me.dzikimlecz.timetables.components.views


import javafx.scene.control.*
import me.dzikimlecz.timetables.managers.FilesManager
import java.io.File
import javafx.util.Callback
import tornadofx.*

class ImportView : View("Otwórz") {
    private lateinit var file: File
    val chosenFile
        get() = file
    private var pathSet: Fieldset by singleAssign()
    val filesManager: FilesManager by param()
    private var filesList: ListView<File> by singleAssign()
    private val pathField = field { textfield() }

    override fun onBeforeShow() = filesManager.refreshJsonFiles()

    override val root = form {
        fieldset("Plany w domyślnej lokalizacji") {
            filesList = listview(filesManager.jsonFiles) {
                maxHeight = 120.0
                cellFactory = Callback<ListView<File>, ListCell<File>> { object : ListCell<File>() {
                    override fun updateItem(item: File?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = item?.nameWithoutExtension ?: ""
                    }
                } }
            }
        }
        pathSet = fieldset("Otwórz inny plan") {
            checkbox( "Inna lokalizacja") {
                action {
                    togglePathSelector(isSelected)
                }
            }
        }

        buttonbar {
            button("Ok").setOnAction {
                file = if (pathSet.children.stream().anyMatch { it is CheckBox && it.isSelected })
                    File((pathField.children.filtered { it is TextField }[0] as TextField).text)
                else filesList.selectedItem!!
                close()
            }
        }
    }

    private fun togglePathSelector(active: Boolean) {
        if (active) with(pathSet) {
            this += pathField
        } else pathField.removeFromParent()
        root.scene.window.sizeToScene()
    }

}
