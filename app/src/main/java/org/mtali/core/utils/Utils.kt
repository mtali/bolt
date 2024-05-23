package org.mtali.core.utils

import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


val ELEMENT_WIDTH = 290.dp

inline fun <T> MutableState<T>.update(block: (T) -> T) {
    value = block(value)
}

fun CoroutineScope.preventMultipleLaunches(
    mutex: Mutex,
    operation: suspend () -> Unit
) {
    launch {
        if (mutex.isLocked) return@launch
        mutex.withLock {
            operation()
        }
    }
}