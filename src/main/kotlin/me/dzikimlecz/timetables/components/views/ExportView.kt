package me.dzikimlecz.timetables.components.views

import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import tornadofx.*

class ExportView : View("Zapisz jako") {
    val exportProperties : MutableMap<String, String> by param()

    private var nameSet : Fieldset by singleAssign()
    private val nameField = field("Nazwa Pliku")
    private val nameTextField = textfield()
    private var pathSet : Fieldset by singleAssign()
    private val pathField = field("Lokalizacja")
    private val pathTextField = textfield()

    init {
        nameField += nameTextField
        pathField += pathTextField
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