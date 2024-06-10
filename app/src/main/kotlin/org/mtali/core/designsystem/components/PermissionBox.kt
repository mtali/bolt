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
package org.mtali.core.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.sharp.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.mtali.R
import org.mtali.core.utils.openAppSettings

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun PermissionBox(
  modifier: Modifier = Modifier,
  permissions: List<String>,
  requiredPermissions: List<String> = permissions,
  description: String? = null,
  contentAlignment: Alignment = Alignment.TopStart,
  onGranted: @Composable BoxScope.(List<String>) -> Unit,
) {
  val context = LocalContext.current

  val permissionState = rememberMultiplePermissionsState(permissions = permissions)
  val allRequiredPermissionsGranted =
    permissionState.revokedPermissions.none { it.permission in requiredPermissions }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .then(modifier),
    contentAlignment = if (allRequiredPermissionsGranted) contentAlignment else Alignment.Center,
  ) {
    if (allRequiredPermissionsGranted) {
      onGranted(permissionState.permissions.filter { it.status.isGranted }.map { it.permission })
    } else {
      PermissionsScreen(
        state = permissionState,
        description = description,
      )

      FloatingActionButton(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(16.dp),
        onClick = { context.openAppSettings() },
      ) {
        Icon(imageVector = Icons.Outlined.Settings, contentDescription = "settings")
      }
    }
  }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
private fun PermissionsScreen(
  state: MultiplePermissionsState,
  description: String?,
) {
  var showRationale by remember { mutableStateOf(false) }

  val permissions = remember(state.revokedPermissions) {
    state.revokedPermissions.joinToString("\n") {
      " - " + it.permission.removePrefix("android.permission.")
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .animateContentSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Icon(
      imageVector = Icons.Sharp.Lock,
      contentDescription = "lock",
      modifier = Modifier
        .size(90.dp)
        .padding(16.dp),
    )
    Text(
      text = stringResource(id = R.string._require_permissions, stringResource(id = R.string.app_name)),
      style = MaterialTheme.typography.titleLarge,
      modifier = Modifier.padding(16.dp),

      )
    Text(
      text = permissions,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.padding(16.dp),
    )

    if (description != null) {
      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(16.dp),
      )
    }

    Button(onClick = {
      if (state.shouldShowRationale) {
        showRationale = true
      } else {
        state.launchMultiplePermissionRequest()
      }
    }) {
      Text(text = stringResource(id = R.string.grant_permissions))
    }
  }

  if (showRationale) {
    AlertDialog(
      title = {
        Text(text = stringResource(id = R.string.permissions_required))
      },
      text = {
        Text(text = stringResource(id = R.string.please_accept_permissions))
      },
      onDismissRequest = { showRationale = false },
      confirmButton = {
        TextButton(
          onClick = {
            showRationale = false
            state.launchMultiplePermissionRequest()
          },
        ) {
          Text(text = stringResource(id = R.string.continue_))
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showRationale = false
          },
        ) {
          Text(text = stringResource(id = R.string.dismiss))
        }
      },
    )
  }
}
