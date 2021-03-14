package me.dzikimlecz.timetables.components.fragments

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.paint.Color
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.ViewMode
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.ViewMode.VIEW
import me.dzikimlecz.timetables.timetable.Cell
import tornadofx.*
import tornadofx.Dimension.LinearUnits.px
import java.lang.Double.max

class CellEditor : Fragment() {
    val cell: Cell by param()
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
        divisionProperty.addListener { _ -> refreshView(ViewMode.EDIT) }
    }



    fun refreshView(viewMode: ViewMode) = refreshImpl {
        if (viewMode == VIEW) label {
            paddingAll = 10
            textProperty().bindBidirectional(texts[it])
            minWidth = root.minWidth + 5
            minHeight = root.minHeight + 25
        } else textarea {
            textProperty().bindBidirectional(texts[it])
        }
    }

    private inline fun refreshImpl(nodes: (i: Int) -> Control) {
        with(root) {
            clear()
            if (divisionProperty.get()) {
                left = nodes(0)
                right = nodes(1)
                if (left is Label) with(left) {
                    style {
                        borderColor += CssBox(
                            Color.TRANSPARENT,
                            Color.DARKGREY,
                            Color.TRANSPARENT,
                            Color.TRANSPARENT
                        )
                        val dim = Dimension(.0, px)
                        borderWidth += CssBox(dim, Dimension(2.0, px), dim, dim)
                    }
                }
                val leftControl = left as Control
                val rightControl = right as Control
                leftControl.prefWidthProperty().bind(root.widthProperty() / 2)
                rightControl.prefWidthProperty().bind(root.widthProperty() / 2)
                minHeight = max(leftControl.minHeight, rightControl.minHeight)
            } else center = nodes(0)
        }
    }
}