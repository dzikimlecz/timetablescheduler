package me.dzikimlecz.timetables.components.fragments

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.control.Label
import javafx.scene.control.ToolBar
import javafx.stage.Stage
import javafx.stage.StageStyle
import tornadofx.*
import kotlin.reflect.KClass

abstract class TimeTableEditorToolBar : Fragment() {
    val parentEditor by param<TimeTableEditor>()




    abstract override val root: ToolBar
}