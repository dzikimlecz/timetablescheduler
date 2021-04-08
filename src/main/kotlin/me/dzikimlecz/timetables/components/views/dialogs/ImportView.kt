package me.dzikimlecz.timetables.components.views.dialogs


import javafx.scene.control.CheckBox
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.stage.FileChooser
import javafx.util.Callback
import me.dzikimlecz.timetables.DefaultPaths
import me.dzikimlecz.timetables.managers.FilesManager
import tornadofx.*
import java.io.File

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
        init {
            with(pathField) {
                this += customPath
                button("Wybierz") {
                    action {
                        val files = chooseFile(
                            "Wybierz plik",
                            arrayOf(FileChooser.ExtensionFilter("Plany","*.json")),
                            File(DefaultPaths.EXPORT.value),
                            owner = currentStage
                        )
                        if (files.isEmpty()) return@action
                        customPath.text = files[0].absolutePath
                    }
                }
            }
        }

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
