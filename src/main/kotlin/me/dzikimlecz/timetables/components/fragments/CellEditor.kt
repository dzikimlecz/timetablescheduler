package me.dzikimlecz.timetables.components.fragments

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Label
import javafx.scene.control.TextField
import me.dzikimlecz.timetables.timetable.Cell
import tornadofx.Fragment
import tornadofx.borderpane
import tornadofx.clear

class CellEditor : Fragment() {
    val cell : Cell by param()
    private val divisionProperty = SimpleBooleanProperty()
    private val texts = Array(2) { SimpleStringProperty() }

    override val root = borderpane {
        minWidth = 30.0
        maxWidth = 100.0
        minHeight = 15.0
        maxHeight = 50.0
    }

    init {
        for ((i, e) in texts.withIndex())
            e.bindBidirectional(cell.getContentProperty(i))
        divisionProperty.addListener { _ -> refreshView(TimeTableEditor.ViewMode.EDIT) }
        divisionProperty.bind(cell.isDivided)
    }

    fun refreshView(viewMode: TimeTableEditor.ViewMode) {
        val nodes = { i: Int ->
            if (viewMode == TimeTableEditor.ViewMode.VIEW) {
                val label = Label()
                label.textProperty().bindBidirectional(texts[i])
                label.minWidthProperty().bind(root.minWidthProperty())
                label.minHeightProperty().bind(root.minHeightProperty())
                label
            } else {
                val field = TextField()
                field.textProperty().bindBidirectional(texts[i])
                field.minWidthProperty().bind(root.minWidthProperty())
                field.minHeightProperty().bind(root.minHeightProperty())
                field
            }
        }
        with(root) {
            clear()
            if (divisionProperty.get()) {
                left = nodes(0)
                right = nodes(1)
            } else center = nodes(0)
        }
    }
}