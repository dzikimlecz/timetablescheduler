package me.dzikimlecz.timetables.timetable

import kotlinx.serialization.Serializable
import tornadofx.isInt
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Serializable
class TimeSpan private constructor(@Serializable(with = TimeSerializer::class) val start: LocalTime,
                                   @Serializable(with = TimeSerializer::class) val end: LocalTime) {

    init { require(start.isBefore(end)) { "Start can't be before or equal to end" } }

    override fun toString(): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        return "${start.format(formatter)}-${end.format(formatter)}"
    }

    companion object {
        private val separator = Regex("[:;,.-]")
        val pattern = Regex("\\d{1,2}[:;,.-]\\d{2}")

        private val spans = mutableSetOf<TimeSpan>()

        @JvmStatic fun of(start: LocalTime, end: LocalTime): TimeSpan = try {
            spans.first { it.start == start && it.end == end }
        } catch(e: NoSuchElementException) {
            val newTimeSpan = TimeSpan(start, end)
            spans += newTimeSpan
            newTimeSpan
        }

        @JvmStatic fun of(start: String, end: String): TimeSpan {
            if (!pattern.matches(start))
                throw IllegalArgumentException("\"$start\" doesn't match TimeSpan pattern")
            if (!pattern.matches(end))
                throw IllegalArgumentException("\"$end\" doesn't match TimeSpan pattern")

            val startISO = StringBuilder(start)
            val endISO = StringBuilder(end)

            if (start.length == 4) startISO.insert(0, '0')
            if (end.length == 4) endISO.insert(0, '0')

            startISO[2] = ':'
            endISO[2] = ':'

            return try { of(LocalTime.parse(startISO), LocalTime.parse(endISO)) }
            catch(e: Exception) {
                throw IllegalArgumentException("TimeSpan could not be created", e)
            }
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
