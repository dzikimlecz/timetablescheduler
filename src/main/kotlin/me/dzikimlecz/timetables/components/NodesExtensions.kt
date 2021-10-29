package me.dzikimlecz.timetables.components

import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane

/**
 * Returns node of the given location constraints located in the applied GridPane
 */
operator fun GridPane.get(x: Int, y: Int) =
    children.firstOrNull { GridPane.getColumnIndex(it) == x && GridPane.getRowIndex(it) == y }

/**
 * Removes node of the given location constraints located in the applied GridPane
 */
fun GridPane.remove(x: Int, y: Int) =
    children.remove(this[x, y])

/**
 * Returns GridPane location constrains of the given [node] in o form of a Pair of 2 ints.
 */
fun locate(node: Node?) =
    GridPane.getRowIndex(node) to GridPane.getColumnIndex(node)

/**
 * property used to simplify setting of the object's margin in the [BorderPane]
 * shorthand for the [BorderPane.getMargin] and [BorderPane.setMargin]
 */
var Node.margin: Insets
    get() = BorderPane.getMargin(this) ?: Insets.EMPTY
    set(value) = BorderPane.setMargin(this, value)
