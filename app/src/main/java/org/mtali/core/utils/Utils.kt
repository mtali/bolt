package org.mtali.core.utils

import androidx.compose.runtime.MutableState

inline fun <T> MutableState<T>.update(block: (T) -> T) {
    value = block(value)
}