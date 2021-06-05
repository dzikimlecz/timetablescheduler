package me.dzikimlecz.timetables.components.views.dialogs

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.Alert.AlertType.ERROR
import me.dzikimlecz.timetables.timetable.TimeSpan
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*
import java.time.format.DateTimeFormatter.ofPattern

class TimeSpanAdjustView : View("Dopasuj Czas trwania") {
    val table by param<TimeTable>()
    val column by param<Int>()
    private val areTwoSpansUsed = SimpleBooleanProperty()
    private val texts = List<Array<StringProperty>>(2) {
        Array(2) { SimpleStringProperty("") }
    }

    private var secondTimeSpanFields by singleAssign<Fieldset>()

    override val root = form {
        fieldset("1. Czas trwania") {
            initForTimeSpans(0)
            checkbox("Ustaw 2 czasy trwania", areTwoSpansUsed)
                .selectedProperty().addListener { _, _, newValue ->
                    if (newValue) secondTimeSpanFields.initForTimeSpans(1)
                    else secondTimeSpanFields.children.removeIf { it is Field }
                    secondTimeSpanFields.isVisible = newValue
                    currentStage?.sizeToScene()
                }
        }
        secondTimeSpanFields = fieldset("2. Czas trwania")
        buttonbar {
            button("Ok") {
                isDefaultButton = true
                action {
                    for (i in 0..1) {
                        table.columnsTimeSpan[column][i] =
                            try { TimeSpan.of(texts[i][0].value, texts[i][1].value) } catch(e: Exception) {
                                alert(ERROR, "Błąd", e.message)
                                return@action
                            }
                        if (!areTwoSpansUsed.value) break
                    }
                    close()
                }
            }
            button("Anuluj") {
                isCancelButton = true
                action(this@TimeSpanAdjustView::close)
            }
        }
    }

    private fun Fieldset.initForTimeSpans(row: Int) {
        field("Początek") {
            textfield(texts[row][0]) {
                filterInput { TimeSpan.validateAsBeginning(text) }
            }
        }
        field("Koniec") {
            textfield(texts[row][1]) {
                filterInput { TimeSpan.validateAsBeginning(text) }
            }
        }
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        secondTimeSpanFields.isVisible = false
        secondTimeSpanFields.children.removeIf { it is Field }
        val spans: MutableList<TimeSpan?> = table.columnsTimeSpan[column]
        areTwoSpansUsed.value = spans.none { it === null }
        val spanStrings = spans.map { arrayOf(
            it?.start?.format(ofPattern("HH:mm")) ?: "",
            it?.end?.format(ofPattern("HH:mm")) ?: "",
        ) }
        for ((i, text) in texts.withIndex())
            for (j in 0..1) text[j].set(spanStrings[i][j])
    }
}

