/*
 * Designed and developed by 2024 mtali (Emmanuel Mtali)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  operation: suspend () -> Unit,
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
    ToastMessage.ACCOUNT_EXISTS -> R.string.account_exists
    ToastMessage.ACCOUNT_CREATED -> R.string.account_created
  }
  Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun String.capitalizeWords(): String =
  split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
