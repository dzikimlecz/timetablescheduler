package me.dzikimlecz.timetables.timetable

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections.observableArrayList
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


    val columnsProperty = SimpleIntegerProperty(-1)
    var columns by columnsProperty

    val columnsTimeSpan: ObservableList<ObservableList<TimeSpan?>> = observableArrayList()

    val titles: ObservableList<SimpleStringProperty> = observableArrayList()

    init {
        columnsProperty.addListener { observable, old, new ->
            val newValue = new.toInt()
            val oldValue = old.toInt()
            require (newValue >= 0) {
                (observable as SimpleIntegerProperty).set(oldValue)
                "Illegal Table Size: $newValue columns"
            }

            while(titles.size > newValue) titles.removeLast()
            while(titles.size < newValue) titles += SimpleStringProperty("")

            while(columnsTimeSpan.size > newValue) columnsTimeSpan.removeLast()
            while(columnsTimeSpan.size < newValue) {
                val element: ObservableList<TimeSpan?> = observableArrayList(null, null)
                element.sizeProperty.addListener { _, _, _ ->
                    throw OperationNotSupportedException("Lists of TimeSpans must have fixed size.")
                }
                columnsTimeSpan += element
            }

            val size = { table.firstOrNull()?.size ?: newValue }
            while (size() < newValue)
                table.forEach { it.add(Cell()) }
            while (size() > newValue)
                table.forEach { it.removeLast() }

        }
    }

    val rowsProperty = SimpleIntegerProperty(0)
    var rows by rowsProperty
    init {
        rowsProperty.addListener { observable, old, new ->
            val newValue = new.toInt()
            val oldValue = old.toInt()
            require (newValue >= 0) {
                (observable as SimpleIntegerProperty).set(oldValue)
                "Illegal Table Size: $newValue rows"
            }
            while (table.size < newValue) {
                val newRow = observableArrayList<Cell>()
                for (j in 0 until (this.columns.takeUnless { it < 0 } ?: columns)) newRow.add(Cell())
                table.add(newRow)
            }
            while (table.size > newValue)  table.removeLast()
        }
    }

    private val table = observableArrayList<ObservableList<Cell>>()

    init {
        this.rows = rows
        this.columns = columns
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

       return date == other.date
               && name == other.name
               && columns == other.columns
               && columnsTimeSpan == other.columnsTimeSpan
               && rows == other.rows
               && table == other.table

    }

    override fun hashCode(): Int {
        var result = date.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + columnsTimeSpan.hashCode()
        result = 31 * result + (table?.hashCode() ?: 0)
        return result
    }

    fun softEquals(other: Any) =
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