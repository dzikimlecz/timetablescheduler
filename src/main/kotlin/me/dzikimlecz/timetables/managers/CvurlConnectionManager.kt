package me.dzikimlecz.timetables.managers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import coresearch.cvurl.io.mapper.BodyType
import coresearch.cvurl.io.mapper.MapperFactory
import coresearch.cvurl.io.model.CVurlConfig
import coresearch.cvurl.io.model.Response
import coresearch.cvurl.io.request.CVurl
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.timetables.managers.DataBaseConnectionManager.Companion.address
import me.dzikimlecz.timetables.timetable.TimeTable
import java.util.*

class CvurlConnectionManager : DataBaseConnectionManager {
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule())
    private val cvurl = CVurl(CVurlConfig.builder()
        .genericMapper(MapperFactory.from(objectMapper))
        .build())

    override fun tryToConnect() {
        cvurl.get("$address/lecturers")
            .asString()
            .checkSuccess()
    }

    override fun getTimeTables(): List<TimeTable> =
        cvurl.get("$address/timetables")
            .asObject(object: BodyType<List<TimeTable>>() {}, 200)
            .orElseThrow { ConnectionException("Could not fetch the tables") }

    override fun lookForTable(name: String): TimeTable? {
        val timeTable = cvurl.get("$address/timetables/$name")
            .asObject(TimeTable::class.java, 200)
        return if (timeTable.isPresent) timeTable.get() else null
    }

    override fun sendTable(table: TimeTable) {
        sendTable(objectMapper.writeValueAsString(table))
    }

    override fun sendTable(data: String) {
        val postResponse = cvurl.post("$address/timetables")
           .header("Content-Type", "application/json")
           .body(data)
           .asString().checkSuccess()
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
        cvurl.delete("$address/timetables/$name").asString().checkSuccess()
    }

    override fun getLecturers(): List<Lecturer> =
        cvurl.get("$address/lecturers")
            .asObject(object: BodyType<List<Lecturer>>() {}, 200)
            .orElseThrow { ConnectionException("Could not fetch the tables") }

    override fun lookForLecturer(name: String): Lecturer? {
        val lecturer = cvurl.get("$address/lecturers/$name")
            .asObject(Lecturer::class.java, 200)
        return if (lecturer.isPresent) lecturer.get() else null
    }

    override fun sendLecturer(lecturer: Lecturer) {
        val postResponse = cvurl.post("$address/lecturers")
            .header("Content-Type", "application/json")
            .body(lecturer)
            .asString().checkSuccess()
        if (!postResponse.isSuccessful) {
            val patchResponse = cvurl.patch(
                "$address/lecturers")
                .header("Content-Type", "application/json")
                .body(lecturer).asString().orElseThrow { ConnectionException("Could not send table: ${postResponse.body}" ) }
            if (!patchResponse.isSuccessful) throw ConnectionException("Could not send table: ${postResponse.body}")
        }
    }

    override fun removeLecturer(name: String) {
        cvurl.delete("$address/lecturers/$name").asString().checkSuccess()
    }

    private inline fun <T> Optional<Response<T>>.checkSuccess(msg: () -> String = { "Connection problem" }): Response<T> {
        if (isEmpty) throw ConnectionException("Could not connect")
        if (get().isSuccessful) throw ResponseException(msg(), get().status(), get().body.toString())
        return get()
    }

}
