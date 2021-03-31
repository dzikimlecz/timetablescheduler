package me.dzikimlecz.timetables.components.views.dialogs

import javafx.scene.control.CheckBox
import tornadofx.*
import java.io.File

class ExportView : View("Zapisz jako") {
    val exportProperties by param<MutableMap<String, String>>()

    private var nameSet by singleAssign<Fieldset>()
    private val nameField = field("Nazwa Pliku")
    private val nameTextField = textfield()
    private var pathSet by singleAssign<Fieldset>()
    private val pathField = field("Lokalizacja")
    private val pathTextField = textfield()

    init {
        nameField += nameTextField
        with(pathField) {
            this += pathTextField
            button("Wybierz") {
                action {
                    val file = chooseDirectory(
                        "Wybierz folder",
                        File(System.getProperty("user.home") + "\\Documents"),
                        currentWindow
                    )
                    pathField.text = file?.absolutePath ?: ""
                }
            }
        }
    }

    override val root = form {
        nameSet = fieldset("Nazwa") {
            checkbox("Użyj nazwy planu jako nazwy pliku") {
                isSelected = true
                action {
                    setNameFieldDisplayed(!isSelected)
                }
            }
        }
        pathSet = fieldset("Lokalizacja") {
            checkbox("Zapisz w domyślnej lokalizacji (Zalecane)") {
                isSelected = true
                action {
                    togglePathSelector(!isSelected)
                }
            }
        }
        buttonbar {
            button("Ok").setOnAction {
                val useCustomName =
                    nameSet.children.stream().anyMatch { it is CheckBox && !it.isSelected }
                val useCustomPath =
                    pathField.children.stream().anyMatch { it is CheckBox && !it.isSelected }
                val customPath = pathTextField.text.ifBlank { null }
                val customName = nameTextField.text.ifBlank { null }
                if (useCustomName && useCustomPath) fillProperties(customName, customPath)
                else if (useCustomName) fillProperties(customName)
                else if (useCustomPath) fillProperties(path = customPath)
                else fillProperties()
                close()
            }
        }
    }

    private fun fillProperties(name: String? = null, path: String? = null) {
        exportProperties["name"] = name ?: "\u0000"
        exportProperties["path"] = path ?: "\u0000"
    }

    private fun setNameFieldDisplayed(active: Boolean) {
        if (active) with(nameSet) {
            this += nameField
        } else nameField.removeFromParent()
        root.scene.window.sizeToScene()
    }

    private fun togglePathSelector(active: Boolean) {
        if (active) with(pathSet) {
            this += pathField
        } else pathField.removeFromParent()
        root.scene.window.sizeToScene()
    }


}