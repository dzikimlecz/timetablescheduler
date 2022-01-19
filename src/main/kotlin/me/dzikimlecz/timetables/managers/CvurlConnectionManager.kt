package me.dzikimlecz.timetables.managers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import coresearch.cvurl.io.mapper.BodyType
import coresearch.cvurl.io.mapper.MapperFactory
import coresearch.cvurl.io.model.CVurlConfig
import coresearch.cvurl.io.model.Response
import coresearch.cvurl.io.request.CVurl
import javafx.geometry.Orientation
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.lecturers.LecturerTransferredSurrogate
import me.dzikimlecz.timetables.managers.DataBaseConnectionManager.Companion.address
import me.dzikimlecz.timetables.timetable.Cell
import me.dzikimlecz.timetables.timetable.TimeSpan
import me.dzikimlecz.timetables.timetable.TimeTable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class CvurlConnectionManager : DataBaseConnectionManager {
    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModules(
        JavaTimeModule()
    )
    private val cvurl = CVurl(CVurlConfig.builder()
        .genericMapper(MapperFactory.from(objectMapper))
        .build())

    override fun tryToConnect() {
        cvurl.get("$address/lecturers")
            .asString()
            .checkSuccess()
    }

    override fun getTimeTables() = cvurl.get("$address/timetables")
        .asObject(object: BodyType<List<JacksonTimeTable>>() {}, 200)
        .orElseThrow { ConnectionException("Could not fetch the tables") }
        .map(JacksonTimeTable::asTimeTable)

    override fun lookForTable(name: String): TimeTable? {
        return try {
            cvurl.get("$address/timetables/${name.replace(" ", "%20")}")
                .asObject(JacksonTimeTable::class.java, 200)
                .orElse(null)?.asTimeTable
        } catch (e: Exception) {
            null
        }
    }

    override fun sendTable(table: TimeTable) {
        sendTable(objectMapper.writeValueAsString(JacksonTimeTable(table)))
    }

    override fun sendTable(data: String) {
        val postResponse = cvurl.post("$address/timetables")
            .header("Content-Type", "application/json")
            .body(data)
            .asString()
            .get()
        if (postResponse.status() == 424) throw ResponseException("Missing Lecturers", 424, postResponse.body)
        else if (!postResponse.isSuccessful) {
            val patchResponse = cvurl.patch(
                "$address/timetables")
                .header("Content-Type", "application/json")
                .body(data).asString().orElseThrow { ConnectionException("Could not send table: ${postResponse.body}" ) }
            if (patchResponse.status() == 424) throw ResponseException("Missing Lecturers", 424, postResponse.body)
            if (!patchResponse.isSuccessful) throw ConnectionException("Could not send table: ${postResponse.body}")
        }
    }

    override fun removeTable(name: String) {
        cvurl.delete("$address/timetables/${name.replace(" ", "%20")}").asString().checkSuccess()
    }

    override fun getLecturers(): List<Lecturer> {
        return cvurl.get("$address/lecturers")
            .asObject(object: BodyType<List<LecturerTransferredSurrogate>>() {}, 200)
            .orElseThrow { ConnectionException("Could not fetch the lecturers") }
            .map(LecturerTransferredSurrogate::toLecturer)
    }

    override fun lookForLecturer(code: String): Lecturer? {
        val lecturer = cvurl.get("$address/lecturers/${code.replace(" ", "%20")}")
            .asObject(LecturerTransferredSurrogate::class.java, 200)
        return if (lecturer.isPresent) lecturer.get().toLecturer() else null
    }

    override fun sendLecturer(lecturer: Lecturer) {
        val postResponse = cvurl.post("$address/lecturers")
            .header("Content-Type", "application/json")
            .body(lecturer.toSurrogate())
            .asString().get()
        if (!postResponse.isSuccessful) {
            val patchResponse = cvurl.patch(
                "$address/lecturers")
                .header("Content-Type", "application/json")
                .body(lecturer).asString().orElseThrow { ConnectionException("Could not send table: ${postResponse.body}" ) }
            if (!patchResponse.isSuccessful) throw ConnectionException("Could not send table: ${postResponse.body}")
        }
    }

    override fun removeLecturer(code: String) {
        cvurl.delete("$address/lecturers/${code.replace(" ", "%20")}").asString().checkSuccess()
    }

    private inline fun <T> Optional<Response<T>>.checkSuccess(msg: () -> String = { "Connection problem" }): Response<T> {
        if (isEmpty) throw ConnectionException("Could not connect")
        if (!get().isSuccessful) throw ResponseException(msg(), get().status(), get().body.toString())
        return get()
    }

}

data class JacksonTimeTable(
    val date: LocalDate,
    val name: String,
    val table: List<List<JacksonCell>>,
    val timeSpans: List<Array<TimeSpan?>>?,
    val titles: List<String>?,
) {
    constructor(timeTable: TimeTable): this(
        timeTable.date,
        with(timeTable) {
            name.ifBlank { date.format(DateTimeFormatter.ISO_DATE) }
        },
        timeTable.list.map { it.map(::JacksonCell) },
        timeTable.columnsTimeSpan.map { it.toTypedArray()},
        timeTable.titles.map { it.value }
    )

    val asTimeTable: TimeTable
        get() {
            val newTable = TimeTable.of(table.map { it.map(JacksonCell::asCell) })
            newTable.date = date
            newTable.name = name
            if (timeSpans != null) {
                for ((i, timeSpan) in timeSpans.withIndex()) {
                    val spans = newTable.columnsTimeSpan[i]
                    spans[0] = timeSpan[0]
                    if (spans.size == 2)
                        spans[1] = timeSpan[1]
                }
            }
            if (titles != null)
                for ((i, title) in titles.withIndex()) newTable.titles[i].set(title)
            return newTable
        }
}

data class JacksonCell(
    val content: List<String>,
    val divisionOrientation: Orientation?,
) {
    constructor(cell: Cell): this(
        with(cell) {
            if (isDivided) listOf(get(0), get(1)) else listOf(get(0))
        },
        cell.divisionDirection
    )

    val asCell by lazy {
        val newCell = Cell()
        newCell.divisionDirection = divisionOrientation
        newCell[0] = content[0]
        if (newCell.isDivided)
            newCell[1] = content[1]
        newCell
    }
}
