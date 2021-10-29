package me.dzikimlecz.timetables.components.fragments.toolbars

import javafx.scene.control.ToolBar
import me.dzikimlecz.timetables.components.fragments.editors.TimeTableEditor
import tornadofx.Fragment

abstract class TimeTableEditorToolBar : Fragment() {
    val parentEditor by param<TimeTableEditor>()

    abstract override val root: ToolBar
}
