package me.dzikimlecz.timetables.managers

import khttp.delete
import khttp.get
import khttp.patch
import khttp.post
import khttp.responses.Response
import kotlinx.serialization.json.Json
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.timetables.managers.DataBaseConnectionManager.Companion.lecturerSerializer
import me.dzikimlecz.timetables.managers.DataBaseConnectionManager.Companion.timetableSerializer
import me.dzikimlecz.timetables.timetable.TimeTable

class KhttpDataBaseConnectionManager: DataBaseConnectionManager {
    override fun tryToConnect() =
        get("$address/lecturers").checkSuccess()

    override fun getTimeTables(): List<TimeTable> {
        val response = get("$address/timetables")
        response.checkSuccess()
        return response.jsonArray.map { Json.decodeFromString(timetableSerializer, it.toString()) }
    }

    override fun lookForTable(name: String): TimeTable? {
        val response = get("$address/timetables/$name")
        return if (response.isOk())
            Json.decodeFromString(timetableSerializer, response.jsonObject.toString())
        else null
    }

    override fun sendTable(table: TimeTable) {

        val postResponse = post(
            "$address/timetables",
            headers = mapOf("Content-Type" to "application/json"),
            data = Json.encodeToString(timetableSerializer, table)
        )
        if (!postResponse.isOk()) {
            val patchResponse = patch(
                "$address/timetables",
                headers = mapOf("Content-Type" to "application/json"),
                data = Json.encodeToString(timetableSerializer, table)
            )
            check (patchResponse.isOk()) {
                """Could not create new, nor update an existing table.
                    post: ${postResponse.text}
                    patch: ${patchResponse.text}""".trimIndent()
            }
        }
    }

    override fun removeTable(name: String) =
        delete("$address/timetables/$name").checkSuccess()

    override fun getLecturers(): List<Lecturer> {
        val response = get("$address/lecturers")
        response.checkSuccess()
        return response.jsonArray.map { Json.decodeFromString(lecturerSerializer, it.toString()) }
    }

    override fun lookForLecturer(name: String): Lecturer? {
        val response = get("$address/lecturers/$name")
        return if (response.isOk())
            Json.decodeFromString(lecturerSerializer, response.jsonObject.toString())
        else null
    }

    override fun sendLecturer(lecturer: Lecturer) {

        val postResponse = post(
            "$address/lecturers",
            headers = mapOf("Content-Type" to "application/json"),
            data = Json.encodeToString(lecturerSerializer, lecturer)
        )
        if (!postResponse.isOk()) {
            val patchResponse = patch(
                "$address/lecturers",
                headers = mapOf("Content-Type" to "application/json"),
                data = Json.encodeToString(lecturerSerializer, lecturer)
            )
            check (patchResponse.isOk()) {
                """Could not create new, nor update an existing table.
                    post: ${postResponse.text}
                    patch: ${patchResponse.text}""".trimIndent()
            }
        }
    }

    override fun removeLecturer(name: String) =
        delete("$address/lecturers/$name").checkSuccess()

}

fun Response.checkSuccess() {
    if (!isOk()) throw ServerAccessException("Connection problem", statusCode, text)
}

fun Response.isOk() = statusCode in 200..299