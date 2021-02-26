package me.dzikimlecz.timetables.components.views

import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import tornadofx.*

class ExportView : View("Zapisz jako") {
    val exportProperties : MutableMap<String, String> by param()
    private var nameSet : Fieldset by singleAssign()
    private val nameField = field("Nazwa Pliku")
    private val nameTextField : TextField by lazy { textfield {

    }}
    private var pathSet : Fieldset by singleAssign()
    private val pathField = field("Lokalizacja")
    private val pathTextField : TextField by lazy { textfield {

    }}

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
                if (nameSet.children.stream().anyMatch { it is CheckBox && !it.isSelected } ||
                    pathField.children.stream().anyMatch { it is CheckBox && !it.isSelected }) {
                    fillProperties(nameTextField.text.ifBlank { null }, pathTextField.text
                        .ifBlank { null })
                } else fillProperties()
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