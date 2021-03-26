package me.dzikimlecz.timetables.components.fragments

import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.Companion.ViewMode.EDIT
import me.dzikimlecz.timetables.components.views.MainView
import tornadofx.action
import tornadofx.button
import tornadofx.separator
import tornadofx.toolbar

class ViewToolBar : TimeTableEditorToolBar()  {
    override val root = toolbar {
        button("Zapisz") {
            action {
                find<MainView>().manager.saveTable()
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