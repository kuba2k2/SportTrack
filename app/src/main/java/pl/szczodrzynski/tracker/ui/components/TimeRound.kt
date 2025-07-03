package pl.szczodrzynski.tracker.ui.components

import kotlin.math.ceil

fun Float.roundTimeUp() = ceil(this / 10) / 100
fun Float.roundTime() = ceil(this / 10 + 0.5) / 100
