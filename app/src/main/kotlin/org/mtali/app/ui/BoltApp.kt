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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.mtali.R
import org.mtali.app.main.MainUiState
import org.mtali.app.main.getRoute
import org.mtali.app.navigator.BoltNavHost
import org.mtali.core.models.UserType
import timber.log.Timber

@Composable
fun BoltApp(
  appState: BoltAppState,
  uiState: MainUiState,
  onLogout: () -> Unit,
  onToggleUserType: () -> Unit,
) {
  val backStack by appState.backStack.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

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
      Modifier.padding(innerPadding) //
      Column(
        Modifier
          .fillMaxSize()
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
              onClickDrawerMenu = {
                appState.coroutineScope.launch {
                  drawerState.open()
                }
              },
            )
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
