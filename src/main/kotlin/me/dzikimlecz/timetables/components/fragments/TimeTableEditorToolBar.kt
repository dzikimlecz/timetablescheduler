package me.dzikimlecz.timetables.components.fragments

import javafx.scene.control.ToolBar
import tornadofx.Fragment

abstract class TimeTableEditorToolBar : Fragment() {
    val parentEditor by param<TimeTableEditor>()

    abstract override val root: ToolBar
}