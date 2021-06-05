package me.dzikimlecz.timetables.components.fragments

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener.Change
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos.CENTER
import javafx.geometry.Pos.TOP_CENTER
import javafx.geometry.Rectangle2D
import javafx.scene.SnapshotParameters
import javafx.scene.control.Button
import javafx.scene.control.Tab
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.GridPane.getColumnIndex
import javafx.scene.layout.GridPane.getRowIndex
import javafx.scene.layout.StackPane
import javafx.scene.text.TextAlignment
import javafx.scene.transform.Transform
import javafx.stage.StageStyle
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.Companion.ViewMode.EDIT
import me.dzikimlecz.timetables.components.fragments.TimeTableEditor.Companion.ViewMode.VIEW
import me.dzikimlecz.timetables.components.fragments.toolbars.EditToolBar
import me.dzikimlecz.timetables.components.fragments.toolbars.ViewToolBar
import me.dzikimlecz.timetables.components.get
import me.dzikimlecz.timetables.components.locate
import me.dzikimlecz.timetables.components.margin
import me.dzikimlecz.timetables.components.remove
import me.dzikimlecz.timetables.components.views.MainView
import me.dzikimlecz.timetables.components.views.dialogs.TimeSpanAdjustView
import me.dzikimlecz.timetables.timetable.Cell
import me.dzikimlecz.timetables.timetable.TimeSpan
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*
import java.time.format.DateTimeFormatter
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


class TimeTableEditor : Fragment() {
    val timeTable by param<TimeTable>()
    val tab by param<Tab>()
    private val viewModeProperty = SimpleObjectProperty(VIEW)
    private val editors: MutableList<MutableList<CellEditor>> = ArrayList()
    private var tablePane by singleAssign<GridPane>()

