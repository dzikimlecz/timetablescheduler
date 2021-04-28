package me.dzikimlecz.timetables.components.views

import javafx.collections.FXCollections.observableArrayList
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback as Factory
import me.dzikimlecz.lecturers.Lecturer
import me.dzikimlecz.timetables.timetable.TimeTable
import tornadofx.*


class DataBaseControlPanelView: View() {
    val lecturers by param<List<Lecturer>>()
    val tables by param<List<TimeTable>>()

    override val root = borderpane {
        center = borderpane {
            left = listview(observableArrayList(lecturers)) {
                cellFactory = Factory {
                    object : ListCell<Lecturer>() {
                        override fun updateItem(item: Lecturer?, empty: Boolean) {
                            super.updateItem(item, empty)
                            text = (if (empty) null else item?.name)
                        }
                    }
                }
            }
            right = listview(observableArrayList(tables)) {
                cellFactory = Factory {
                    object : ListCell<TimeTable>() {
                        override fun updateItem(item: TimeTable?, empty: Boolean) {
                            super.updateItem(item, empty)
                            text = (if (empty) null else item?.name)
                        }
                    }
                }
            }
        }
    }
}