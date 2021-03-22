package me.dzikimlecz.timetables.components.fragments

import javafx.scene.Parent
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.ViewMode.*
import me.dzikimlecz.timetables.components.views.MainView
import tornadofx.*

class ViewToolBar : TimeTableEditorToolBar()  {
    override val root = toolbar {
        button("Zapisz") {
            action {
                find<MainView>().manager.exportTable()
            }
        }
        button("Zapisz jako") {
            action {
                find<MainView>().manager.describedExport()
            }
        }
        button("Eksportuj") {
            action {
                parentEditor.exportTable()
            }
        }
        separator()
        button("Edytuj").setOnAction {
            parentEditor.viewMode = EDIT
        }
        separator()
        button("Zamknij").setOnAction { parentEditor.closePane() }
    }
}