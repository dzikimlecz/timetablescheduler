package me.dzikimlecz.timetables.components.fragments

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.GridPane
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.ViewMode.*
import me.dzikimlecz.timetables.timetable.Cell
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*

class TimeTableEditor : Fragment() {
    val timeTable: TimeTable by param()
    private val viewModeProperty = SimpleObjectProperty<ViewMode>()
    private val editors = ArrayList<ArrayList<CellEditor>>()
    private val rowsProperty = SimpleIntegerProperty()
    private val columnsProperty = SimpleIntegerProperty()
    private var tablePane by singleAssign<GridPane>()

    private val viewToolBar: ViewToolBar
        get() = find(params = mapOf(ViewToolBar::parentEditor to this))

    private val editToolBar: EditToolBar
        get() = find(params = mapOf(EditToolBar::parentEditor to this))

    private val toolBars by lazy {
        mapOf(VIEW to viewToolBar.root, EDIT to editToolBar.root)
    }


    var viewMode = VIEW
        set(value) {
            field = value
            viewModeProperty.set(value)
        }

    override val root = borderpane {
        paddingTop = 100
        paddingBottom = 120
        paddingHorizontal = 50
        top = viewToolBar.root
        center {
            tablePane = gridpane {
                maxWidthProperty().bind(primaryStage.widthProperty() - 180)
                paddingTop = 10
                alignment = Pos.TOP_CENTER
                isGridLinesVisible = true
            }
        }
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
        editor.refreshView(viewMode)
        editors[y].add(editor)
        with(tablePane) {
            stackpane {
                maxWidthProperty().bind(tablePane.maxWidthProperty().divide(columnsProperty))
                add(editor.root)
                gridpaneConstraints {
                    columnRowIndex(x, y)
                }
            }
        }
        editor.root.maxWidthProperty().bind(tablePane.maxWidthProperty().divide(columnsProperty))
    }

    private fun initListeners() {
        viewModeProperty.addListener { _, _, newVal ->
            editors.forEach { list -> list.forEach { it.refreshView(viewMode) } }
            root.top = toolBars[newVal]
        }

        rowsProperty.addListener { _, oldValue, newValue ->
            val delta = newValue.toInt() - oldValue.toInt()
            if (delta < 0) for (i in delta until 0) {
                val lastY = editors.size - 1
                for (x in 0 until editors[lastY].size) tablePane.remove(x, lastY)
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
                    tablePane.remove(oldVal - delta, i)
                }
            else for (y in editors.indices)
                for (x in oldVal until newVal)
                    addCell(x, y, timeTable[y][x])
        }
    }

    enum class ViewMode {
        EDIT, VIEW
    }

    fun cleanCells() {
        TODO("Not yet implemented")
    }

    fun divideCells() {
        TODO("Not yet implemented")
    }
}

private fun GridPane.remove(x: Int, y: Int) {
    val toRemove = children.filter {
        GridPane.getColumnIndex(it) == x && GridPane.getRowIndex(it) == y
    }
    children.removeAll(toRemove)
}