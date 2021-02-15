package me.dzikimlecz.managers

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.text.Font
import javafx.stage.Stage
import java.util.*

class WindowsManager(private val mainStage: Stage) {
    private val dialogStage = Stage()

    fun showTimeTableSetUp(properties: Properties) {
        dialogStage.title = "Nowy Plan"
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

        proceedButton.onAction = EventHandler {
            var text = columnsField.text
            properties.setProperty("ColumnsAmount", if (text.isNotEmpty()) text else "1")
            text = rowsField.text
            properties.setProperty("RowsAmount", if (text.isNotEmpty()) text else "1")
            dialogStage.close()
        }

        pane.add(infoLabel, 0, 0, 5, 1)
        pane.addRow(1, columnsLabel, columnsField)
        pane.addRow(2, rowsLabel, rowsField)
        pane.add(proceedButton, 5, 2, 2, 1)

        dialogStage.scene = Scene(pane, 400.0, 180.0)
        dialogStage.sizeToScene()
        dialogStage.centerOnScreen()
        dialogStage.isResizable = false
        dialogStage.showAndWait()
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