package me.dzikimlecz.timetables.components.views.dialogs

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import me.dzikimlecz.timetables.DefaultPaths
import tornadofx.*
import java.io.File

class ExportView : View("Zapisz jako") {
    private var pathToName: Pair<String?, String?>? = null

    val fileData
        get() = pathToName

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
                    val path = if (useDefaultPath.get()) null else customPath.get().ifBlank { null }
                    val name = if (useDefaultName.get()) null else customName.get().ifBlank { null }
                    pathToName = path to name
                    close()
                }
            }
            button("Anuluj") {
                isCancelButton = true
                action(::close)
            }
        }
    }

    override fun onBeforeShow() {
        customPath.set("")
        customName.set("")
        useDefaultName.set(true)
        useDefaultPath.set(true)
        pathToName = null
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