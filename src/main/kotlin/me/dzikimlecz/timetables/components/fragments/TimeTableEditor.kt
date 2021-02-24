package me.dzikimlecz.timetables.components.fragments

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import me.dzikimlecz.timetables.timetable.Cell
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*

class TimeTableEditor : Fragment() {
    val timeTable: TimeTable by param()
    private val viewModeProperty = SimpleObjectProperty<ViewMode>()
    private val editors = ArrayList<ArrayList<CellEditor>>()
    private val rowsProperty = SimpleIntegerProperty()
    private val columnsProperty = SimpleIntegerProperty()

    var viewMode = ViewMode.EDIT
        set(value) {
            field = value
            viewModeProperty.set(value)
        }

    override val root = gridpane {
        alignment = Pos.CENTER
        isGridLinesVisible = true
    }

    init {
        rowsProperty.bind(timeTable.rowsProperty)
        columnsProperty.bind(timeTable.columnsProperty)
        for (y in 0 until timeTable.rows) {
            editors.add(ArrayList())
            for (x in 0 until timeTable.columns)
                addCell(x, y, timeTable[y][x])
        }
        initListeners()
    }

    private fun addCell(x: Int, y: Int, cell: Cell) {
        val editor = find<CellEditor>(mapOf(CellEditor::cell to cell))
        editors[y].add(editor)
        with(root) {
            borderpane {
                center = editor.root
                gridpaneConstraints {
                    columnRowIndex(x, y)
                }
            }
        }
    }

    private fun initListeners() {
        viewModeProperty.addListener { _ -> editors.forEach {
                list -> list.forEach { it.refreshView(viewMode) }
        } }

        rowsProperty.addListener { _, oldValue, newValue ->
            val delta = newValue.toInt() - oldValue.toInt()
            if (delta < 0) for (i in delta until 0) {
                val lastY = editors.size - 1
                for (x in 0 until editors[lastY].size) root.remove(x, lastY)
                editors.removeLast()
            }
            else for (i in 0 until delta) {
                editors.add(ArrayList())
                for (x in 0 until timeTable.columns) {
                    val y = editors.size - 1
                    addCell(x, y, timeTable[y][x])
                }
            }
        }
        columnsProperty.addListener { _, oldValue, newValue ->
            val oldVal = oldValue.toInt()
            val newVal = newValue.toInt()
            val delta = newVal - oldVal
            if (delta < 0) for ((i, row) in editors.withIndex())
                for (j in delta until 0) {
                    row.removeLast()
                    root.remove(oldVal - delta, i)
                }
            else for (y in editors.indices)
                for (x in oldVal until newVal)
                    addCell(x, y, timeTable[y][x])
        }
    }

    enum class ViewMode {
        EDIT, VIEW
    }
}

private fun GridPane.remove(x: Int, y: Int) {
    val toRemove = children.filter {
        GridPane.getColumnIndex(it) == x && GridPane.getRowIndex(it) == y
    }
    children.removeAll(toRemove)
}