package me.dzikimlecz.timetables.components.fragments

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.paint.Color
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.Companion.ViewMode
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.Companion.ViewMode.EDIT
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.Companion.ViewMode.VIEW
import me.dzikimlecz.timetables.timetable.Cell
import tornadofx.*
import tornadofx.Dimension.LinearUnits.px

class CellEditor : Fragment() {
    val cell: Cell by param()
    private val texts: Array<SimpleStringProperty> = Array(2) { i -> cell.getContentProperty(i) }

    override val root = borderpane {
        minWidth = 30.0
        minHeight = 15.0
    }

    private val labels: List<Label>
    init {
        val list = mutableListOf<Label>()
        for (i in 0..1) label {
            paddingAll = 10
            textProperty().bindBidirectional(texts[i])
            minWidth = root.minWidth + 5
            minHeight = root.minHeight + 25
        }.also(list::add)
        labels = list
    }

    private val textAreas: List<TextArea> by lazy {
        val list = mutableListOf<TextArea>()
        for (i in 0..1) textarea {
            textProperty().bindBidirectional(texts[i])
        }.also(list::add)
        list
    }



    init {
        cell.divisionDirectionProperty.addListener { _,_,_ -> refreshView(EDIT) }
    }

    fun refreshView(viewMode: ViewMode) = refreshImpl { i: Int ->
        if (viewMode == VIEW) labels[i]
        else textAreas[i]
    }

    private inline fun refreshImpl(nodes: (i: Int) -> Control) = with(root) {
        clear()
        if (!cell.isDivided) {
            center = nodes(0)
            return
        }

        val sectionsToFill: List<ObjectProperty<Node>>
        val colors: List<Color>
        val dims: List<Dimension<Dimension.LinearUnits>>

        val emptyDim = Dimension(.0, px)
        if (cell.divisionDirection == Orientation.HORIZONTAL) {
            sectionsToFill = listOf(leftProperty(), rightProperty())
            colors = listOf(Color.DARKGREY, Color.TRANSPARENT)
            dims = listOf(Dimension(2.0, px), emptyDim)
        } else {
            sectionsToFill = listOf(topProperty(), bottomProperty())
            colors = listOf(Color.TRANSPARENT, Color.DARKGREY)
            dims = listOf(emptyDim, Dimension(2.0, px))
        }

        sectionsToFill[0].set(nodes(0))
        sectionsToFill[1].set(nodes(1))
        if (sectionsToFill[0].value is Label) with(sectionsToFill[0].value) {
            style {
                borderColor += CssBox(
                    Color.TRANSPARENT,
                    colors[0],
                    colors[1],
                    Color.TRANSPARENT
                )
                borderWidth += CssBox(emptyDim, dims[0], dims[1], emptyDim)
            }
        }

        val firstControl = sectionsToFill[0].value as Control
        val secondControl = sectionsToFill[1].value as Control
        if (cell.divisionDirection == Orientation.HORIZONTAL) {
            firstControl.prefWidthProperty().bind(root.widthProperty() / 2)
            secondControl.prefWidthProperty().bind(root.widthProperty() / 2)
            firstControl.prefHeightProperty().bind(root.heightProperty())
            secondControl.prefHeightProperty().bind(root.heightProperty())
        } else {
            firstControl.maxHeightProperty().bind(root.heightProperty() / 2)
            secondControl.maxHeightProperty().bind(root.heightProperty() / 2)
            firstControl.prefWidthProperty().bind(root.widthProperty())
            secondControl.prefWidthProperty().bind(root.widthProperty())
        }
    }
}