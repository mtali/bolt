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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.mtali.R
import org.mtali.app.main.MainUiState
import org.mtali.app.navigator.BoltNavHost
import org.mtali.features.login.navigation.navigation.loginRoute
import org.mtali.features.passenger.navigation.passengerRoute
import timber.log.Timber

@Composable
fun BoltApp(
  appState: BoltAppState,
  uiState: MainUiState,
  onLogout: () -> Unit,
) {
  val backStack by appState.backStack.collectAsStateWithLifecycle()

  Scaffold { innerPadding ->
    Column(
      Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .windowInsetsPadding(
          WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal,
          ),
        ),
    ) {
      when (uiState) {
        MainUiState.Loading -> {
          LoadingPage()
        }

        is MainUiState.Success -> {
          BoltNavHost(
            startDestination = if (uiState.isLoggedIn) passengerRoute else loginRoute,
            appState = appState,
            onLogout = onLogout,
          )
        }
      }
    }
  }

  // Log backstack
  LaunchedEffect(backStack) {
    Timber.d("BackStack changed: $backStack")
  }
}

@Composable
private fun LoadingPage(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = stringResource(id = R.string.loading))
  }
}
