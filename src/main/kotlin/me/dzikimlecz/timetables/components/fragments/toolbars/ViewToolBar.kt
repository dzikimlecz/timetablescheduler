package me.dzikimlecz.timetables.components.fragments.toolbars

import me.dzikimlecz.timetables.components.fragments.editors.TimeTableEditor
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
                find<MainView>().manager.describedSaving()
            }
        }
        button("Eksportuj") {
            action {
                parentEditor.exportTable()
            }
        }
        separator()
        button("Edytuj").setOnAction {
            parentEditor.viewMode = TimeTableEditor.Companion.ViewMode.EDIT
        }
        separator()
        button("Zamknij").setOnAction { parentEditor.closePane() }
    }
}
