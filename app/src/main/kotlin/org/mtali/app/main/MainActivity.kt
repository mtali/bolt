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
package org.mtali.app.main

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.mtali.app.ui.BoltApp
import org.mtali.app.ui.rememberBoltAppState
import org.mtali.core.designsystem.BoltTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val viewModel by viewModels<MainViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    var uiState: MainUiState by mutableStateOf(MainUiState.Loading)
    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState
          .onEach { uiState = it }
          .collect()
      }
    }

    splashScreen.setKeepOnScreenCondition { uiState !is MainUiState.Success }

    setContent {
      val appState = rememberBoltAppState()
      BoltTheme {
        BoltApp(
          appState = appState,
          uiState = uiState,
          onLogout = viewModel::onLogout,
          shouldShowRequestPermissionRationale = {
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
          },
        )
      }
    }
  }
}
