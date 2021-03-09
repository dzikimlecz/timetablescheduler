package me.dzikimlecz.timetables.components.fragments

import javafx.scene.control.Label
import javafx.scene.control.ToolBar
import javafx.stage.StageStyle
import tornadofx.*
import kotlin.reflect.KClass

abstract class TimeTableEditorToolBar : Fragment() {
    val parentEditor by param<TimeTableEditor>()
    class ModifierChoiceStage : View() {
        override val root = vbox {
            label("chuj")
        }
    }

    abstract override val root: ToolBar
}