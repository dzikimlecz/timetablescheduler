package me.dzikimlecz.timetable

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty

class Cell(isDivided : Boolean = false) {
    private val contents = arrayOf(
        SimpleStringProperty(this, ""),
        SimpleStringProperty(this, "")
    )

    val isDivided = SimpleBooleanProperty(isDivided)

    operator fun set(subCell: Int = 0, content: String) {
        if ((!isDivided.get() && subCell != 0) || subCell < 0 || subCell > 1)
            throw IndexOutOfBoundsException("There is no subCell of index $subCell")
        contents[subCell].set(content)
    }

    operator fun get(subCell: Int = 0) : String {
        if ((!isDivided.get() && subCell != 0) || subCell < 0 || subCell > 1)
            throw IndexOutOfBoundsException("There is no subCell of index $subCell")
        return contents[subCell].get()
    }

    fun getContentProperty(subCell : Int) : SimpleStringProperty {
        return contents[subCell]
    }
}