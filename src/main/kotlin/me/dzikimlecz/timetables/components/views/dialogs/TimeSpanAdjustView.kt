package me.dzikimlecz.timetables.components.views.dialogs

import javafx.beans.property.BooleanProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.Alert.AlertType.ERROR
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
        var areTwoSpansUsed: BooleanProperty? = null
        fieldset("2. Czas trwania") {
            isVisible = false
            areTwoSpansUsed = visibleProperty()
            initForTimeSpans(1)
        }
        checkbox("Ustaw 2 czasy trwania") {
            selectedProperty().bindBidirectional(areTwoSpansUsed)
        }
        buttonbar {
            button("Anuluj") {
                isCancelButton = true
                action(this@TimeSpanAdjustView::close)
            }
            button("Ok") {
                isDefaultButton = true
                action {
                    for (i in 0..1) {
                        table.columnsTimeSpan[column][i] =
                            try { TimeSpan.of(texts[i][0]!!.value, texts[i][1]!!.value) } catch(e: Exception) {
                                alert(ERROR, "Błąd", e.message)
                                return@action
                            }
                        if (!areTwoSpansUsed!!.value) break
                    }
                    close()
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

