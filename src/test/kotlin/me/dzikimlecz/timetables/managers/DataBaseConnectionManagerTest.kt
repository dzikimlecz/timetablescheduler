package me.dzikimlecz.timetables.managers

import me.dzikimlecz.timetables.timetable.TimeTable
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

//  -------------- SERVER MUST BE RUNNING FOR THESE TO PASS! -----------------------------------------------------------
//  -------------- IF DATA SOURCES ARE MOCKED/ALMOST EMPTY, YOU SHOULD RESTART SERVER EVERY TIME -----------------------

@TestInstance(PER_CLASS)
internal class DataBaseConnectionManagerTest {
    val manager: DataBaseConnectionManager =
        KhttpDataBaseConnectionManager()

    @BeforeAll
    fun waitForServerToStart() = Thread.sleep(7_000)

    @Nested
    @DisplayName("Connection to the server")
    @TestInstance(PER_CLASS)
    inner class ConnectionTest {
        @Test
        fun `should connect to the server`() {
            assertDoesNotThrow { manager.tryToConnect() }
        } 
    }    
    
    @Nested
    @DisplayName("Retaining tables")
    @TestInstance(PER_CLASS)
    inner class RetainTables {
        @Test
        fun `should retain all tables from database`() {
            // when
            val timeTables = manager.getTimeTables()
            // then
            assertTrue(timeTables.isNotEmpty())
        }

        @Test
        fun `should retain single table`() {
            // when
            val table = manager.lookForTable("test")
            // then
            assertNotNull(table)
        }

        @Test
        fun `should fail to retain not existent table`() {
            // when
            val table = manager.lookForTable("there is no 4e90d3bf-2fdf-4c21-83e8-b578040c2e8b")
            // then
            assertNull(table)
        }
    }

    @Nested
    @DisplayName("Sending a table")
    @TestInstance(PER_CLASS)
    inner class SendingTimeTable {
        @Test
        fun `should post a new table`() {
            // when
            val table = TimeTable(2, 2, name = "UwU")
            //then
            assertDoesNotThrow { manager.sendTable(table) }
        }

        @Test
        fun `should patch existing table`() {
            // when
            val table = TimeTable(2, 2, name = "test")
            //then
            assertDoesNotThrow { manager.sendTable(table) }
        }
    }
    
    @Nested
    @DisplayName("Deleting a table")
    @TestInstance(PER_CLASS)
    inner class DeletingTable {
        @Test
        fun `should delete ordered table`() {
            // when
            val name = "test"
            // then
            assertDoesNotThrow { manager.removeTable(name) }
            assertNull(manager.lookForTable(name))
        }

        @Test
        fun `should fail to delete nonexistent table`() {
            // given
            val name = "there is no a7b5f507-cf10-46b5-94dc-bd856e3fa9e4"
            // when
            assertNull(manager.lookForTable(name))
            // then
            assertThrows<IllegalStateException> { manager.removeTable(name) }
        }
    }
    
    @Nested
    @DisplayName("Getting Lecturers")
    @TestInstance(PER_CLASS)
    inner class GetLecturers {
        @Test
        fun `should get all lecturers from the server`() =
            assertDoesNotThrow { manager.getLecturers() }

        @Test
        fun `should get a specific lecturer from the server`() {
            // given
            val code = manager.getLecturers()[0].code
            // when
            val lecturer = manager.lookForLecturer(code)
            // then
            assertNotNull(lecturer)
        }

        @Test
        fun `should fail to get a nonexistent lecturer from the server`() {
            // given
            val code = "there is no f0b5fcdb-0ae5-4890-b816-a57ac24f772e"
            // when
            val lecturer = manager.lookForLecturer(code)
            // then
            assertNull(lecturer)
        }
    }
    
    @Nested
    @DisplayName("Sending a lecturer")
    @TestInstance(PER_CLASS)
    inner class SendLecturer {
        @Test
        fun `should post a lecturer`() {
            // given
            val lecturer = manager.getLecturers()[0].derive(code = "FA26AAf")
            // when
            assertNull(manager.lookForLecturer(lecturer.code))
            // then
            assertDoesNotThrow { manager.sendLecturer(lecturer) }
        }
        @Test
        fun `should patch a lecturer`() {
            // given
            val lecturer = manager.getLecturers()[0]
            // then
            assertDoesNotThrow { manager.sendLecturer(lecturer) }
        }
    }

    @Nested
    @DisplayName("Deleting lecturers")
    @TestInstance(PER_CLASS)
    inner class DeleteLecturer {
        @Test
        fun `should delete an existing lecturer`() {
            // when
            val code = manager.getLecturers()[0].code
            // then
            assertDoesNotThrow { manager.removeLecturer(code) }
            assertNull(manager.lookForLecturer(code))
        }

        @Test
        fun `should fail to delete nonexistent lecturer`() {
            // given
            val code = manager.getLecturers()[0].code
            //when
            manager.removeLecturer(code)
            assertNull(manager.lookForLecturer(code))
            // then
            assertThrows<IllegalStateException> { manager.removeLecturer(code) }
        }
    }
    
}