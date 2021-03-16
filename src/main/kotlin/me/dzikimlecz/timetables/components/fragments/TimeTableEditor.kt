package me.dzikimlecz.timetables.components.fragments

import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
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
        top = viewToolBar.root
        center {
            tablePane = gridpane {
                maxWidthProperty().bind(primaryStage.widthProperty() - 230)
                maxHeightProperty().bind(primaryStage.heightProperty() - 230)
                paddingTop = 10
                alignment = Pos.TOP_CENTER
                isGridLinesVisible = true
            }
            BorderPane.setMargin(tablePane, Insets(90.0, 25.0, 120.0, 25.0))
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
                maxWidthProperty().bind(tablePane.maxWidthProperty() / timeTable.columnsProperty)
                maxHeightProperty().bind(tablePane.maxHeightProperty() / timeTable.rowsProperty)
                prefWidthProperty().bind(maxWidthProperty())
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
                editors.last().forEach { it.cell.clean() }
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
                    row.last().cell.clean()
                    row.removeLast()
                    tablePane.remove(oldVal + delta, i)
                }
            else for (y in editors.indices)
                for (x in oldVal until newVal)
                    addCell(x, y, timeTable[y][x])
        }
    }

    enum class ViewMode {
        EDIT, VIEW
    }

    fun cleanCells() = handleCellsOverlayingAction { clean() }

    fun divideCells() = handleCellsOverlayingAction { isDivided = !isDivided }

    private fun handleCellsOverlayingAction(action: Cell.() -> Unit) {
        val buttons = overlayCells()
        for (button in buttons.keys) {
            val editor = buttons[button]!!
            button.setOnAction {
                editor.cell.action()
                button.removeFromParent()
            }
        }
    }

    private val okButton by lazy { button("Ok") }
    private fun overlayCells() : Map<Button, CellEditor> {
        removeOverlayFromCells()
        val editorPanes = tablePane.children.parallelStream()
            .filter { it is StackPane }.map {it as StackPane }.collect(Collectors.toList())
        val buttons = mutableMapOf<Button, CellEditor>()
        for (pane in editorPanes) {
            val location = GridPane.getRowIndex(pane) to GridPane.getColumnIndex(pane)
            val editor = editors[location.first][location.second]
            with(pane) {
                this += button {
                    text = editor.cell[0]
                    setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
                    buttons[this] = editor
                }
            }
        }
        with(okButton) {
            action {
                removeOverlayFromCells()
                this.removeFromParent()
            }
        }
        editToolBar.root.items.add(okButton)
        return buttons
    }

    private fun removeOverlayFromCells() {
        tablePane.childrenUnmodifiable.stream()
            .map {it as? StackPane }.forEach { pane -> pane?.children?.removeIf { it is Button } }
        okButton.removeFromParent()
    }

    fun cleanRows() {
        val buttons = overlayCells()
        for (button in buttons.keys) {
            button.setOnAction {
                val rowIndex = GridPane.getRowIndex(button.parent)
                for ((index, editor) in editors[rowIndex].withIndex()) {
                    editor.cell.clean()
                    (tablePane.get(index, rowIndex) as? StackPane)?.children
                        ?.removeIf { it is Button }
                }
                button.removeFromParent()
            }
        }
    }

    fun cleanColumns() {
        val buttons = overlayCells()
        for (button in buttons.keys) {
            button.setOnAction {
                val columnIndex = GridPane.getColumnIndex(button.parent)
                for (row in 0 until editors.size) {
                    editors[row][columnIndex].cell.clean()
                    (tablePane.get(columnIndex, row) as? StackPane)?.children
                    ?.removeIf { it is Button }
                }
                button.removeFromParent()
            }
        }
    }
}

private fun GridPane.get(x: Int, y: Int) =
    try {
        children.filter { GridPane.getColumnIndex(it) == x && GridPane.getRowIndex(it) == y }[0]
    } catch(e: IndexOutOfBoundsException) { null }


private fun GridPane.remove(x: Int, y: Int) = children.remove(get(x, y))
