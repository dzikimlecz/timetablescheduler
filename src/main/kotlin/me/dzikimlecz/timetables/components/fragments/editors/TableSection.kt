package me.dzikimlecz.timetables.components.fragments.editors

import javafx.collections.ListChangeListener
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.geometry.Pos.CENTER
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.layout.StackPane
import javafx.scene.text.TextAlignment
import javafx.stage.StageStyle
import me.dzikimlecz.timetables.components.get
import me.dzikimlecz.timetables.components.locate
import me.dzikimlecz.timetables.components.margin
import me.dzikimlecz.timetables.components.remove
import me.dzikimlecz.timetables.components.views.dialogs.TimeSpanAdjustView
import me.dzikimlecz.timetables.timetable.Cell
import me.dzikimlecz.timetables.timetable.TimeSpan
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*
import java.time.format.DateTimeFormatter

class TableSection: Fragment() {
    val timeTable by param<TimeTable>()
    val parentEditor by param<TimeTableEditor>()
    private val editors: MutableList<MutableList<CellEditor>> = ArrayList()

    override val root = gridpane {
        maxWidthProperty().bind(primaryStage.widthProperty() - 230)
        maxHeightProperty().bind(primaryStage.heightProperty() - 230)
        margin = Insets(90.0, 25.0, 120.0, 25.0)
        paddingTop = 20
        alignment = Pos.TOP_CENTER
        isGridLinesVisible = true
    }

    init {
        for (x in 0..timeTable.columns)
            root.addLabels(x)
        for (y in 0 until timeTable.rows) {
            root.addDate(y)
            addCellsRow(y)
        }
        initListeners()
    }

