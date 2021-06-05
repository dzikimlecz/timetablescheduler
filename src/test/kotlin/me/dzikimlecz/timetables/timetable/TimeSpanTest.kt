package me.dzikimlecz.timetables.timetable

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.time.LocalTime

internal class TimeSpanTest {

    @Test
    fun `should create TimeSpan from 2 times`() {
        val time0 = LocalTime.parse("00:00")
        val time1 = time0.plusHours(1)
        assertDoesNotThrow { TimeSpan.of(time0, time1) }
    }

    @Test
    fun `should fail to create TimeSpan from times when end is not after start`() {
        //when
        val time0 = LocalTime.parse("23:59")
        val time1 = time0.minusHours(1)
        //then
        assertThrows(IllegalArgumentException::class.java) {
            TimeSpan.of(time0, time1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            TimeSpan.of(time0, time0)
        }
    }

    @Test
    fun `should create only one distinct object when called multiple times`() {
        //when
        val time0 = LocalTime.parse("00:00")
        val time1 = time0.plusHours(1)
        val span0 = TimeSpan.of(time0, time1)
        val span1 = TimeSpan.of(time0, time1)
        //then
        assertSame(span0, span1)
    }

    @Test
    fun `should create TimeSpan from strings`() {
        //when
            val time0 = "00:00"
            val time1 = "00:01"
        //then
            assertDoesNotThrow {
                TimeSpan.of(time0, time1)
            }
    }

    @Test
    fun `should created TimeSpan from not formatted strings`() {
        //when
            val time0 = "0.00"
            val time1 = "0-01"
        //then
            assertDoesNotThrow {
                TimeSpan.of(time0, time1)
            }
    }

    @Test
    fun `should fail to create TimeSpan from invalid strings`() {
        //when
            val time0 = "0.60"
            val time1 = "90:91"
            val time2 = "mayonnaise"
            val time3 = "Poland the mountain"
        //then
        assertThrows(IllegalArgumentException::class.java) {
            TimeSpan.of(time0, time1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            TimeSpan.of(time2, time3)
        }
    }

    @Test
    fun `should validate strings as beginnings of the TimeSpan string representations`() {
        // given
        val strings = listOf(
            "",
            "1",
            "5",
            "15",
            "17",
            "1:",
            "9:",
            "19:",
            "29:",
            "2:1",
            "3:5",
            "13:5",
            "23:6",
            "3:50",
            "2:41",
            "13:66",
            "43:66",
        )
        // when
            val matcher = { st: String -> TimeSpan.validateAsBeginning(st) }
        // then
            assertTrue(strings.all(matcher))
    }

    @Test
    fun `should not validate invalid strings as TimeSpan beginnings`() {
        // given
        val strings = listOf(
            "p",
            "122",
            "15::",
            "21:22:",
            "21:223",
            "1:1:",
            "9::2",
        )
        // when
        val matcher = { st: String -> TimeSpan.validateAsBeginning(st) }
        // then
        strings.asSequence().filter(matcher).forEach { fail("""String "$it" shouldn't match, but it does.""") }
    }
}