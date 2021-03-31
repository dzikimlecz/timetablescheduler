package me.dzikimlecz.timetables.timetable

import kotlinx.serialization.Serializable
import me.dzikimlecz.timetables.timetable.json.TimeSerializer
import tornadofx.isInt
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Serializable
class TimeSpan private constructor(@Serializable(with = TimeSerializer::class) val start: LocalTime,
                                   @Serializable(with = TimeSerializer::class) val end: LocalTime) {

    override fun toString(): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        return "${start.format(formatter)}-${end.format(formatter)}"
    }

    companion object {
        private val separator = Regex("[:;,.-]")
        val pattern = Regex("\\d{1,2}[:;,.-]\\d{2}")

        @JvmStatic private val spans = mutableSetOf<TimeSpan>()

        @JvmStatic fun of(start: LocalTime, end: LocalTime): TimeSpan {
            val filtered = spans.filter { it.start == start }.filter { it.end == end }
            if (filtered.isNotEmpty())
                return filtered[0]
            val newTimeSpan = TimeSpan(start, end)
            spans += newTimeSpan
            return newTimeSpan
        }


        fun validate(st: String) = pattern.matches(st)

        fun validateAsBeginning(st: String) = when(st.length) {
            1 -> st.isInt()
            2 -> st.isInt() ||
                    (st[0].isDigit() && separator.containsMatchIn(st))
            3 -> separator.containsMatchIn(st) &&
                    (st.substring(0..1).isInt() ||
                            (st[0].isDigit() && st[2].isDigit()))
            4 -> separator.containsMatchIn(st) &&
                    ((st.substring(0..1).isInt() && st[3].isDigit()) ||
                            (st[0].isDigit() && st.substring(2..3).isInt()))
            5 -> validate(st)
            else -> false
        }
    }
}
