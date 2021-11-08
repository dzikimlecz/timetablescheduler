package me.dzikimlecz.timetables.components.fragments.toolbars

import me.dzikimlecz.timetables.components.fragments.editors.TimeTableEditor.Companion.ViewMode.EDIT
import tornadofx.action
import tornadofx.button
import tornadofx.separator
import tornadofx.toolbar

class ViewToolBar : TimeTableEditorToolBar()  {
    override val root = toolbar {
        button("Zapisz")
            .action(parentEditor::saveTable)
        button("Zapisz jako")
            .action(parentEditor::describedSaving)
        button("Eksportuj")
            .action(parentEditor::exportTable)
        separator()
        button("Edytuj")
            .action { parentEditor.viewMode = EDIT }
        separator()
        button("Zamknij")
            .action(parentEditor::closePane)
    }
}
