package me.dzikimlecz.timetable

import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

class TimeTable(columns: Int, rows: Int) : Iterable<ObservableList<Cell>> {
    var columns = 0
        set(value) {
            if (value <= 0) throw IllegalArgumentException("Illegal Table Size")
            val diff = value - field
            field = value
            columnsProperty.set(value)
            if (diff > 0)
                for (row in table)
                    for (i in 1..diff) row.add(Cell())
            else if (diff < 0)
                for (row in table)
                    for (i in 1..diff) row.removeLast()
        }
    var rows = 0
        set(value) {
            if (value <= 0) throw IllegalArgumentException("Illegal Table Size")
            val diff = value - field
            field = value
            rowsProperty.set(value)
            if (diff > 0) {
                for (i in 0 until diff) {
                    val newRow = FXCollections.observableArrayList<Cell>()
                    for (j in 0 until columns) newRow.add(Cell())
                    table.add(newRow)
                }
            } else if (diff < 0)
                for (i in 1..diff) table.removeLast()
        }
    val columnsProperty = SimpleIntegerProperty(columns)
    val rowsProperty = SimpleIntegerProperty(rows)

    private val table = FXCollections.observableArrayList<ObservableList<Cell>>()

    init {
        this.columns = columns
        this.rows = rows
        for (i in 0 until rows) {
            val row = FXCollections.observableArrayList<Cell>()
            for (j in 0 until columns) row.add(Cell())
            table.add(row)
        }
    }

    operator fun get(i : Int) : ObservableList<Cell> {
        if (i >= rows || i < 0) throw IndexOutOfBoundsException("Index $i out of bonds $rows")
        return table[i]
    }

    override fun iterator() : Iterator<ObservableList<Cell>> = table.iterator()

    }