package me.dzikimlecz.timetables.components.fragments

import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.ViewMode.EDIT
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.ViewMode.VIEW
import me.dzikimlecz.timetables.timetable.Cell
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*
import java.util.stream.Collectors

class TimeTableEditor : Fragment() {
    val timeTable: TimeTable by param()
    private val viewModeProperty = SimpleObjectProperty(VIEW)
    private val editors = ArrayList<ArrayList<CellEditor>>()

    private var tablePane by singleAssign<GridPane>()

    private val viewToolBar: ViewToolBar = find(params = mapOf(ViewToolBar::parentEditor to this))

    private val editToolBar: EditToolBar by lazy {
        find(params = mapOf(EditToolBar::parentEditor to this))
    }

    private val toolBars by lazy {
        mapOf(VIEW to viewToolBar.root, EDIT to editToolBar.root)
    }

    var viewMode: ViewMode by viewModeProperty

    override val root = borderpane {
        paddingTop = 100
        paddingBottom = 120
        paddingHorizontal = 50
        top = viewToolBar.root
        center {
            tablePane = gridpane {
                maxWidthProperty().bind(primaryStage.widthProperty() - 230)
                paddingTop = 10
                alignment = Pos.TOP_CENTER
                isGridLinesVisible = true
            }
        }
    }

    init {
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
        with(tablePane) {
            stackpane {
                maxWidthProperty().bind(tablePane.maxWidthProperty() / timeTable.columns)
                minWidthProperty().bind(maxWidthProperty())
                this += editor.root
                gridpaneConstraints { columnRowIndex(x, y) }
            }
        }
        editor.refreshView(viewMode)
    }

    private fun initListeners() {
        viewModeProperty.addListener { _, _, newVal ->
            editors.forEach { list -> list.forEach { it.refreshView(viewMode) } }
            root.top = toolBars[newVal]
            removeOverlayFromCells()
        }

        timeTable.rowsProperty.addListener { _, oldValue, newValue ->
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
        timeTable.columnsProperty.addListener { _, oldValue, newValue ->
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
        val buttons = overlayCells()
        for (button in buttons.keys) {
            val cell = buttons[button]!!
            button.setOnAction {
                cell.isDivided.set(true)
                button.removeFromParent()
            }
        }
    }

    private fun overlayCells() : Map<Button, Cell> {
        val editorPanes = tablePane.children.parallelStream()
            .filter { it is StackPane }.map {it as StackPane }.collect(Collectors.toList())
        val buttons = mutableMapOf<Button, Cell>()
        for (pane in editorPanes) {
            val location = GridPane.getRowIndex(pane) to GridPane.getColumnIndex(pane)
            val cell = editors[location.first][location.second].cell
            with(pane) {
                if (!cell.isDivided.get()) this += button(cell[0]) {
                    setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
                    buttons += this to cell
                }
            }
        }
        val button = button("Ok") {
            action {
                removeOverlayFromCells()
                this.removeFromParent()
            }
        }
        editToolBar.root.items.add(button)
        return buttons
    }

    private fun removeOverlayFromCells() = tablePane.childrenUnmodifiable.stream()
        .map {it as? StackPane }.forEach { pane -> pane?.children?.removeIf { it is Button } }
}

private fun GridPane.remove(x: Int, y: Int) {
    val toRemove = children.filter {
        GridPane.getColumnIndex(it) == x && GridPane.getRowIndex(it) == y
    }
    children.removeAll(toRemove)
}