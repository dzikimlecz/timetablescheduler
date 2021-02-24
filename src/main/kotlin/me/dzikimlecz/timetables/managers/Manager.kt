package me.dzikimlecz.timetables.managers

import me.dzikimlecz.timetables.timetable.TimeTable
import java.time.LocalDate

class Manager {
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
        return TimeTable(columns, rows, date, name)
    }

    private val badProperty = { name : String, missing: Boolean ->
        val cause = if (missing) "Missing" else "Wrongly formatted"
        throw IllegalArgumentException("$cause property: $name")
    }


}
