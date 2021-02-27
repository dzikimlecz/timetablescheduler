package me.dzikimlecz.timetables.managers

import kotlinx.serialization.json.Json
import me.dzikimlecz.timetables.timetable.TimeTable
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.streams.toList

class FilesManager(
    val defaultSavePath: String = System.getProperty("defaultSavePath")
) {
    init {
        val file = File(defaultSavePath)
        if (!file.exists()) file.mkdirs()
    }


    fun saveTable(timeTable: TimeTable, path: String = defaultSavePath, enforce: Boolean = false,
    name: String? = null) {
        val filename = name ?: timeTable.name.ifBlank {
            timeTable.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
            .replace(Regex("[:.]"), "-")
        } + ".json"
        val file = File(path, filename)
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
            table.name.ifBlank {
                table.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                .replace(Regex("[:.]"), "-")
        } + ".json")


    fun readTable(name: String, path: String = defaultSavePath) : TimeTable {
        val file = File(path, "$name.json")
        if (!file.exists()) throw FileNotFoundException("Plik \"$file\" nie istnieje.")
        if (!file.canRead()) throw IOException("Odczyt pliku zablokowany przez system")
        return try {
            Json.decodeFromString(TimeTable.serializer(), file.readText())
        } catch (e: Exception) {
            throw IOException("Zawartość pliku została naruszona. Odczyt niemożliwy", e)
        }
    }

    fun jsonFiles(path : String = defaultSavePath)  = Files.walk(Paths.get(path))
        .filter { Files.isRegularFile(it) && Files.isReadable(it) }.map { it.toFile() }
        .filter { it.toString().endsWith(".json") }.toList()

    fun savedTables(path : String = defaultSavePath) = jsonFiles(path)
        .mapNotNull { try { readTable(it.name, path) } catch (e: Exception) { null } }

}