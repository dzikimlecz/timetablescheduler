package me.dzikimlecz.timetables.managers

import khttp.delete
import khttp.get
import khttp.patch
import khttp.post
import khttp.responses.Response
import kotlinx.serialization.json.Json
import me.dzikimlecz.timetables.timetable.TimeTable

const val address = "http://localhost:8080/timetableapi/"

class DataBaseConnectionManager {
    fun tryToConnect() =
        get("$address/lecturers").checkSuccess()

    fun getTimeTables(): List<TimeTable> {
        val response = get("$address/timetables")
        response.checkSuccess()
        return response.jsonArray.map { Json.decodeFromString(timetableSerializer, it.toString()) }
    }

    fun lookForTable(name: String): TimeTable? {
        val response = get("$address/timetables/$name")
        return if (response.isOk())
            Json.decodeFromString(timetableSerializer, response.jsonObject.toString())
        else null
    }

    fun sendTable(table: TimeTable) {

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

    fun removeTable(name: String) =
        delete("$address/timetables/$name").checkSuccess()

    fun removeTable(table: TimeTable) = removeTable(table.name)

    companion object {
        private val timetableSerializer = TimeTable.serializer()
    }

}

private fun Response.checkSuccess() =
    check(isOk()) { "Connection problem: status = $statusCode" }

private fun Response.isOk() = statusCode in 200..299