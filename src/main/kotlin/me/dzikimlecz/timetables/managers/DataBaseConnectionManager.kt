package me.dzikimlecz.timetables.managers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.lecturers.LecturerTransferredSurrogate
import me.dzikimlecz.timetables.timetable.TimeTable

const val address = "http://localhost:8080/timetableapi/"

interface DataBaseConnectionManager {
    fun tryToConnect()

    fun getTimeTables(): List<TimeTable>

    fun lookForTable(name: String): TimeTable?

    fun sendTable(table: TimeTable)

    fun sendTable(data: String)

    fun removeTable(name: String)

    fun getLecturers(): List<Lecturer>

    fun lookForLecturer(name: String): Lecturer?

    fun sendLecturer(lecturer: Lecturer)

    fun removeLecturer(name: String)

    companion object {
        val timetableSerializer = TimeTable.serializer()
        val lecturerSerializer = object: KSerializer<Lecturer> {
            override fun deserialize(decoder: Decoder): Lecturer =
                LecturerTransferredSurrogate.serializer().deserialize(decoder).toLecturer()

            override val descriptor: SerialDescriptor
                get() = LecturerTransferredSurrogate.serializer().descriptor

            override fun serialize(encoder: Encoder, value: Lecturer) =
                LecturerTransferredSurrogate.serializer().serialize(encoder, value.toSurrogate())
        }
    }
}
