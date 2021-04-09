package me.dzikimlecz.timetables.components.fragments

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos.CENTER
import javafx.geometry.Pos.TOP_CENTER
import javafx.geometry.Rectangle2D
import javafx.scene.SnapshotParameters
import javafx.scene.control.Button
import javafx.scene.control.Tab
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import javafx.scene.transform.Transform
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.Companion.ViewMode.EDIT
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.Companion.ViewMode.VIEW
import me.dzikimlecz.timetables.components.fragments.toolbars.EditToolBar
import me.dzikimlecz.timetables.components.fragments.toolbars.ViewToolBar
import me.dzikimlecz.timetables.components.views.MainView
import me.dzikimlecz.timetables.components.views.dialogs.TimeSpanAdjustView
import me.dzikimlecz.timetables.timetable.Cell
import me.dzikimlecz.timetables.timetable.TimeSpan
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*

private const val exportScale = 2.0

class TimeTableEditor : Fragment() {
    val timeTable by param<TimeTable>()
    val tab by param<Tab>()
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
                paddingTop = 20
                alignment = TOP_CENTER
                isGridLinesVisible = true
            }
            for (i in 0 until timeTable.columns) addTimeSpans(i)
            BorderPane.setMargin(tablePane, Insets(90.0, 25.0, 120.0, 25.0))
        }
    }

    init {
        for (y in 0 until timeTable.rows) {
            editors.add(ArrayList())
            for (x in 0 until timeTable.columns)
                addCell(x, y, timeTable[y][x])
        }
        tab.content = root
        tab.text = "${timeTable.name} : ${timeTable.date}"
        initListeners()
    }

    private fun initListeners() {
        viewModeProperty.addListener { _, _, newVal ->
            editors.forEach { list -> list.forEach { it.refreshView(viewMode) } }
            root.top = toolBars[newVal]
            removeOverlayFromCells()
        }

        timeTable.rowsProperty.addListener { _, _, newVal ->
            val newValue = newVal.toInt()
            for (i in editors.size until newValue) {
                editors.add(ArrayList())
                for (x in 0 until timeTable.columns) {
                    val y = editors.size - 1
                    addCell(x, y, timeTable[y][x])
                }
            }
            while (editors.size > newValue) {
                tablePane.removeRow(tablePane.get(editors.last().size - 1, editors.size)!!)
                editors.last().forEach { it.cell.clean() }
                editors.removeLast()
            }
        }

        timeTable.columnsProperty.addListener { _, _, newVal ->
            val newValue = newVal.toInt()
            while (editors.last().size < newValue) {
                for (y in 0 until editors.size)
                    addCell(editors.last().size, y, timeTable[y][editors.last().size])
                val columnIndex = editors.first().size - 1
                addTimeSpans(columnIndex)
            }
            while (editors.last().size > newValue) {
                tablePane.remove(editors.last().size - 1, 0)
                for ((y, row) in editors.withIndex()) {
                    tablePane.remove(editors.last().size - 1, y + 1)
                    row.last().cell.clean()
                    row.removeLast()
                }
            }
        }
    }

    private fun addCell(x: Int, y: Int, cell: Cell) {
        val editor = find<CellEditor>(mapOf(CellEditor::cell to cell))
        editors[y].add(editor)
        with(tablePane) {
            stackpane {
                maxWidthProperty().bind(
                    tablePane.maxWidthProperty() / timeTable.columnsProperty
                )
                maxHeightProperty().bind(
                    (tablePane.maxHeightProperty() - 20) / timeTable.rowsProperty
                )
                prefWidthProperty().bind(maxWidthProperty())
                this += editor.root
                editor.root.maxWidthProperty().bind(maxWidthProperty())
                editor.root.maxHeightProperty().bind(maxHeightProperty())
                gridpaneConstraints { columnRowIndex(x, y + 1) }
            }
        }
        editor.refreshView(viewMode)
    }

    private fun addTimeSpans(columnIndex: Int): Unit = with(tablePane) {
        if (columnIndex < 0) throw IndexOutOfBoundsException("Index $columnIndex out of bonds")
        remove(columnIndex, 0)
        stackpane {
            borderpane {
                val timeSpans = timeTable.columnsTimeSpan[columnIndex]
                timeSpans.addListener { _: ListChangeListener.Change<out TimeSpan?>? -> addTimeSpans(columnIndex) }
                val firstSpan = timeSpans[0]
                val secondSpan = timeSpans[1]
                (if (secondSpan == null) centerProperty() else leftProperty()).set(
                    label {
                        text = firstSpan?.toString() ?: "-/-"
                        alignment = CENTER
                        maxWidthProperty().bind(
                            this@with.maxWidthProperty() /
                                    timeTable.columnsProperty /
                                    (if (secondSpan == null) 2 else 1)
                        )
                        maxHeight = 20.0
                    }
                )

                if (secondSpan != null) right {
                    label {
                        text = secondSpan.toString()
                        alignment = CENTER
                        maxWidthProperty().bind(
                            this@with.maxWidthProperty() / timeTable.columnsProperty / 2
                        )
                        maxHeight = 20.0
                    }
                }
            }
            gridpaneConstraints { columnRowIndex(columnIndex, 0) }
        }
    }

    fun cleanCells() = handleCellsOverlayingAction { clean() }

    fun divideCells(direction: Orientation) = handleCellsOverlayingAction { divisionDirection = direction }

    fun cleanRows() {
        val buttons = overlayCells()
        for (button in buttons.keys) {
            button.setOnAction {
                val rowIndex = GridPane.getRowIndex(button.parent)
                for ((index, editor) in editors[rowIndex].withIndex()) {
                    editor.cell.clean()
                    (tablePane.get(index, rowIndex + 1) as? StackPane)?.children?.removeIf { it is Button }
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
                    (tablePane.get(columnIndex, row + 1) as? StackPane)?.children?.removeIf { it is Button }
                }
                button.removeFromParent()
            }
        }
    }

    fun exportTable() {
        val params = SnapshotParameters()
        params.transform = Transform.scale(exportScale, exportScale)
        params.viewport = generateViewPort()
        val img = tablePane.snapshot(params, null)
        find<MainView>().manager.exportTableImage(img, timeTable.name)
    }

    private fun generateViewPort() = with(tablePane) {
        val margin = paddingTop.toDouble()
        var x = 0
        var width = .0
        while (true)
            width += get(x++, 0)?.boundsInParent?.width ?: break
        var y = 0
        var height = .0
        while (true)
            height += get(0, y++)?.boundsInParent?.width ?: break
        Rectangle2D(
            boundsInParent.minX * exportScale - margin,
            boundsInParent.minY * exportScale,
            width * exportScale + 3 * margin,
            height * exportScale + 3 * margin
        )
    }

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

    private fun overlayCells(): Map<Button, CellEditor> {
        val map = overlayGrid { (GridPane.getRowIndex(it) ?: 0) != 0 }
            .mapValues { (_, location) -> editors[location.first - 1][location.second] }
        for ((button, editor) in map)
            button.text = editor.cell[0]
        return map
    }

    private fun overlayGrid(predicate: (StackPane) -> Boolean = { true } ): Map<Button, Pair<Int, Int>> {
        removeOverlayFromCells()
        val editorPanes = tablePane.children.filterIsInstance<StackPane>()
        val buttons = mutableMapOf<Button, Pair<Int, Int>>()
        for (pane in editorPanes) {
            if (predicate(pane)) {
                val location = GridPane.getRowIndex(pane) to GridPane.getColumnIndex(pane)
                with(pane) {
                    this += button {
                        setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
                        buttons[this] = location
                    }
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
        tablePane.children.filterIsInstance<StackPane>().forEach { pane ->
            pane.children.removeIf { it is Button }
        }
        okButton.removeFromParent()
    }


    fun closePane() {
        tab.removeFromParent()
        root.removeFromParent()
    }

    fun adjustTimeSpans() = with(tablePane) {
        val buttons = overlayGrid { GridPane.getRowIndex(it) == 0 }
        for ((i, button) in buttons.keys.withIndex()) button.setOnAction {
            this@TimeTableEditor.openInternalWindow<TimeSpanAdjustView>(
                movable = false,
                params = mapOf(
                    TimeSpanAdjustView::column to i,
                    TimeSpanAdjustView::table to timeTable,
                )
            )
            button.removeFromParent()
        }
    }

    companion object {
        enum class ViewMode {
            EDIT, VIEW
        }
    }
}

private fun GridPane.get(x: Int, y: Int) =
    try {
        children.first { GridPane.getColumnIndex(it) == x && GridPane.getRowIndex(it) == y }
    } catch(e: NoSuchElementException) { null }

private fun GridPane.remove(x: Int, y: Int) = children.remove(get(x, y))
