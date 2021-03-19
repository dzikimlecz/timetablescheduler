package me.dzikimlecz.timetables.managers

import javafx.scene.control.Alert
import javafx.stage.StageStyle.UTILITY
import me.dzikimlecz.timetables.components.views.ExportView
import me.dzikimlecz.timetables.components.views.ImportView
import me.dzikimlecz.timetables.components.views.MainView
import me.dzikimlecz.timetables.components.views.TimeTableSetUpView
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.alert
import tornadofx.find
import java.time.LocalDate
import kotlin.reflect.KProperty1

class Manager {
    private lateinit var lastTable : TimeTable
    private val filesManager by lazy { FilesManager() }

    val activeTable : TimeTable
        get() = lastTable

    private fun newTable(properties: Map<KProperty1<TimeTable, Any>, String>): TimeTable {
        val columns = (properties[TimeTable::columns] ?: badProperty("columns", true))
            .toIntOrNull() ?: badProperty("columns", false)
        val rows = (properties[TimeTable::rows] ?: badProperty("rows", true))
            .toIntOrNull() ?: badProperty("rows", false)
        val name = properties[TimeTable::name] ?: badProperty("name", true)
        val date = try {
            LocalDate.parse(properties[TimeTable::date] ?: badProperty("date", true))
        } catch (e: Exception) {
            badProperty("date", false)
        }
        lastTable = TimeTable(columns, rows, date, name)
        return lastTable
    }

    fun exportTable() = try { filesManager.saveTable(lastTable) }
        catch (e: FileAlreadyExistsException) { describedExport() }


    fun describedExport() {
        val properties = mutableMapOf<String, String>()
        find<ExportView>(params = mapOf(ExportView::exportProperties to properties)).openModal(
            UTILITY, block = true, resizable = false
        )
        val customName = properties["name"] ?: return
        val customPath = properties["path"] ?: return
        if (customName != "\u0000" && customPath != "\u0000")
            filesManager.saveTable(lastTable, customPath, true, customName)
        else if (customPath != "\u0000")
            filesManager.saveTable(lastTable, customPath, true)
        else if (customName != "\u0000")
            filesManager.saveTable(lastTable, enforce = true, name = customName)
        else filesManager.saveTable(lastTable, enforce = true)
    }

    fun importTable() {
        val importView = find<ImportView>(params = mapOf(ImportView::filesManager to filesManager))
        importView.openModal(block = true, resizable = false)
        if (importView.chosenFile == null)  {
            alert(Alert.AlertType.ERROR, "Nie wybrano pliku")
            return
        }
        val table = try {
             filesManager.readTable(importView.chosenFile!!)
        } catch(e: Exception) {
            alert(Alert.AlertType.ERROR,"Błąd odczytu", e.message)
            return
        }
        lastTable = table
        find<MainView>().displayTable(table)
    }

    fun setUpTable() {
        val map = mutableMapOf<KProperty1<TimeTable, Any>, String>()
        find<TimeTableSetUpView>(params = mapOf(TimeTableSetUpView::tableProperties to map))
            .openModal(UTILITY, resizable = false, block = true)
        try { find<MainView>().displayTable(newTable(map)) } catch (ignore : Exception) {}
    }

    fun openDB() {
        TODO("Not yet implemented")
    }

    private val badProperty = { name : String, missing: Boolean ->
        val cause = if (missing) "Missing" else "Wrongly formatted"
        throw IllegalArgumentException("$cause property: $name")
    }


}
