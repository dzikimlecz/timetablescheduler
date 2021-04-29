package me.dzikimlecz.timetables.managers

import khttp.responses.Response
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.timetables.timetable.TimeTable

const val address = "http://localhost:8080/timetableapi/"

interface DataBaseConnectionManager {
    fun tryToConnect()

    fun getTimeTables(): List<TimeTable>

    fun lookForTable(name: String): TimeTable?

    fun sendTable(table: TimeTable)

    fun removeTable(name: String)

    fun getLecturers(): List<Lecturer>

    fun lookForLecturer(name: String): Lecturer?

    fun sendLecturer(lecturer: Lecturer)

    fun removeLecturer(name: String)
}

fun Response.checkSuccess() =
    check(isOk()) { "Connection problem: status = $statusCode, $text" }

fun Response.isOk() = statusCode in 200..299