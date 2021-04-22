package me.dzikimlecz.timetables.managers

import me.dzikimlecz.timetables.timetable.TimeTable
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.lang.IllegalStateException

internal class DataBaseConnectionManagerTest {
    val manager = DataBaseConnectionManager()

    @Nested
    @DisplayName("Connection to the server")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ConnectionTest {
        @Test
        fun `should connect to the server`() {
            assertDoesNotThrow { manager.tryToConnect() }
        } 
    }    
    
    @Nested
    @DisplayName("Retaining tables")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    @DisplayName("Posting a table")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class PostingTimeTable {
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
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    
}