    private val viewToolBar = find<ViewToolBar>(params = mapOf(ViewToolBar::parentEditor to this))

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
                for (x in 0..timeTable.columns)
                    stackpane { borderpane(); gridpaneConstraints { columnRowIndex(x, 0) } }
                margin = Insets(90.0, 25.0, 120.0, 25.0)
            }
        }
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        for (y in 0 until timeTable.rows) {
            tablePane.label {
                text = timeTable.date.plusDays(y.toLong()).format(dateFormatter)
                gridpaneConstraints { columnRowIndex(0, y + 1) }
                paddingAll = 5
            }
            editors.add(ArrayList())
            for (x in 0 until timeTable.columns)
                addCell(x, y, timeTable[y][x])
        }
        for (x in 0 until timeTable.columns) {
            addTimeSpans(x)
            addTitle(x)
        }
    }

    init {
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
                    this@with.maxWidthProperty() / (timeTable.columnsProperty + 1)
                )
                maxHeightProperty().bind(
                    (this@with.maxHeightProperty() - 20) / (timeTable.rowsProperty + 1)
                )
                prefWidthProperty().bind(maxWidthProperty())
                this += editor.root
                editor.root.maxWidthProperty().bind(maxWidthProperty())
                editor.root.maxHeightProperty().bind(maxHeightProperty())
                gridpaneConstraints { columnRowIndex(x + 1, y + 1) }
            }
        }
        editor.refreshView(viewMode)
    }

    private fun addTitle(x: Int) {
        val columnIndex = x + 1
        if (columnIndex < 0) throw IndexOutOfBoundsException(columnIndex)
        val stackPane = tablePane.get(columnIndex, 0) as StackPane
        with(stackPane) {
            maxWidthProperty().bind(
                tablePane.maxWidthProperty() / (timeTable.columnsProperty + 1)
            )
            maxHeightProperty().bind(
                (tablePane.maxHeightProperty() - 20) / (timeTable.rowsProperty + 1)
            )
        }
        val borderPane = stackPane.children.filterIsInstance<BorderPane>().first()
        with(borderPane) {
            top = label {
                textProperty().bind(timeTable.titles[x])
                textAlignment = TextAlignment.CENTER
                maxWidthProperty().bind(this@with.widthProperty())
                maxHeight = 20.0
                margin = Insets(5.0, 5.0, 2.5, 5.0)
                isWrapText = true
            }
        }
    }

    private fun addTimeSpans(column: Int) {
        val columnIndex = column + 1
        if (columnIndex < 0) throw IndexOutOfBoundsException(columnIndex)
        val stackPane = tablePane.get(columnIndex, 0) as StackPane
        with(stackPane) {
            maxWidthProperty().bind(
                tablePane.maxWidthProperty() / (timeTable.columnsProperty + 1)
            )
            maxHeightProperty().bind(
                (tablePane.maxHeightProperty() - 20) / (timeTable.rowsProperty + 1)
            )
        }
        val borderPane = stackPane.children.first { it is BorderPane } as BorderPane
        with(borderPane) {
            val timeSpans = timeTable.columnsTimeSpan[column]
            timeSpans.addListener { _: Change<out TimeSpan?>? -> addTimeSpans(column) }
            val firstSpan = timeSpans[0]
            val secondSpan = timeSpans[1]
            left = label {
                text = firstSpan?.toString() ?: "-/-"
                alignment = CENTER
                maxWidthProperty().bind(
                    this@with.widthProperty() /
                            (if (secondSpan == null) 2 else 1)
                )
                maxHeightProperty().bind(
                    this@with.heightProperty() / 2
                )
                margin = Insets(2.5, 5.0, 5.0, 2.5 )
            }

            if (secondSpan != null) right = label {
                text = secondSpan.toString()
                alignment = CENTER
                maxWidthProperty().bind(
                    this@with.widthProperty() / 2
                )
                maxHeightProperty().bind(
                    this@with.heightProperty() / 2
                )
                margin = Insets(2.5, 2.5, 5.0, 5.0)
            }
        }
    }

    fun cleanCells() = handleCellsOverlayingAction { clean() }

    fun divideCells(direction: Orientation) = handleCellsOverlayingAction { divisionDirection = direction }

    fun cleanRows() {
        val buttons = overlayCells()
        for (button in buttons.keys) {
            button.setOnAction {
                val rowIndex = getRowIndex(button.parent)
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
                val columnIndex = getColumnIndex(button.parent)
                for (row in 0 until editors.size) {
                    editors[row][columnIndex].cell.clean()
                    (getCell(columnIndex, row) as? StackPane)?.children?.removeIf { it is Button }
                }
                button.removeFromParent()
            }
        }
    }

    fun exportTable() {
        val params = SnapshotParameters().apply {
            transform = Transform.scale(exportScale, exportScale)
//            viewport = generateViewPort()
        }
        val img = tablePane.snapshot(params, null)
        find<MainView>().manager.exportTableImage(img, timeTable.name)
    }

    @Deprecated("Doesn't work") private fun generateViewPort() = with(tablePane) {
        // FIXME: 10.05.2021 Doesn't consider width of elements' margins. Needs complete rethink
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
        val map = overlayGrid { (getRowIndex(it) ?: -1) > 0 && (getColumnIndex(it) ?: -1) > 0 }
            .mapValues { (_, location) ->
                val (y, x) = location
                editors[y - 1][x - 1]
            }
        for ((button, editor) in map)
            button.text = editor.cell[0]
        return map
    }

    private fun overlayGrid(predicate: (StackPane) -> Boolean = { true } ): Map<Button, Pair<Int, Int>> {
        // ensures that cells are overlaid exactly once
        removeOverlayFromCells()
        val buttons = mutableMapOf<Button, Pair<Int, Int>>()
        // overlays and add button with it coordinates to buttons map
        tablePane.editorPanes().filter(predicate).forEach {
            it += button {
                setMaxSize(POSITIVE_INFINITY, POSITIVE_INFINITY)
                buttons[this] = locate(it)
            }
        }
        okButton.action {
            removeOverlayFromCells()
            okButton.removeFromParent()
        }
        editToolBar.root.items += okButton
        return buttons
    }

    private fun removeOverlayFromCells() {
        tablePane.editorPanes().forEach { pane ->
            pane.children.removeIf { it is Button }
        }
        okButton.removeFromParent()
    }

    fun closePane() {
        tab.removeFromParent()
        root.removeFromParent()
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

    private fun getCell(x: Int, y: Int) =
        tablePane.get(x + 1, y + 1)

    fun adjustTitles() {
        val buttons = overlayGrid {
            val (y, x) = locate(it)
            x != 0 && y == 0
        }
        for ((i, button) in buttons.keys.withIndex()) button.setOnAction {
            val result = TextInputDialog().apply {
                headerText = "Zmiana nazwy zajęć"
                contentText = "Podaj nazwę zajęć dla tej kolumny"
            }.showAndWait().orElseGet { "" }
            timeTable.titles[i].set(result)
            button.removeFromParent()
        }
    }

    companion object {
        enum class ViewMode {
            EDIT, VIEW
        }

        private const val exportScale = 2.0

        fun GridPane.editorPanes() =
            children.filterIsInstance<StackPane>()
    }
}
