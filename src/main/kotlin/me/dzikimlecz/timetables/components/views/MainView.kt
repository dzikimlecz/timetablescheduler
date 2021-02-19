package me.dzikimlecz.timetables.components.views

import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import me.dzikimlecz.timetables.managers.Manager
import tornadofx.*
import java.util.concurrent.atomic.AtomicInteger as BoxedInt
import me.dzikimlecz.timetables.components.views.TimeTableSetUpView as SetUpView


class MainView : View("Układacz planów 3tysionce !!!") {
    private val manager : Manager by inject()

    override val root = borderpane {
        left {
            vbox {
                spacing = 3E1
                background = Background(BackgroundFill(Color.LIGHTGREY, null, null))
                padding = Insets(1.5E1, 0.0, 0.0, 0.0)
                val buttonWidth = 1.8E2
                val buttonHeight = 5E1
                button("Nowy Plan") {
                    action {
                        val dimensions = Pair(BoxedInt(), BoxedInt())
                        find<SetUpView>(mapOf(SetUpView::dimensions to dimensions))
                            .openModal(StageStyle.UTILITY, resizable = false, block = true)

                    }
                }
                button("Otwórz Plan")
                button("Zapisz Plan")
                button("Dodaj Plan do Bazy Godzin")
                button("Otwórz Bazę Godzin")
                children.forEach {
                    if (it is Button) {
                        it.prefWidth = buttonWidth
                        it.prefHeight = buttonHeight
                    }
                }
            }
        }
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        setWindowMinSize(800, 500)
    }
}
