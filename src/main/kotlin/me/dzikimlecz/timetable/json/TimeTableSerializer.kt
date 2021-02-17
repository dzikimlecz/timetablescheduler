package me.dzikimlecz.timetable.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import me.dzikimlecz.timetable.Cell
import me.dzikimlecz.timetable.TimeTable
import me.dzikimlecz.timetable.timeTableOf
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
@SerialName("TimeTable")
private data class TimeTableSurrogate(
    val table: List<List<Cell>>,
    @Serializable(with = DateSerializer::class) val date: LocalDateTime,
    @Required val name: String = ""
) {
    init {
        require(table.stream().allMatch {it.size == table[0].size}) {"This list is not a table!"}
    }
}

object TimeTableSerializer : KSerializer<TimeTable> {
    override val descriptor: SerialDescriptor = TimeTableSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: TimeTable) {
        val name = value.name.ifBlank {
            value.dateCreated.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace('T', '_')
        }
        encoder.encodeSerializableValue(
            TimeTableSurrogate.serializer(), TimeTableSurrogate(value.list(), value.dateCreated,
                name
            )
        )
    }

    override fun deserialize(decoder: Decoder): TimeTable {
        val surrogate = decoder.decodeSerializableValue(TimeTableSurrogate.serializer())
        val timeTable = timeTableOf(surrogate.table, surrogate.date)
        timeTable.name = surrogate.name
        return timeTable
    }
}

class DateSerializer : KSerializer<LocalDateTime>{
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("date") {
        element<String>("date")
        element<String>("time")
    }

    override fun deserialize(decoder: Decoder): LocalDateTime =
        decoder.decodeStructure(descriptor) {
            var date = ""
            var time = ""
            while (true) when (val index = decodeElementIndex(descriptor)) {
                0 -> date = decodeStringElement(descriptor, 0)
                1 -> time = decodeStringElement(descriptor, 1)
                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index: $index")
            }
            LocalDateTime.parse("${date}T${time}")
        }

    override fun serialize(encoder: Encoder, value: LocalDateTime) =
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 1, value.format(DateTimeFormatter.ISO_LOCAL_DATE))
            encodeStringElement(descriptor, 0, value.format(DateTimeFormatter.ISO_LOCAL_TIME))
        }

}