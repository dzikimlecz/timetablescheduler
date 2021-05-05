package me.dzikimlecz.timetables.timetable

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.dzikimlecz.serializers.DateSerializer
import tornadofx.sizeProperty
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.naming.OperationNotSupportedException

object TimeTableSerializer : KSerializer<TimeTable> {
    override val descriptor: SerialDescriptor = TimeTableSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: TimeTable) = with (value) {
        val nameToSave = name.ifBlank { date.format(DateTimeFormatter.ISO_DATE) }
        encoder.encodeSerializableValue(
            TimeTableSurrogate.serializer(),
            TimeTableSurrogate(date, nameToSave, list, columnsTimeSpan.map { it.toTypedArray()}, titles.map {it.value })
        )
    }

    override fun deserialize(decoder: Decoder): TimeTable {
        val surrogate = decoder.decodeSerializableValue(TimeTableSurrogate.serializer())
        val timeTable = timeTableOf(surrogate.table)
        with(surrogate) {
            timeTable.name = name
            timeTable.date = date
            if (timeSpans != null) {
                timeTable.columnsTimeSpan.clear()
                timeSpans.map {
                    FXCollections.observableArrayList(*it).apply {
                        sizeProperty.addListener { _, _, _ ->
                            throw OperationNotSupportedException("Lists of TimeSpans must have fixed size.")
                        }
                    }
                }.also { timeTable.columnsTimeSpan.addAll(it) }
            }
            if (titles != null) {
                timeTable.titles.clear()
                timeTable.titles += titles.map {
                    SimpleStringProperty(it)
                }
            }
        }
        return timeTable
    }

    @Serializable
    @SerialName("TimeTable")
    private class TimeTableSurrogate(
        @Serializable(with = DateSerializer::class) val date: LocalDate,
        val name: String,
        val table: List<List<Cell>>,
        val timeSpans: List<Array<TimeSpan?>>?= null,
        val titles: List<String>?= null,
    ) {
        init {
            if (table.isNotEmpty()) {
                val size = table[0].size
                require(table.stream().allMatch { it.size == size }) { "This list is not a table!" }
                require((timeSpans?.size ?: size) == size) {
                    "Size of time spans: ${timeSpans?.size} doesn't match size of the table: $size)"
                }
                require((titles?.size ?: size) == size) {
                    "Size of titles: ${titles?.size} doesn't match size of the table: $size)"
                }
            }
        }
    }
}


object CellSerializer : KSerializer<Cell> {
    override val descriptor: SerialDescriptor = CellSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Cell) {
        val content = mutableListOf(value[0])
        if (value.isDivided) content.add(value[1])
        encoder.encodeSerializableValue(CellSurrogate.serializer(),
            CellSurrogate(content, value.divisionDirection))
    }

    override fun deserialize(decoder: Decoder): Cell {
        val surrogate = decoder.decodeSerializableValue(CellSurrogate.serializer())
        val cell = Cell(surrogate.content.size == 2)
        for ((i, element) in surrogate.content.withIndex()) cell[i] = element
        cell.divisionDirection =
            surrogate.divisionOrientation ?: if (cell.isDivided) Orientation.HORIZONTAL else null
        return cell
    }

    @Serializable
    @SerialName("Cell")
    private class CellSurrogate(val content: List<String>,
                                val divisionOrientation: Orientation?= null) {
        init { require(content.size in 1..2) }
    }
}


object TimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("day", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalTime = LocalTime.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: LocalTime) =
        encoder.encodeString(value.format(DateTimeFormatter.ISO_TIME))
}
