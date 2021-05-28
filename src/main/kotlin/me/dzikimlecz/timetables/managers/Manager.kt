package me.dzikimlecz.timetables.managers

import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.image.Image
import javafx.stage.StageStyle.UTILITY
import me.dzikimlecz.timetables.DefaultPaths
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
import javax.imageio.ImageIO

class Manager {
    lateinit var activeTable : TimeTable
    private val filesManager by lazy { FilesManager() }
    private val dataBaseConnectionManager: DataBaseConnectionManager = KhttpDataBaseConnectionManager()

    fun saveTable() = try { filesManager.saveTable(activeTable) }
        catch (e: FileAlreadyExistsException) { describedExport() }
        catch (e: Exception) {
            alert(ERROR,"Błąd Zapisu", e.message)
        }

    fun describedExport() {
        val exportView = find<ExportView>()
        exportView.openModal(UTILITY, block = true, resizable = false)
        val (path, name) = exportView.fileData ?: return
        if (name !== null && path !== null)
            filesManager.saveTable(activeTable, path, true, name)
        else if (path !== null)
            filesManager.saveTable(activeTable, path, true)
        else if (name !== null)
            filesManager.saveTable(activeTable, enforce = true, name = name)
        else filesManager.saveTable(activeTable, enforce = true)
    }

    fun openTable() {
        val table = importTable() ?: return
        activeTable = table
        displayTable(table)
    }

    fun importTable(): TimeTable? {
        val importView = find<ImportView>(params = mapOf(ImportView::filesManager to filesManager))
        importView.openModal(block = true, resizable = false)
        val chosenFile = importView.chosenFile
        return if (chosenFile !== null)
            try {
                filesManager.readTable(chosenFile)
            } catch(e: Exception) {
                alert(ERROR,"Błąd odczytu", e.message)
                null
            }
        else null
    }

    fun setUpTable() {
        val tableSetUpView = find<TimeTableSetUpView>()
        tableSetUpView.openModal(UTILITY, resizable = false, block = true)
        val table = tableSetUpView.table
        if (table !== null) try {
            displayTable(table)
        } catch (e: Exception) {
            alert(ERROR, "Błąd", e.message)
        }
    }

    fun openDB() = runAsync {
        fun alert(e: Throwable) =
            Platform.runLater { alert(ERROR, "Błąd Połączenia!", e.message) }
        try { dataBaseConnectionManager.tryToConnect() } catch (e: Exception) { return@runAsync alert(e) }
        val panelProvider = {
            find<DataBaseControlPanelView>(
                params = mapOf(DataBaseControlPanelView::db to dataBaseConnectionManager)
            ).apply { refresh() }
        }
        Platform.runLater { find<MainView>().showDataBaseControlPane(panelProvider) }
    }

    fun exportTableImage(img: Image, name: String) = Thread {
        val image = SwingFXUtils.fromFXImage(img, null)
        val file = File(DefaultPaths.EXPORT.value, "$name.png")
        if (!file.exists()) file.createNewFile()
        ImageIO.write(image, "png", file)
    }.start()

    fun displayTable(table: TimeTable) {
        find<MainView>().displayTable(table)
        activeTable = table
    }

}
