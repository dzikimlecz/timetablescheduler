package me.dzikimlecz.timetables.components.views.dialogs

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import me.dzikimlecz.timetables.DefaultPaths
import tornadofx.*
import java.io.File

class ExportView : View("Zapisz jako") {
    val exportProperties by param<MutableMap<String, String>>()

    private val customPath = SimpleStringProperty("")
    private val customName = SimpleStringProperty("")

    private val useDefaultName = SimpleBooleanProperty(true).apply {
        addListener { _, _, newValue -> setNameFieldDisplayed(!newValue) }
    }

    private val useDefaultPath = SimpleBooleanProperty(true).apply {
        addListener { _, _, newValue -> togglePathSelector(!newValue) }
    }

    private var nameSet by singleAssign<Fieldset>()
    private val nameField = field("Nazwa Pliku") { textfield(customName) }

    private var pathSet by singleAssign<Fieldset>()
    private val pathField = field("Lokalizacja") {
        textfield(customPath)
        button("Wybierz") {
            action {
                val file = chooseDirectory(
                    "Wybierz folder",
                    File(DefaultPaths.EXPORT.value),
                    currentWindow
                )
                customPath.set(file?.absolutePath ?: "")
            }
        }
    }

    override val root = form {
        nameSet = fieldset("Nazwa") {
            checkbox("Użyj nazwy planu jako nazwy pliku", useDefaultName)
        }
        pathSet = fieldset("Lokalizacja") {
            checkbox("Zapisz w domyślnej lokalizacji (Zalecane)", useDefaultPath)
        }
        buttonbar {
            button("Ok") {
                isDefaultButton = true
                action {
                    val customPath = customPath.get().ifBlank { null }
                    val customName = customName.get().ifBlank { null }
                    val useCustomName = !useDefaultName.get()
                    val useCustomPath = !useDefaultPath.get()
                    if (useCustomName && useCustomPath) fillProperties(customName, customPath)
                    else if (useCustomName) fillProperties(customName)
                    else if (useCustomPath) fillProperties(path = customPath)
                    else fillProperties()
                    close()
                }
            }
        }
    }

    private fun fillProperties(name: String? = null, path: String? = null) {
        exportProperties["name"] = name ?: "\u0000"
        exportProperties["path"] = path ?: "\u0000"
    }

    private fun setNameFieldDisplayed(active: Boolean) {
        if (active) nameSet += nameField
        else nameField.removeFromParent()
        root.scene.window.sizeToScene()
    }

    private fun togglePathSelector(active: Boolean) {
        if (active) pathSet += pathField
        else pathField.removeFromParent()
        root.scene.window.sizeToScene()
    }

}