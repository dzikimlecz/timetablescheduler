package me.dzikimlecz.timetables.components.fragments

import me.dzikimlecz.timetables.components.views.LecturerWorkTimeDisplay
import org.junit.jupiter.api.Test
import tornadofx.App
import tornadofx.launch

internal class LecturerWorkTimeDisplayTest {
    @Test
    fun `should show the window`() {
        class TestApp : App(LecturerWorkTimeDisplay::class)
        launch<TestApp>()
    }
}