package me.dzikimlecz.timetables.components.views.dialogs

import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.text.Font.font
import javafx.util.Duration
import me.dzikimlecz.lecturers.Lecturer
import tornadofx.*
import java.util.concurrent.atomic.AtomicReference

class LecturerSetUpView : View("Nowy Wykładowca") {
    private var nameField by singleAssign<TextField>()
    private var codeField by singleAssign<TextField>()
    private var isCodeCustom by singleAssign<CheckBox>()
    val lecturerContainer = AtomicReference<Lecturer?>(null)

    override val root = form {
        fieldset("Dane") {
            field("Imię i Nazwisko") {
                nameField = textfield()
            }
            field("Kod") {
                isCodeCustom = checkbox("Na podstawie nazwiska") {
                    tooltip = tooltip("Jan Kowalski \u2794 JK") {
                        this.font = font(12.0)
                        showDelay = Duration.ZERO
                    }
                    isSelected = true
                }
                codeField = textfield {
                    visibleProperty().bind(!isCodeCustom.selectedProperty())
                }
            }
        }
        buttonbar {
            button("Ok") {
                isDefaultButton = true
                action {
                    val lecturer =
                        if (isCodeCustom.isSelected) Lecturer(nameField.text, hoursWorked = emptyMap())
                        else Lecturer(nameField.text, codeField.text, emptyMap())
                    lecturerContainer.set(lecturer)
                    close()
                    this@LecturerSetUpView.clear()
                }
            }
            button("Anuluj") {
                isCancelButton = true
                action {
                    close()
                    this@LecturerSetUpView.clear()
                }
            }
        }
    }

    private fun clear() {
        nameField.text = ""
        codeField.text = ""
        isCodeCustom.isSelected = true
    }
}