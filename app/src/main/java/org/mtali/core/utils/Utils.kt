package org.mtali.core.utils

import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.dp


val ELEMENT_WIDTH = 290.dp

inline fun <T> MutableState<T>.update(block: (T) -> T) {
    value = block(value)
}