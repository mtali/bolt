package org.mtali.core.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mtali.R
import org.mtali.core.models.ToastMessage


val ELEMENT_WIDTH = 290.dp

val emailPattern = "^[A-Za-z](.*)([@])(.+)(\\\\.)(.+)".toRegex()

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

fun String.isValidEmail() = emailPattern.matches(this)

fun Context.handleToast(code: ToastMessage) {
    val message: Int = when (code) {
        ToastMessage.SERVICE_ERROR -> R.string.service_error
        ToastMessage.INVALID_CREDENTIALS -> R.string.invalid_credentials
        ToastMessage.INVALID_INPUT -> R.string.invalid_input
    }
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
