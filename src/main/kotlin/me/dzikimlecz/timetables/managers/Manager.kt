package me.dzikimlecz.timetables.managers

import javafx.application.Platform.runLater
import javafx.embed.swing.SwingFXUtils
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.image.Image
import javafx.stage.StageStyle.UTILITY
import me.dzikimlecz.timetables.components.views.DataBaseControlPanelView
import me.dzikimlecz.timetables.components.views.MainView
import me.dzikimlecz.timetables.components.views.dialogs.ExportView
import me.dzikimlecz.timetables.components.views.dialogs.ImportView
import me.dzikimlecz.timetables.components.views.dialogs.TimeTableSetUpView
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.alert
import tornadofx.find
import tornadofx.runAsync
import java.io.File

class Manager {
    lateinit var activeTable : TimeTable
    private val filesManager = FilesManager()
    private val dataBaseConnectionManager: DataBaseConnectionManager = KhttpDataBaseConnectionManager()

    fun saveTable() =
        try { filesManager.saveTable(activeTable) }
        catch (e: FileAlreadyExistsException) { describedSaving() }
        catch (e: Exception) { alert(ERROR,"Błąd Zapisu", e.message) }

    fun describedSaving() {
        val exportView = find<ExportView>()
        exportView.openModal(stageStyle = UTILITY, block = true, resizable = false)
        saveWithGivenData(exportView.fileData)
    }

    fun openTable() {
        val table = importTable() ?: return
        activeTable = table
        displayTable(table)
    }

    fun importTable(): TimeTable? {
        val importView = find<ImportView>(params = mapOf(ImportView::filesManager to filesManager))
        importView.openModal(stageStyle = UTILITY, block = true, resizable = false)
        return readTable(importView.chosenFile)
    }

    fun setUpTable() {
        val tableSetUpView = find<TimeTableSetUpView>()
        tableSetUpView.openModal(UTILITY, resizable = false, block = true)
        val table = tableSetUpView.buildTable ?: return
        displayTable(table)
    }

    fun openDatabasePanel() = runAsync {
        val failedToConnect = !tryToConnect()
        if (failedToConnect) return@runAsync
        runLater(::displayDatabasePanel)
    }

    fun exportTableImage(img: Image, name: String) = runAsync {
        val image = SwingFXUtils.fromFXImage(img, null)
        filesManager.saveImage(name, image)
    }

    fun displayTable(table: TimeTable) {
        find<MainView>().displayTable(table)
        activeTable = table
    }

    private fun saveWithGivenData(saveData: Pair<String?, String?>?) {
        val (path, name) = saveData ?: return
        if (name !== null && path !== null)
            filesManager.saveTable(activeTable, path, true, name)
        else if (path !== null)
            filesManager.saveTable(activeTable, path, true)
        else if (name !== null)
            filesManager.saveTable(activeTable, enforce = true, name = name)
        else filesManager.saveTable(activeTable, enforce = true)
    }

    private fun readTable(chosenFile: File?): TimeTable? =
        if (chosenFile === null) null
        else try {
            filesManager.readTable(chosenFile)
        } catch (e: Exception) {
            alert(ERROR, "Błąd odczytu", e.message)
            null
        }

    private fun tryToConnect(): Boolean =
        try {
            dataBaseConnectionManager.tryToConnect()
            true
        } catch (e: Exception) {
            runLater { alert(ERROR, "Błąd Połączenia!", e.message) }
            false
        }

    private fun displayDatabasePanel() {
        find<MainView>().showDataBaseControlPane {
            find<DataBaseControlPanelView>(
                params = mapOf(DataBaseControlPanelView::db to dataBaseConnectionManager)
            ).apply { refresh() }
        }
    }

}
