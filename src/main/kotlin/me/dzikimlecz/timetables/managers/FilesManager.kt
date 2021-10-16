package me.dzikimlecz.timetables.managers

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.serialization.json.Json
import me.dzikimlecz.timetables.DefaultPaths
import me.dzikimlecz.timetables.managers.FilesManager.Companion.ExportResult.*
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.sortByDescending
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.imageio.ImageIO
import kotlin.streams.toList

class FilesManager(
    private val defaultSavePath: String = DefaultPaths.SAVE.value!!
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

    // FIXME: 28/05/2021 saving existing table faces some strange bug of path that could have not been found .
    //  actually it does not appear every time.
    //  sometimes it just doesn't work at all, other times in the same circumstances works flawlessly
    fun saveTable(
        timeTable: TimeTable,
        path: String = defaultSavePath,
        enforce: Boolean = false,
        name: String? = null,
    ) {
        val filename = name?.replace(Regex("[<>\"/\\\\:|?*]"), "-")
        val file =
            if (filename !== null) File(
                path, filename.takeIf { it.endsWith(".json") } ?: "$filename.json"
            )
            else getProperFile(this, timeTable, path)
        val resultStatus =
            if (file.exists()) checkIdentity(file, timeTable)
            else serialize(timeTable, file)
        when (resultStatus) {
            ACCESS_DENIED -> throw IOException("Brak dostępu do pliku")
            SUCCESS -> return
            CONTINUE -> serialize(timeTable, file)
            IDENTITY_PROBLEM -> if (enforce) serialize(timeTable, file) else throw FileAlreadyExistsException(file,
                null, "Ten plik może zawierać już inny plan!")
            SAVING_ERROR -> throw IOException("Błąd zapisu")
        }
    }

    private fun serialize(timeTable: TimeTable, file: File) : ExportResult {
        val serialized = try {
            Json.encodeToString(TimeTable.serializer(), timeTable)
        } catch (e: Exception) {
            return SAVING_ERROR
        }
        file.createNewFile()
        return if (!file.canWrite()) ACCESS_DENIED else try {
            file.writeText(serialized)
            SUCCESS
        } catch (e: Exception) {
            SAVING_ERROR
        }
    }


    fun readTable(file: File) : TimeTable {
        if (!file.exists()) throw FileNotFoundException("Plik \"$file\" nie istnieje.")
        val text = file.readText()
        return try {
            Json.decodeFromString(TimeTable.serializer(), text)
        } catch (e: Exception) {
            throw IOException("Zawartość pliku została naruszona. Odczyt niemożliwy", e)
        }
    }

    fun refreshJsonFiles() {
        files.addAll(
            Files.walk(Paths.get(defaultSavePath)).filter { Files.isRegularFile(it) && Files.isReadable(it) }
                .map { it.toFile() }
        .filter { it.toString().endsWith(".json") }.filter { files.stream().noneMatch {
                    file -> file.name == it.name } }.toList())
        files.sortByDescending { it.lastModified() }
    }

    fun saveImage(name: String, image: BufferedImage?): Boolean {
        val file = File(DefaultPaths.EXPORT.value, "$name.png")
        if (!file.exists()) file.createNewFile()
        return ImageIO.write(image, "png", file)
    }

    companion object {
        private fun checkIdentity(file: File, timeTable: TimeTable) : ExportResult {
            require(file.exists()) {
                "Can't check if content of non-existent file: $file corresponds to the given table: ${timeTable.name}"
            }
            if (!file.canRead()) return if (file.canWrite()) IDENTITY_PROBLEM else ACCESS_DENIED
            val serialized = file.readText()
            val table = Json.decodeFromString(TimeTable.serializer(), serialized)
            return if (table.softEquals(timeTable)) CONTINUE else IDENTITY_PROBLEM
        }

        private fun getProperFile(
            filesManager: FilesManager,
            table: TimeTable,
            path: String = filesManager.defaultSavePath
        ) = File(path,
            table.name + "_" + table.date
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                .replace(Regex("[<>\"/\\\\:.|?*]"), "-") + ".json"
        )

        private enum class ExportResult {
            ACCESS_DENIED,
            SUCCESS,
            CONTINUE,
            IDENTITY_PROBLEM,
            SAVING_ERROR,
        }
    }


}