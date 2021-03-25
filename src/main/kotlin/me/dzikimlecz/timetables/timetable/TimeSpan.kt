package me.dzikimlecz.timetables.timetable

import kotlinx.serialization.Serializable
import me.dzikimlecz.timetables.timetable.json.TimeSerializer
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Serializable
class TimeSpan private constructor(@Serializable(with = TimeSerializer::class) val start: LocalTime,
                                   @Serializable(with = TimeSerializer::class) val end: LocalTime) {
    companion object {
        private val spans = mutableSetOf<TimeSpan>()
        fun of(start: LocalTime, end: LocalTime): TimeSpan {
            val filtered = spans.filter { it.start == start }.filter { it.end == end }
            if (filtered.isNotEmpty())
                return filtered[0]
            val newTimeSpan = TimeSpan(start, end)
            spans += newTimeSpan
            return newTimeSpan
        }
    }

    override fun toString(): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        return "${start.format(formatter)}-${end.format(formatter)}"
    }


}
