package me.dzikimlecz.timetables.components.fragments.editors

import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.scene.SnapshotParameters
import javafx.scene.control.Tab
import javafx.scene.transform.Transform
import me.dzikimlecz.timetables.components.fragments.editors.TimeTableEditor.Companion.ViewMode.EDIT
import me.dzikimlecz.timetables.components.fragments.editors.TimeTableEditor.Companion.ViewMode.VIEW
import me.dzikimlecz.timetables.components.fragments.toolbars.EditToolBar
import me.dzikimlecz.timetables.components.fragments.toolbars.ViewToolBar
import me.dzikimlecz.timetables.components.views.MainView
import me.dzikimlecz.timetables.components.views.dialogs.DetailsView
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*

class TimeTableEditor : Fragment() {
    val timeTable by param<TimeTable>()
    val tab by param<Tab>()
    private val viewModeProperty = SimpleObjectProperty(VIEW)
    var viewMode: ViewMode by viewModeProperty

    private val viewToolBar = find<ViewToolBar>(params = mapOf(ViewToolBar::parentEditor to this))
    private val editToolBar: EditToolBar by lazy {
        find(params = mapOf(EditToolBar::parentEditor to this))
    }
    private val toolBars by lazy {
        mapOf(VIEW to viewToolBar.root, EDIT to editToolBar.root)
    }
    private val okButton by lazy { button("Ok") }
    private var tableSection by singleAssign<TableSection>()

    override val root = borderpane {
        top = viewToolBar.root
        tableSection = find(params = mapOf(
            TableSection::timeTable to timeTable,
            TableSection::parentEditor to this@TimeTableEditor
        ))
        center = tableSection.root
    }

    init {
        tab.content = root
        tab.text = "${timeTable.name} : ${timeTable.date}"
        initListeners()
    }

    fun cleanCells() = tableSection.cleanCells()

    fun divideCells(direction: Orientation) = tableSection.divideCells(direction)

    fun cleanRows() =
        tableSection.cleanRows()

    fun cleanColumns() =
        tableSection.cleanColumns()

    fun exportTable() {
        val params = SnapshotParameters().apply {
            transform = Transform.scale(exportScale, exportScale)
        }
        val img = tableSection.root.snapshot(params, null)
        find<MainView>().manager.exportTableImage(img, timeTable.name)
    }

    fun adjustTimeSpans() =
        tableSection.adjustTimeSpans()

    fun adjustTitles() =
        tableSection.adjustTitles()


    fun showConfirmationButton(runnable: () -> Unit) {
        editToolBar.root.items += okButton
        okButton.setOnAction {
            runnable()
            okButton.removeFromParent()
        }
    }

    fun hideConfirmationButton() {
        okButton.setOnAction {  }
        okButton.removeFromParent()
    }

    fun closePane() {
        tab.removeFromParent()
        root.removeFromParent()
    }

    fun openDetailsWindow() =
        openInternalWindow<DetailsView>(params = mapOf(DetailsView::table to timeTable))

    private fun initListeners() =
        viewModeProperty.addListener { _, _, newVal ->
            root.top = toolBars[newVal]
            tableSection.changeViewMode(newVal)
        }

    companion object {
        enum class ViewMode {
            EDIT, VIEW
        }

        private const val exportScale = 2.0
    }
}
