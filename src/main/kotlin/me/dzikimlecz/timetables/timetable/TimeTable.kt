package me.dzikimlecz.timetables.timetable

import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import tornadofx.getValue
import tornadofx.setValue
import tornadofx.sizeProperty
import java.time.LocalDate
import javax.naming.OperationNotSupportedException

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

    val columnsTimeSpan: ObservableList<ObservableList<TimeSpan?>> = FXCollections.observableArrayList()
    init {
        columnsProperty.addListener { _, _, newVal ->
            val newValue = newVal.toInt()
            while(columnsTimeSpan.size > newValue) columnsTimeSpan.removeLast()
            while(columnsTimeSpan.size < newValue) {
                val element: ObservableList<TimeSpan?> = FXCollections.observableArrayList(null, null)
                element.sizeProperty.addListener { _, _, _, ->
                    throw OperationNotSupportedException("Lists of TimeSpans must have fixed size.")
                }
                columnsTimeSpan.add(element)
            }
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

    val list: List<List<Cell>>
        get() = table.toList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TimeTable) return false

        if (date != other.date) return false
        if (name != other.name) return false
        if (columns != other.columns) return false
        if (columnsTimeSpan != other.columnsTimeSpan) return false
        if (rows != other.rows) return false
        if (table != other.table) return false

        return true
    }

    override fun hashCode(): Int {
        var result = date.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + columnsTimeSpan.hashCode()
        result = 31 * result + (table?.hashCode() ?: 0)
        return result
    }

    fun softEquals(other: Any?) =
        this === other || (other is TimeTable && other.name == name && other.date == date)

}

fun timeTableOf(table: List<List<Cell>>) : TimeTable {
    require(table.isEmpty() || table.stream().allMatch {it.size == table[0].size})
        {"This list is not a table!"}
    val timeTable = TimeTable(if (table.isNotEmpty()) table[0].size else 0, table.size)
    for ((y, row) in table.withIndex())
        for((x, cell) in row.withIndex())
            timeTable[y][x] = cell
    return timeTable
}