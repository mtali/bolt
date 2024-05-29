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
package org.mtali.app.ui

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun rememberBoltAppState(
  navController: NavHostController = rememberNavController(),
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
): BoltAppState {
  return BoltAppState(
    navController = navController,
    coroutineScope = coroutineScope,
  )
}

@Stable
@SuppressLint("RestrictedApi")
class BoltAppState(
  val navController: NavHostController,
  val coroutineScope: CoroutineScope,
) {

  private val isNavigating = AtomicBoolean(false)

  /**
   * This is for observing back stack
   */
  val backStack = navController.currentBackStack
    .map { stackEntries ->
      stackEntries.map { entry -> entry.destination.route }
    }
    .stateIn(
      scope = coroutineScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptyList(),
    )

  /**
   * Only navigate when we are not navigating
   * USE CASE: Prevent/Reduce multiple back clicks from a single screen
   */
  private fun checkNavigation(block: () -> Unit) {
    coroutineScope.launch {
      if (isNavigating.compareAndSet(false, true)) {
        block()
        delay(500)
        isNavigating.set(false)
      }
    }
  }

  fun onBackClick() = checkNavigation {
    navController.popBackStack()
  }
}
