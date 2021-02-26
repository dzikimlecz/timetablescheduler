package me.dzikimlecz.timetables.timetable.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import me.dzikimlecz.timetables.timetable.Cell
import me.dzikimlecz.timetables.timetable.TimeTable
import me.dzikimlecz.timetables.timetable.timeTableOf
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
@SerialName("TimeTable")
private data class TimeTableSurrogate(
    @Serializable(with = DateSerializer::class) val date: LocalDate,
    @Required val name: String = "",
    val table: List<List<Cell>>,
) {
    init {
        require(table.stream().allMatch {it.size == table[0].size}) {"This list is not a table!"}
    }
}

object TimeTableSerializer : KSerializer<TimeTable> {
    override val descriptor: SerialDescriptor = TimeTableSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: TimeTable) {
        val name = value.name.ifBlank { value.date.format(DateTimeFormatter.ISO_LOCAL_DATE) }
        encoder.encodeSerializableValue(
            TimeTableSurrogate.serializer(),
            TimeTableSurrogate(value.date, name, value.list())
        )
    }

    override fun deserialize(decoder: Decoder): TimeTable {
        val surrogate = decoder.decodeSerializableValue(TimeTableSurrogate.serializer())
        val timeTable = timeTableOf(surrogate.table, surrogate.date)
        timeTable.name = surrogate.name
        return timeTable
    }
}

class DateSerializer : KSerializer<LocalDate>{
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("day", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: LocalDate) =
        encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE))
}