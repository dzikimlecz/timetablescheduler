package me.dzikimlecz.timetables.managers

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.serialization.json.Json
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.sortByDescending
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.streams.toList

class FilesManager(
    private val defaultSavePath: String = System.getProperty("defaultSavePath")
) {
    private val files: ObservableList<File> = FXCollections.observableArrayList()

    val jsonFiles: ObservableList<File>
        get() {
            refreshJsonFiles()
            return files
        }

    init {
        refreshJsonFiles()
    }


    fun saveTable(
        timeTable: TimeTable,
        path: String = defaultSavePath,
        enforce: Boolean = false,
        name: String? = null,
    ) {
        val file = if (name !== null) File(path, name) else getProperFile(timeTable, path)
        val resultStatus = if (file.exists()) checkIdentity(file, timeTable)
        else serialize(timeTable, file)
        when (resultStatus) {
            0 -> return
            1 -> serialize(timeTable, file)
            -1 -> throw IOException("Brak dostępu do pliku")
            2 -> if (enforce) serialize(timeTable, file) else throw FileAlreadyExistsException(file,
                null, "Ten plik może zawierać już inny plan!")
            3 -> throw IOException("Błąd zapisu")
            else -> throw IOException()
        }
    }

    private fun serialize(timeTable: TimeTable, file: File) : Int {
        val serialized = try {
            Json.encodeToString(TimeTable.serializer(), timeTable)
        } catch (e: Exception) {
            return 3
        }
        if (!file.exists()) file.createNewFile()
        return if (!file.canWrite()) -1 else try {
            file.writeText(serialized)
            0
        } catch (e: Exception) {
            3
        }
    }

    private fun checkIdentity(file: File, timeTable: TimeTable) : Int {
        require(file.exists())
        if (!file.canRead()) return if (file.canWrite()) 2 else -1
        val serialized = file.readText()
        val table = Json.decodeFromString(TimeTable.serializer(), serialized)
        return if (table.name.equals(timeTable.name, true) ||
            table.date == timeTable.date
        ) 1 else 2
    }

    fun getProperFile(table: TimeTable, path: String = defaultSavePath) =
        File(path,
            table.name +
                table.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                .replace(Regex("[:.]"), "-")
         + ".json")


    fun readTable(file: File) : TimeTable {
        if (!file.exists()) throw FileNotFoundException("Plik \"$file\" nie istnieje.")
        val text = file.readText()
        return try {
            Json.decodeFromString(TimeTable.serializer(), text)
        } catch (e: Exception) {
            throw IOException("Zawartość pliku została naruszona. Odczyt niemożliwy", e)
        }
    }

    fun readTable(name: String, path: String = defaultSavePath) : TimeTable {
        val file = File(path, "$name.json")
        return readTable(file)
    }

    fun refreshJsonFiles(path : String = defaultSavePath) {
        files.addAll(
            Files.walk(Paths.get(path)).filter { Files.isRegularFile(it) && Files.isReadable(it) }
                .map { it.toFile() }
        .filter { it.toString().endsWith(".json") }.filter { files.stream().noneMatch {
                    file -> file.name == it.name } }.toList())
        files.sortByDescending { it.lastModified() }
    }



}