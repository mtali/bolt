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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.SwitchRight
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.mtali.R
import org.mtali.app.main.MainUiState
import org.mtali.app.main.getRoute
import org.mtali.app.navigator.BoltNavHost
import org.mtali.core.models.UserType
import org.mtali.core.utils.areLocationPermissionGranted
import org.mtali.core.utils.devicePermissionStatus
import org.mtali.core.utils.openSettings
import timber.log.Timber

@Composable
fun BoltApp(
  appState: BoltAppState,
  uiState: MainUiState,
  onLogout: () -> Unit,
  shouldShowRequestPermissionRationale: () -> Boolean,
  onLocationPermissionGranted: () -> Unit,
  onToggleUserType: () -> Unit,
) {
  val backStack by appState.backStack.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  // Permissions
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var isLocationPermissionGranted by remember { mutableStateOf(context.areLocationPermissionGranted()) }
  var shouldShowPermissionRationale by remember { mutableStateOf(shouldShowRequestPermissionRationale()) }
  var shouldDirectUserToSettings by remember { mutableStateOf(false) }
  var currentPermissionsStatus by remember {
    mutableStateOf(devicePermissionStatus(isLocationPermissionGranted, shouldShowPermissionRationale))
  }
  val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
  )
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
    onResult = { permissions ->
      isLocationPermissionGranted = permissions.values.reduce { acc, isPermissionGranted ->
        acc && isPermissionGranted
      }

      if (isLocationPermissionGranted) onLocationPermissionGranted()

      if (!isLocationPermissionGranted) {
        shouldShowPermissionRationale =
          ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.ACCESS_COARSE_LOCATION)
      }

      shouldDirectUserToSettings = !shouldShowPermissionRationale && !isLocationPermissionGranted
      currentPermissionsStatus = devicePermissionStatus(isLocationPermissionGranted, shouldDirectUserToSettings)
    },
  )

  DisposableEffect(key1 = lifecycleOwner, effect = {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_START &&
        !isLocationPermissionGranted &&
        !shouldShowPermissionRationale
      ) {
        locationPermissionLauncher.launch(locationPermissions)
      }
    }
    if (isLocationPermissionGranted) onLocationPermissionGranted()
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  })

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

  ModalNavigationDrawer(
    drawerState = drawerState,
    gesturesEnabled = drawerState.isOpen,
    drawerContent = {
      if (uiState is MainUiState.Success) {
        DrawerContent(
          userType = uiState.userType,
          drawerState = drawerState,
          onLogout = onLogout,
          onToggleUserType = onToggleUserType,
        )
      }
    },
  ) {
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
              modifier = Modifier.fillMaxSize(),
              startDestination = uiState.getRoute(),
              appState = appState,
              locationPermissionGranted = isLocationPermissionGranted,
              onClickDrawerMenu = {
                appState.coroutineScope.launch {
                  drawerState.open()
                }
              },
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

@Composable
private fun DrawerContent(
  modifier: Modifier = Modifier,
  userType: UserType,
  drawerState: DrawerState,
  onLogout: () -> Unit,
  onToggleUserType: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  ModalDrawerSheet(modifier = modifier) {
    Text(
      text = stringResource(id = R.string.app_name),
      fontSize = 30.sp,
      modifier = Modifier.padding(16.dp),
    )

    HorizontalDivider()

    NavigationDrawerItem(
      label = {
        val text = if (userType == UserType.DRIVER) R.string.switch_to_passenger else R.string.switch_to_driver
        Text(text = stringResource(id = text))
      },
      icon = {
        Icon(imageVector = Icons.Outlined.SwitchRight, contentDescription = "switch")
      },
      selected = false,
      onClick = {
        scope.launch { drawerState.close() }.invokeOnCompletion {
          onToggleUserType()
        }
      },
    )

    Spacer(modifier = Modifier.weight(1f))

    NavigationDrawerItem(
      label = { Text(text = stringResource(id = R.string.logout)) },
      selected = false,
      icon = {
        Icon(imageVector = Icons.AutoMirrored.Outlined.Logout, contentDescription = "logout")
      },
      onClick = {
        scope.launch { drawerState.close() }.invokeOnCompletion {
          onLogout()
        }
      },
    )
  }
}
