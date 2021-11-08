package me.dzikimlecz.timetables.managers

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.serialization.SerializationException
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.stream.Stream
import javax.imageio.ImageIO
import kotlin.streams.toList

class FilesManager(private val defaultSavePath: String = DefaultPaths.SAVE.value!!) {
    private val _jsonFiles: ObservableList<File> = FXCollections.observableArrayList()
    val jsonFiles: ObservableList<File>
        get() {
            refreshJsonFiles()
            return _jsonFiles
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
        val file = makeFile(name, timeTable, path)
        val identityStatus = if (enforce) CONTINUE else checkIfFileContainsThisTable(file, timeTable)
        if (identityStatus.isFailure)
            fail(identityStatus, file)
        val resultStatus = serialize(timeTable, file)
        if (resultStatus.isFailure)
            fail(resultStatus, file)
    }

    private fun serialize(timeTable: TimeTable, file: File) : ExportResult {
        if (file.creatingFailed())
            return SAVING_ERROR
        val serializedString = try {
            timeTable.serialize()
        } catch (e: SerializationException) {
            return SAVING_ERROR
        }
        return if (!file.canWrite()) ACCESS_DENIED
        else file.tryToWrite(serializedString)
    }

    fun readTable(file: File) : TimeTable {
        if (!file.exists()) throw FileNotFoundException("Plik \"$file\" nie istnieje.")
        return file.readText().decodeTable()
            ?: throw IOException("Plik nie zawiera planu. Odczyt niemożliwy")

    }

    fun refreshJsonFiles() {
        _jsonFiles.clear()
        _jsonFiles += jsonFilesInDirectory(defaultSavePath)
        _jsonFiles.sortByDescending { it.lastModified() }
    }

    fun saveImage(name: String, image: BufferedImage) {
        val file = File(DefaultPaths.EXPORT.value, "${name.fileNameCompatible}.png")
        ImageIO.write(image, "png", file)
    }

    private companion object {
        /**
         *  ## Checks if the contents of the given file are probably the sme table in other version.
         * returns:
         *
         *  file may contain other table -> IDENTITY_PROBLEM
         *
         *  file is neither readable nor writable -> ACCESS_DENIED
         *
         *  file doesn't contain timetable or table of the same name and date -> CONTINUE
         */
        fun checkIfFileContainsThisTable(file: File, timeTable: TimeTable) : ExportResult {
            if (!file.exists()) return CONTINUE
            if (!file.canRead()) return if (file.canWrite()) IDENTITY_PROBLEM else ACCESS_DENIED
            val table = file.readText().decodeTable() ?: return CONTINUE
            return if (timeTable.softEquals(table)) CONTINUE else IDENTITY_PROBLEM
        }

        private fun generateFileName(table: TimeTable): String =
            "${table.name.fileNameCompatible}_${table.date.fileNameCompatibleString}"

        val LocalDate.fileNameCompatibleString: String
            get() = format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                .fileNameCompatible
                .plus(".json")

        val String.fileNameCompatible: String
            get() = replace(Regex("[<>\"/\\\\:.|?*]"), "-")

        val String.asTimeTableFileName: String
            get() = removeSuffix(".json").fileNameCompatible + ".json"

        fun String.decodeTable(): TimeTable? =
            try {
                Json.decodeFromString(TimeTable.serializer(), this)
            } catch (e: SerializationException) {
                null
            }

        fun TimeTable.serialize(): String = Json.encodeToString(TimeTable.serializer(), this)

        fun makeFile(
            name: String?,
            timeTable: TimeTable,
            path: String
        ): File {
            val filename = name?.asTimeTableFileName ?: generateFileName(timeTable)
            return File(path, filename)
        }

        enum class ExportResult(val isFailure: Boolean) {
            ACCESS_DENIED(true),
            SUCCESS(false),
            CONTINUE(false),
            IDENTITY_PROBLEM(true),
            SAVING_ERROR(true),
        }

        fun fail(status: ExportResult, file: File): Nothing =
            when (status) {
                ACCESS_DENIED -> throw IOException("Brak dostępu do pliku")
                IDENTITY_PROBLEM -> throw FileAlreadyExistsException(file, null, "Ten plik może zawierać już inny plan!")
                SAVING_ERROR -> throw IOException("Błąd zapisu")
                else -> throw IllegalArgumentException("Status: $status is not a failure")
            }

        fun File.creatingFailed(): Boolean =
            try {
                createNewFile()
                false
            } catch (e: IOException) {
                true
            }

        fun File.tryToWrite(serialized: String) =
            try {
                writeText(serialized)
                SUCCESS
            } catch (e: Exception) {
                SAVING_ERROR
            }

        fun getFilesInDirectory(path: String): Stream<File> =
            Files.walk(Paths.get(path))
                .filter { Files.isRegularFile(it) }
                .filter { Files.isReadable(it) }
                .map { it.toFile() }

        fun jsonFilesInDirectory(path: String) =
            getFilesInDirectory(path)
                .filter { it.extension == "json" }.toList()
                .distinctBy { it.name }

    }
}
