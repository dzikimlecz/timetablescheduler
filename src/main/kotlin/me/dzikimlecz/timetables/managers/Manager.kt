package me.dzikimlecz.timetables.managers

import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.image.Image
import javafx.stage.StageStyle.UTILITY
import me.dzikimlecz.timetables.DefaultPaths
import me.dzikimlecz.timetables.components.views.MainView
import me.dzikimlecz.timetables.components.views.dialogs.ExportView
import me.dzikimlecz.timetables.components.views.dialogs.ImportView
import me.dzikimlecz.timetables.components.views.dialogs.TimeTableSetUpView
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.alert
import tornadofx.find
import java.io.File
import java.time.LocalDate
import javax.imageio.ImageIO
import kotlin.reflect.KProperty1

class Manager {
    private lateinit var lastTable : TimeTable
    private val filesManager by lazy { FilesManager() }
    private val dataBaseConnectionManager by lazy { DataBaseConnectionManager() }

    val activeTable : TimeTable
        get() = lastTable

    private fun newTable(properties: Map<KProperty1<TimeTable, Any>, String>): TimeTable {
        val columns = (properties[TimeTable::columns] ?: badProperty("columns", true))
            .toIntOrNull() ?: badProperty("columns", false)
        val rows = (properties[TimeTable::rows] ?: badProperty("rows", true))
            .toIntOrNull() ?: badProperty("rows", false)
        val name = properties[TimeTable::name] ?: badProperty("name", true)
        val date = try {
            LocalDate.parse(properties[TimeTable::date] ?: badProperty("date", true))
        } catch (e: Exception) {
            badProperty("date", false)
        }
        lastTable = TimeTable(columns, rows, date, name)
        return lastTable
    }

    fun saveTable() = try { filesManager.saveTable(lastTable) }
        catch (e: FileAlreadyExistsException) { describedExport() }


    fun describedExport() {
        val properties = mutableMapOf<String, String>()
        find<ExportView>(params = mapOf(ExportView::exportProperties to properties)).openModal(
            UTILITY, block = true, resizable = false
        )
        val customName = properties["name"] ?: return
        val customPath = properties["path"] ?: return
        if (customName != "\u0000" && customPath != "\u0000")
            filesManager.saveTable(lastTable, customPath, true, customName)
        else if (customPath != "\u0000")
            filesManager.saveTable(lastTable, customPath, true)
        else if (customName != "\u0000")
            filesManager.saveTable(lastTable, enforce = true, name = customName)
        else filesManager.saveTable(lastTable, enforce = true)
    }

    fun importTable() {
        val importView = find<ImportView>(params = mapOf(ImportView::filesManager to filesManager))
        importView.openModal(block = true, resizable = false)
        if (importView.chosenFile == null)  {
            alert(ERROR, "Nie wybrano pliku")
            return
        }
        val table = try {
             filesManager.readTable(importView.chosenFile!!)
        } catch(e: Exception) {
            alert(ERROR,"Błąd odczytu", e.message)
            return
        }
        lastTable = table
        find<MainView>().displayTable(table)
    }

    fun setUpTable() {
        val map = mutableMapOf<KProperty1<TimeTable, Any>, String>()
        find<TimeTableSetUpView>(params = mapOf(TimeTableSetUpView::tableProperties to map))
            .openModal(UTILITY, resizable = false, block = true)
        try { find<MainView>().displayTable(newTable(map)) } catch (e: Exception) {
            alert(ERROR,"Błąd", e.message)
            e.printStackTrace()
        }
    }

    fun openDB() {
        fun alert(e: Throwable) =
            Platform.runLater { alert(ERROR, "Błąd Połączenia!", e.message) }

        try { dataBaseConnectionManager.tryToConnect() } catch (e: Exception) {
            return alert(e)
        }
        val lecturers = try {
            dataBaseConnectionManager.getLecturers()
        } catch (e: IllegalStateException) {
            return alert(e)
        }
        val tables = try {
            dataBaseConnectionManager.getTimeTables()
        } catch (e: IllegalStateException) {
            return alert(e)
        }
        find<MainView>().showDataBaseControlPane(lecturers, tables)
    }

    private fun badProperty(name: String, missing: Boolean): Nothing {
        val cause = if (missing) "Missing" else "Wrongly formatted"
        throw IllegalArgumentException("$cause property: $name")
    }

    fun exportTableImage(img: Image, name: String) = Thread {
        val image = SwingFXUtils.fromFXImage(img, null)
        val file = File(DefaultPaths.EXPORT.value, "$name.png")
        if (!file.exists()) file.createNewFile()
        ImageIO.write(image, "png", file)
    }.start()

}
