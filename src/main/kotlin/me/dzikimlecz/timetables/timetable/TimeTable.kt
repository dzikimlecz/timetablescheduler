package me.dzikimlecz.timetables.timetable

import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.dzikimlecz.timetables.timetable.json.TimeTableSerializer
import tornadofx.getValue
import tornadofx.setValue
import java.time.LocalDate

@Serializable(with = TimeTableSerializer::class)
class TimeTable(
    columns: Int,
    rows: Int,
    @Required var date: LocalDate = LocalDate.now(),
    @Required var name: String = ""
) : Iterable<ObservableList<Cell>> {


    val columnsProperty = SimpleIntegerProperty(0)
    var columns by columnsProperty
    init {
        columnsProperty.addListener {observable, old, new ->
            val newValue = new.toInt()
            val oldValue = old.toInt()
            if (newValue <= 0) {
                (observable as SimpleIntegerProperty).set(oldValue)
                throw IllegalArgumentException("Illegal Table Size: $newValue columns")
            }
            val diff = newValue - oldValue
            if (diff > 0)
                for (row in table)
                    for (i in 1..diff) row.add(Cell())
            else if (diff < 0)
                for (row in table)
                    for (i in 1..diff) row.removeLast()
        }
    }

    val columnsTimeSpan: ObservableList<TimeSpan> = FXCollections.observableArrayList()
    init {
        columnsProperty.addListener { _, _, newVal ->
            val newValue = newVal.toInt()
            while(columnsTimeSpan.size > newValue) columnsTimeSpan.removeLast()
            while(columnsTimeSpan.size < newValue) columnsTimeSpan += null
        }
    }

    val rowsProperty = SimpleIntegerProperty(0)
    var rows by rowsProperty
    init {
        rowsProperty.addListener { observable, old, new ->
            val newValue = new.toInt()
            val oldValue = old.toInt()
            if (newValue <= 0) {
                (observable as SimpleIntegerProperty).set(oldValue)
                throw IllegalArgumentException("Illegal Table Size: $newValue rows")
            }
            val diff = newValue - oldValue
            rowsProperty.set(newValue)
            if (diff > 0) {
                for (i in 0 until diff) {
                    val newRow = FXCollections.observableArrayList<Cell>()
                    for (j in 0 until columns) newRow.add(Cell())
                    table.add(newRow)
                }
            } else if (diff < 0)
                for (i in 1..diff) table.removeLast()
        }
    }

    private val table = FXCollections.observableArrayList<ObservableList<Cell>>()

    init {
        this.columns = columns
        this.rows = rows
    }

    operator fun get(i : Int) : ObservableList<Cell> {
        if (i >= rows || i < 0) throw IndexOutOfBoundsException("Index $i out of bonds $rows")
        return table[i]
    }

    override fun iterator() : Iterator<ObservableList<Cell>> = table.iterator()

    fun list(): List<List<Cell>> = table.toList()
}

fun timeTableOf(table: List<List<Cell>>) : TimeTable {
    require(table.isNotEmpty() && table.stream().allMatch {it.size == table[0].size})
        {"This list is not a table!"}
    val timeTable = TimeTable(table[0].size, table.size)
    for ((y, row) in table.withIndex())
        for((x, cell) in row.withIndex())
            timeTable[y][x] = cell
    return timeTable
}