    private fun initListeners() {

        timeTable.rowsProperty.addListener { _, _, newVal ->
            val newValue = newVal.toInt()
            for (i in editors.size until newValue) {
                editors.add(ArrayList())
                for (x in 0 until timeTable.columns) {
                    val y = editors.size - 1
                    root.addCell(x, y, timeTable[y][x])
                }
                root.addDate(i)
            }
            while (editors.size > newValue) {
                root.removeRow(root[editors.last().size - 1, editors.size]!!)
                editors.last().forEach { it.cell.clean() }
                editors.removeLast()
            }
        }

        timeTable.columnsProperty.addListener { _, _, newVal ->
            //fixme exception thrown on columns 0->1
            val newValue = newVal.toInt()
            while (editors.last().size < newValue) {
                for (y in 0 until editors.size) {
                    root.addCell(editors.last().size, y, timeTable[y][editors.last().size])
                }
                val columnIndex = editors.first().lastIndex
                root.stackpane { borderpane(); gridpaneConstraints { columnRowIndex(columnIndex + 1, 0) } }
                loadTimeSpans(columnIndex)
                addTitle(columnIndex)
            }
            while (editors.last().size > newValue) {
                root.remove(editors.last().size, 0 )
                for (y in 0 until timeTable.rows) {
                    root.remove(editors.last().size, y + 1)
                    editors[y].last().cell.clean()
                    editors[y].removeLast()
                }
            }
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
                    (getCell(index, rowIndex) as? StackPane)?.children?.removeIf { it is Button }
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
                    (getCell(columnIndex, row) as? StackPane)?.children?.removeIf { it is Button }
                }
                button.removeFromParent()
            }
        }
    }

    fun adjustTimeSpans() {
        val buttons = overlayGrid {
            val (y, x) = locate(it)
            x != 0 && y == 0
        }
        for ((i, button) in buttons.keys.withIndex()) button.setOnAction {
            find<TimeSpanAdjustView>(
                params = mapOf(
                    TimeSpanAdjustView::column to i,
                    TimeSpanAdjustView::table to timeTable,
                )
            ).openModal(stageStyle = StageStyle.UTILITY, resizable = false)
            button.removeFromParent()
        }
    }

    fun adjustTitles() {
        val buttons = overlayGrid {
            val (y, x) = locate(it)
            x != 0 && y == 0
        }
        for ((i, button) in buttons.keys.withIndex())
            button.action {
                TextInputDialog().apply {
                    headerText = "Zmiana nazwy zajęć"
                    contentText = "Podaj nazwę zajęć dla tej kolumny"
                }.showAndWait().ifPresent {
                    timeTable.titles[i].set(it)
                    button.removeFromParent()
                }
            }
    }

    fun changeViewMode(viewMode: TimeTableEditor.Companion.ViewMode) {
        for (list in editors) list.forEach { it.refreshView(viewMode) }
        removeOverlayFromCells()
    }

    private fun GridPane.addDate(y: Int) {
        label {
            text = timeTable.date.plusDays(y.toLong()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            gridpaneConstraints { columnRowIndex(0, y + 1) }
            paddingAll = 5
        }
    }

    private fun GridPane.addLabels(x: Int) {
        stackpane {
            borderpane()
            maxWidthProperty().bind(
                this@addLabels.maxWidthProperty() / (timeTable.columnsProperty + 1)
            )
            maxHeightProperty().bind(
                (this@addLabels.maxHeightProperty() - 20) / (timeTable.rowsProperty + 1)
            )
            gridpaneConstraints { columnRowIndex(x, 0) }
        }
        if (x != 0) {
            loadTimeSpans(x)
            addTitle(x)
        }
    }

    private fun addCellsRow(y: Int) {
        editors += ArrayList<CellEditor>()
        for (x in 0 until timeTable.columns)
            root.addCell(x, y, timeTable[y][x])
    }

    private fun GridPane.addCell(x: Int, y: Int, cell: Cell) {
        val editor = find<CellEditor>(mapOf(CellEditor::cell to cell))
        editors[y].add(editor)
        stackpane {
            maxWidthProperty().bind(
                this@addCell.maxWidthProperty() / (timeTable.columnsProperty + 1)
            )
            maxHeightProperty().bind(
                (this@addCell.maxHeightProperty() - 20) / (timeTable.rowsProperty + 1)
            )
            prefWidthProperty().bind(maxWidthProperty())
            this += editor.root
            editor.root.maxWidthProperty().bind(maxWidthProperty())
            editor.root.maxHeightProperty().bind(maxHeightProperty())
            gridpaneConstraints { columnRowIndex(x + 1, y + 1) }
        }
        editor.refreshView(parentEditor.viewMode)
    }

    private fun addTitle(column: Int) {
        if (column <= 0) throw IndexOutOfBoundsException("$column")
        val stackPane = root[column, 0] as StackPane
        val borderPane = stackPane.children.filterIsInstance<BorderPane>().first()
        borderPane.top = label(timeTable.titles[column - 1]) {
            margin = Insets(5.0, 5.0, 2.5, 5.0)
            textAlignment = TextAlignment.CENTER
            maxWidthProperty().bind(borderPane.widthProperty())
            isWrapText = true
        }
    }

    private fun loadTimeSpans(column: Int) {
        if (column <= 0) throw IndexOutOfBoundsException("$column")
        val timeSpans = timeTable.columnsTimeSpan[column - 1]
        timeSpans.addListener { _: ListChangeListener.Change<out TimeSpan> -> loadTimeSpans(column) }
        val stackPane = root[column, 0] as StackPane
        val borderPane = stackPane.children.filterIsInstance<BorderPane>().first()
        borderPane.left = label(timeSpans[0]?.toString() ?: "-/-") {
            alignment = CENTER
            maxWidthProperty().bind(borderPane.widthProperty() /
                    (if (timeSpans[1] != null) 2 else 1))
            maxHeightProperty().bind(borderPane.heightProperty() / 2)
            margin = Insets(2.5, 5.0, 5.0, 2.5 )
        }
        if (timeSpans[1] != null)
            borderPane.right = label(timeSpans[1].toString()) {
                alignment = CENTER
                maxWidthProperty().bind(
                    borderPane.widthProperty() / 2
                )
                maxHeightProperty().bind(borderPane.heightProperty() / 2)
                margin = Insets(2.5, 2.5, 5.0, 5.0)
            }
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

    private fun overlayCells(): Map<Button, CellEditor> {
        val map = overlayGrid {
            val (y, x) = locate(it)
            (y ?: -1) > 0 && (x ?: -1) > 0
        }
            .mapValues { (_, location) ->
                val (y, x) = location
                editors[y - 1][x - 1]
            }
        for ((button, editor) in map)
            button.text = editor.cell[0]
        return map
    }

    private fun overlayGrid(predicate: (StackPane) -> Boolean = { true } ): Map<Button, Pair<Int, Int>> {
        removeOverlayFromCells()
        val buttons = mutableMapOf<Button, Pair<Int, Int>>()
        // overlays button and add it with its coordinates to buttons' map
        root.editorPanes().filter(predicate).forEach {
            it += button {
                hgrow = ALWAYS
                vgrow = ALWAYS
                buttons[this] = locate(it)
            }
        }
        parentEditor.showConfirmationButton(this::removeOverlayFromCells)
        return buttons
    }

    private fun removeOverlayFromCells() {
        root.editorPanes().forEach { pane ->
            pane.children.removeIf { it is Button }
        }
        parentEditor.hideConfirmationButton()
    }

    private fun getCell(x: Int, y: Int): Node? =
        root[x + 1, y + 1]

}

private fun GridPane.editorPanes() =
    children.filterIsInstance<StackPane>()
