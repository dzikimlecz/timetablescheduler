package me.dzikimlecz.timetables.components.scenes

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.layout.GridPane
import javafx.scene.text.Font
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class TimeTableSetUpScene private constructor(private val pane: GridPane) :
    Scene(pane, 400.0, 180.0) {
    init {
        pane.hgap = 10.0
        pane.vgap = 10.0
        pane.alignment = Pos.CENTER
    }

    constructor(dimensionsContainer: Pair<AtomicInteger, AtomicInteger>) : this(GridPane()) {
        val font = Font.font(14.0)
        val infoLabel = Label("Początkowe wymiary siatki godzin:")
        infoLabel.font = font
        val columnsLabel = Label("L. kolumn:")
        columnsLabel.font = font
        val columnsField = TextField()
        columnsField.promptText = "1"
        columnsField.font = font
        columnsField.filterContent()
        val rowsLabel = Label("L. rzędów:")
        rowsLabel.font = font
        val rowsField = TextField()
        rowsField.promptText = "1"
        rowsField.font = font
        rowsField.filterContent()
        val proceedButton = Button("Ok")
        proceedButton.font = font

        proceedButton.onAction = EventHandler {
            var text = rowsField.text
            dimensionsContainer.first.set(Integer.parseInt(text.ifBlank { "1" }))
            text = columnsField.text
            dimensionsContainer.second.set(Integer.parseInt(text.ifBlank { "1" }))
            window.hide()
        }
        GlobalScope.launch {
            delay(500)
            proceedButton.requestFocus()
        }

        pane.add(infoLabel, 0, 0, 5, 1)
        pane.addRow(1, columnsLabel, columnsField)
        pane.addRow(2, rowsLabel, rowsField)
        pane.add(proceedButton, 5, 2, 2, 1)
    }
}
private fun TextField.filterContent() {
    textFormatter = TextFormatter<String> {
        if (it.text.matches(Regex("\\D")) || this.text.length >= 2)
            it.text = ""
        it
    }
}