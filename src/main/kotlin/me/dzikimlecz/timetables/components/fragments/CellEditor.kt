package me.dzikimlecz.timetables.components.fragments

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Label
import javafx.scene.control.TextField
import me.dzikimlecz.timetables.timetable.Cell
import tornadofx.*
import java.util.logging.Level
import java.util.logging.Logger

class CellEditor : Fragment() {
    val cell : Cell by param()
    private val divisionProperty = SimpleBooleanProperty()
    private val texts = Array(2) { SimpleStringProperty() }

    override val root = borderpane {
        minWidth = 30.0
        minHeight = 15.0
    }

    init {
        for ((i, e) in texts.withIndex())
            e.bindBidirectional(cell.getContentProperty(i))
        divisionProperty.bind(cell.isDivided)
        divisionProperty.addListener { _ -> refreshView(TimeTableEditor.ViewMode.EDIT) }
    }

    fun refreshView(viewMode: TimeTableEditor.ViewMode) {
        val nodes = { i: Int ->
            if (viewMode == TimeTableEditor.ViewMode.VIEW) label {
                paddingAll = 10
                textProperty().bindBidirectional(texts[i])
                minWidthProperty().bind(root.minWidthProperty() + 5)
                minHeightProperty().bind(root.minHeightProperty() + 25)
            } else textarea {
                textProperty().bindBidirectional(texts[i])
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