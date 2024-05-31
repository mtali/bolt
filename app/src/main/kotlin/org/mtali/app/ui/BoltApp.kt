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

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.mtali.R
import org.mtali.app.main.MainUiState
import org.mtali.app.navigator.BoltNavHost
import org.mtali.core.utils.areLocationPermissionGranted
import org.mtali.core.utils.devicePermissionStatus
import org.mtali.core.utils.openSettings
import org.mtali.features.login.navigation.navigation.loginRoute
import org.mtali.features.passenger.navigation.passengerRoute
import timber.log.Timber

@Composable
fun BoltApp(
  appState: BoltAppState,
  uiState: MainUiState,
  onLogout: () -> Unit,
  shouldShowRequestPermissionRationale: () -> Boolean,
) {
  val backStack by appState.backStack.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  // Permissions
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var locationPermissionGranted by remember { mutableStateOf(context.areLocationPermissionGranted()) }
  var shouldShowPermissionRationale by remember { mutableStateOf(shouldShowRequestPermissionRationale()) }
  var shouldDirectUserToSettings by remember { mutableStateOf(false) }
  var currentPermissionsStatus by remember {
    mutableStateOf(devicePermissionStatus(locationPermissionGranted, shouldShowPermissionRationale))
  }
  val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
  )
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
    onResult = { permissions ->
      locationPermissionGranted = permissions.values.reduce { acc, isPermissionGranted ->
        acc && isPermissionGranted
      }

      if (!locationPermissionGranted) {
        shouldShowPermissionRationale =
          ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.ACCESS_COARSE_LOCATION)
      }

      shouldDirectUserToSettings = !shouldShowPermissionRationale && !locationPermissionGranted
      currentPermissionsStatus = devicePermissionStatus(locationPermissionGranted, shouldDirectUserToSettings)
    },
  )

  DisposableEffect(key1 = lifecycleOwner, effect = {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_START &&
        !locationPermissionGranted &&
        !shouldShowPermissionRationale
      ) {
        locationPermissionLauncher.launch(locationPermissions)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  })

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
  ) { innerPadding ->
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
            locationPermissionGranted = locationPermissionGranted,
          )

          if (shouldShowPermissionRationale) {
            LaunchedEffect(Unit) {
              appState.coroutineScope.launch {
                val userAction = snackbarHostState.showSnackbar(
                  message = "Please authorize locations permission",
                  actionLabel = "Approve",
                  duration = SnackbarDuration.Indefinite,
                  withDismissAction = false,
                )
                when (userAction) {
                  SnackbarResult.Dismissed -> {
                    shouldShowPermissionRationale = true
                  }

                  SnackbarResult.ActionPerformed -> {
                    shouldShowPermissionRationale = false
                    locationPermissionLauncher.launch(locationPermissions)
                  }
                }
              }
            }
          }

          if (shouldDirectUserToSettings) {
            context.openSettings()
          }
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
