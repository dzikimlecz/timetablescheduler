package me.dzikimlecz.timetables.timetable

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import kotlinx.serialization.Serializable
import me.dzikimlecz.timetables.timetable.json.CellSerializer
import tornadofx.getValue
import tornadofx.setValue

@Serializable(with = CellSerializer::class)
class Cell(isDivided : Boolean = false) {
    private val contents = Array(2) {
        SimpleStringProperty(this, "", "")
    }

    val isDividedProperty = SimpleBooleanProperty(isDivided)
    var isDivided by isDividedProperty

    operator fun set(subCell: Int = 0, content: String) {
        if ((!isDivided && subCell != 0) || subCell < 0 || subCell > 1)
            throw IndexOutOfBoundsException("There is no subCell of index $subCell")
        contents[subCell].set(content)
    }

    operator fun get(subCell: Int = 0) : String {
        if ((!isDivided && subCell != 0) || subCell < 0 || subCell > 1)
            throw IndexOutOfBoundsException("There is no subCell of index $subCell")
        return contents[subCell].get()
    }

    fun getContentProperty(subCell : Int) : SimpleStringProperty {
        return contents[subCell]
    }

    fun clean() {
        this[0] = ""
        if (isDivided) {
            this[1] = ""
            isDivided = false
        }
    }
}