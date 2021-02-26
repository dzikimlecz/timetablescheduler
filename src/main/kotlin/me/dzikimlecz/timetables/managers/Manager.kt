package me.dzikimlecz.timetables.managers

import me.dzikimlecz.timetables.components.views.ExportView
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.find
import java.time.LocalDate

class Manager {
    private lateinit var lastTable : TimeTable
    private val filesManager by lazy { FilesManager() }

    val activeTable : TimeTable
        get() = lastTable

    fun newTable(properties: Map<String, String>): TimeTable {
        val columns = (properties["columns"] ?: badProperty("columns", true))
            .toIntOrNull() ?: badProperty("columns", false)
        val rows = (properties["rows"] ?: badProperty("rows", true))
            .toIntOrNull() ?: badProperty("rows", false)
        val name = properties["name"] ?: badProperty("name", true)
        val date = try {
            LocalDate.parse(properties["date"] ?: badProperty("date", true))
        } catch (e: Exception) {
            badProperty("date", false)
        }
        lastTable = TimeTable(columns, rows, date, name)
        return lastTable
    }

    fun exportPlan() {
        if (filesManager.getProperFile(lastTable).exists())
            try {
                filesManager.saveTable(lastTable)
            } catch (e: FileAlreadyExistsException) {
                describedExport()
            }
        else describedExport()
    }

    private fun describedExport() {
        val properties = mutableMapOf<String, String>()
        find<ExportView>(params = mapOf(ExportView::exportProperties to properties))
            .openModal(block = true)
        filesManager.saveTable(lastTable, enforce = true)

    }

    private val badProperty = { name : String, missing: Boolean ->
        val cause = if (missing) "Missing" else "Wrongly formatted"
        throw IllegalArgumentException("$cause property: $name")
    }


}
