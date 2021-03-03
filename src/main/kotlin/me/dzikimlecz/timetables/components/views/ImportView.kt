package me.dzikimlecz.timetables.components.views


import javafx.scene.control.*
import me.dzikimlecz.timetables.managers.FilesManager
import java.io.File
import javafx.util.Callback
import tornadofx.*

class ImportView : View("Otwórz") {
    private var file: File? = null
    val chosenFile
        get() = file
    private var pathSet: Fieldset by singleAssign()
    val filesManager: FilesManager by param()
    private var filesList: ListView<File> by singleAssign()
    private var useCustomPath: CheckBox by singleAssign()
    private val customPath = textfield()
    private val pathField = field()
        init {pathField += customPath}

    override fun onBeforeShow() {
        filesManager.refreshJsonFiles()
        useCustomPath.isSelected = false
    }

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
            useCustomPath = checkbox( "Inna lokalizacja") {
                action {
                    togglePathSelector(isSelected)
                }
            }
        }

        buttonbar {
            button("Ok").setOnAction {
                file = if (useCustomPath.isSelected)
                    try { File(customPath.text) } catch(e: Exception) { null }
                else filesList.selectedItem
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
