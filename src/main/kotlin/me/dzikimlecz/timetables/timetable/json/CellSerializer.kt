package me.dzikimlecz.timetables.timetable.json

import javafx.geometry.Orientation
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.dzikimlecz.timetables.timetable.Cell

@Serializable
@SerialName("Cell")
private data class CellSurrogate(val content: List<String>,
                                 val divisionOrientation: Orientation?= null) {
    init { require(content.size in 1..2) }
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
        cell.divisionDirection = surrogate.divisionOrientation
        return cell
    }
}