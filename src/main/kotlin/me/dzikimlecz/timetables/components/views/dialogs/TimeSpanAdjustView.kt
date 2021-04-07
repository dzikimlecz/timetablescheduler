package me.dzikimlecz.timetables.components.views.dialogs

import javafx.beans.property.BooleanProperty
import javafx.beans.property.StringProperty
import me.dzikimlecz.timetables.timetable.TimeSpan
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*

class TimeSpanAdjustView : View("Dopasuj Czas trwania") {
    val table by param<TimeTable>()
    val column by param<Int>()
    private val texts = List<Array<StringProperty?>>(2) { Array(2) { null } }

    override val root = form {
        fieldset("1. Czas trwania") {
            initForTimeSpans(0)
        }
        var isVisibleProperty: BooleanProperty? = null
        fieldset("2. Czas trwania") {
            isVisible = false
            isVisibleProperty = visibleProperty()
            initForTimeSpans(1)
        }
        checkbox("Ustaw 2 czasy trwania") {
            selectedProperty().bindBidirectional(isVisibleProperty)
        }
        buttonbar {
            button("Anuluj") {
                isCancelButton = true
                action(this@TimeSpanAdjustView::close)
            }
            button("Ok") {
                isDefaultButton = true
                action {
                    table.columnsTimeSpan[column]
                }
            }
        }

    }

    private fun Fieldset.initForTimeSpans(row: Int) {
        field("Początek") {
            textfield {
                texts[row][0] = textProperty()
                textProperty().addListener { observable, oldValue, newValue ->
                    if (!TimeSpan.validateAsBeginning(newValue))
                        (observable as StringProperty).value = oldValue
                }
            }
        }
        field("Koniec") {
            textfield {
                texts[row][1] = textProperty()
                textProperty().addListener { observable, oldValue, newValue ->
                    if (!TimeSpan.validateAsBeginning(newValue))
                        (observable as StringProperty).value = oldValue
                }
            }
        }
    }
}

