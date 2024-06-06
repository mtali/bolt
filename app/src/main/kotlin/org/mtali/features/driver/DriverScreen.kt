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
package org.mtali.features.driver

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.mtali.core.designsystem.components.MapDashboard

@Composable
fun DriverRoute(locationPermissionGranted: Boolean, onClickDrawerMenu: () -> Unit) {
  DriverScreen(
    onMapLoaded = {},
    onClickDrawerMenu = onClickDrawerMenu,
    locationPermissionGranted = locationPermissionGranted,
  )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DriverScreen(
  onMapLoaded: () -> Unit,
  onClickDrawerMenu: () -> Unit,
  locationPermissionGranted: Boolean,
) {
  val sheetState = rememberStandardBottomSheetState(skipHiddenState = true, confirmValueChange = { false })

  MapDashboard(
    onMapLoaded = onMapLoaded,
    sheetState = sheetState,
    onClickDrawerMenu = onClickDrawerMenu,
    sheetFillHeight = false,
    forceShowDragHandle = true,
    locationPermissionGranted = locationPermissionGranted,
    showDrawerMenu = true,
    mapContent = {
    },
    sheetContent = {
      Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "Looking for rides")
      }
    },
  )
}
