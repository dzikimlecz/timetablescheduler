package me.dzikimlecz.components

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import me.dzikimlecz.timetable.Cell
import me.dzikimlecz.timetable.TimeTable

class TimeTableView(private val timeTable: TimeTable) : GridPane() {
    private val viewModeProperty = SimpleObjectProperty<ViewMode>()
    private val cellViews = ArrayList<ArrayList<CellView>>()
    private val rowsProperty = SimpleIntegerProperty()
    private val columnsProperty = SimpleIntegerProperty()

    var viewMode = ViewMode.EDIT
        set(value) {
            field = value
            viewModeProperty.set(value)
        }

    init {
        alignment = Pos.CENTER
        isGridLinesVisible = true
        for (y in 0 until timeTable.rows) {
            cellViews.add(ArrayList())
            for (x in 0 until timeTable.columns)
                addCell(x, y, CellView(timeTable[y][x]))
        }
        initListeners()
    }
    private fun initListeners() {
        rowsProperty.bind(timeTable.rowsProperty)
        columnsProperty.bind(timeTable.columnsProperty)

        viewModeProperty.addListener { _ -> cellViews.forEach {
                list -> list.forEach { it.refreshView() }
        } }

        rowsProperty.addListener { _, oldValue, newValue ->
            val delta = newValue.toInt() - oldValue.toInt()
            if (delta < 0) for (i in delta until 0)
                cellViews.removeLast()
            else for (i in 0 until delta) {
                cellViews.add(ArrayList())
                for (x in 0 until timeTable.columns) {
                    val y = cellViews.size - 1
                    addCell(x, y, CellView(timeTable[y][x]))
                }
            }
        }
        columnsProperty.addListener { _, oldValue, newValue ->
            val oldVal = oldValue.toInt()
            val newVal = newValue.toInt()
            val delta = newVal - oldVal
            if (delta < 0) for (row in cellViews)
                for (i in delta until 0) row.removeLast()
            else for (y in cellViews.indices)
                for (x in oldVal until newVal)
                    addCell(x, y, CellView(timeTable[y][x]))
        }
    }

    private fun addCell(x: Int, y: Int, cellView: CellView) {
        cellViews[y].add(cellView)
        this.add(cellView, x, y)
    }

    enum class ViewMode {
        EDIT, VIEW
    }

    private inner class CellView(cell: Cell) : BorderPane() {
        val labels = arrayOf(Label(), Label())
        val fields = arrayOf(TextField(), TextField())
        private val divisionProperty = SimpleBooleanProperty()

        init {
            minWidth = 30.0
            maxWidth = 100.0
            minHeight = 15.0
            maxHeight = 50.0
            for ((i, label) in labels.withIndex()) {
                label.minWidthProperty().bind(this.minWidthProperty())
                label.minHeightProperty().bind(this.minHeightProperty())
                label.textProperty().bindBidirectional(cell.getContentProperty(i))
                fields[i].textProperty().bindBidirectional(label.textProperty())
            }
            divisionProperty.addListener { _ -> refreshView() }
            divisionProperty.bind(cell.isDivided)
        }

        fun refreshView() {
            children.clear()
            val array = if (viewMode == ViewMode.VIEW) labels else fields
            if (divisionProperty.get()) {
                left = array[0]
                right = array[1]
            } else center = array[0]
        }
    }
}