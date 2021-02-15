package me.dzikimlecz.timetable.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.dzikimlecz.timetable.Cell
import me.dzikimlecz.timetable.TimeTable
import me.dzikimlecz.timetable.timeTableOf

@Serializable
@SerialName("TimeTable")
private data class TimeTableSurrogate(val table: List<List<Cell>>) {
    init {
        require(table.stream().allMatch {it.size == table[0].size}) {"This list is not a table!"}
    }
}


object TimeTableSerializer : KSerializer<TimeTable> {
    override val descriptor: SerialDescriptor = TimeTableSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: TimeTable) = encoder.encodeSerializableValue(
        TimeTableSurrogate.serializer(), TimeTableSurrogate(value.list())
    )

    override fun deserialize(decoder: Decoder): TimeTable {
        val surrogate = decoder.decodeSerializableValue(TimeTableSurrogate.serializer())
        return timeTableOf(surrogate.table)
    }
}