package me.dzikimlecz.timetables.timetable.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.dzikimlecz.timetables.timetable.Cell
import me.dzikimlecz.timetables.timetable.TimeSpan
import me.dzikimlecz.timetables.timetable.TimeTable
import me.dzikimlecz.timetables.timetable.timeTableOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
@SerialName("TimeTable")
private class TimeTableSurrogate(
    @Serializable(with = DateSerializer::class) val date: LocalDate,
    @Required val name: String = "",
    val table: List<List<Cell>>,
    val timeSpans : List<Array<TimeSpan?>>?= null
) {
    init {
        require(table.stream().allMatch { it.size == table[0].size }) {"This list is not a table!"}
    }
}

object TimeTableSerializer : KSerializer<TimeTable> {
    override val descriptor: SerialDescriptor = TimeTableSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: TimeTable) {
        val name = value.name.ifBlank { value.date.format(DateTimeFormatter.ISO_DATE) }
        encoder.encodeSerializableValue(
            TimeTableSurrogate.serializer(),
            TimeTableSurrogate(value.date, name, value.list, value.columnsTimeSpan)
        )
    }

    override fun deserialize(decoder: Decoder): TimeTable {
        val surrogate = decoder.decodeSerializableValue(TimeTableSurrogate.serializer())
        val timeTable = timeTableOf(surrogate.table)
        timeTable.name = surrogate.name
        timeTable.date = surrogate.date
        if (surrogate.timeSpans != null) {
            timeTable.columnsTimeSpan.clear()
            timeTable.columnsTimeSpan.addAll(surrogate.timeSpans)
        }
        return timeTable
    }
}
