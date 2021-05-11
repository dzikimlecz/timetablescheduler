package me.dzikimlecz.timetables.components.views.dialogs

import org.junit.jupiter.api.Test
import tornadofx.App
import tornadofx.launch

internal class LecturerSetUpViewTest {

    @Test
    fun `should show the window`() {
        class TestApp : App(LecturerSetUpView::class) {}
        launch<TestApp>()
    }

}