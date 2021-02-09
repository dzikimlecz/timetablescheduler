package me.dzikimlecz

import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.layout.GridPane
import javafx.scene.text.Font
import javafx.stage.Stage
import java.util.*

class ScenesManager(private val stage: Stage) {


    fun showTimeTableSetUp(properties: Properties) {
        stage.title = "Nowy Plan"
        val pane = GridPane()
        pane.hgap = 10.0
        pane.vgap = 10.0
        pane.alignment = Pos.CENTER

        val font = Font.font(14.0)
        val infoLabel =
            Label("Początkowe wymiary siatki godzin:")
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

        pane.add(infoLabel, 0, 0, 5, 1)
        pane.addRow(1, columnsLabel, columnsField)
        pane.addRow(2, rowsLabel, rowsField)
        pane.add(proceedButton, 5, 2, 2, 1)

        stage.scene = Scene(pane)
        stage.height = 180.0
        stage.width = 400.0
        stage.centerOnScreen()
        stage.isResizable = false
        stage.show()
        proceedButton.requestFocus()
    }

    private fun TextField.filterContent() {
        textFormatter = TextFormatter<String> {
            if (it.text.matches(Regex("\\D")) || this.text.length >= 2)
                it.text = ""
            it
        }
    }
}