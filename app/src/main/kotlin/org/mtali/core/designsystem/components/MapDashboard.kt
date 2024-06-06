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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState

private val DEFAULT_CORNER = 10.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MapDashboard(
  modifier: Modifier = Modifier,
  sheetState: SheetState,
  onMapLoaded: () -> Unit,
  onClickDrawerMenu: () -> Unit,
  sheetFillHeight: Boolean = false,
  forceShowDragHandle: Boolean = false,
  locationPermissionGranted: Boolean = false,
  showDrawerMenu: Boolean = false,
  mapContent:
  (
  @Composable @GoogleMapComposable
    () -> Unit
  )? = null,
  sheetContent: @Composable ColumnScope.() -> Unit,
) {
  val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
  val corner = if (sheetState.progress() > 0.85f) 0.dp else DEFAULT_CORNER

  LaunchedEffect(key1 = Unit) {
    sheetState.expand()
  }

  Box(
    modifier = modifier.fillMaxSize(),
  ) {
    BottomSheetScaffold(
      sheetContent = {
        Column(modifier = Modifier.animateContentSize(), content = sheetContent)
      },
      sheetShape = RoundedCornerShape(topEnd = corner, topStart = corner),
      modifier = Modifier.fillMaxSize(),
      scaffoldState = scaffoldState,
      sheetDragHandle = {
        if (!sheetFillHeight || forceShowDragHandle) {
          BottomSheetDefaults.DragHandle()
        }
      },
    ) {
      Map(
        onMapLoaded = onMapLoaded,
        locationPermissionGranted = locationPermissionGranted,
        mapContent = mapContent,
      )
    }

    DrawerMenu(
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(16.dp),
      visible = showDrawerMenu,
      onClick = onClickDrawerMenu,
    )
  }
}

@Composable
private fun Map(
  modifier: Modifier = Modifier,
  onMapLoaded: () -> Unit,
  locationPermissionGranted: Boolean,
  mapContent:
  (
  @Composable @GoogleMapComposable
    () -> Unit
  )?,
) {
  val cameraPositionState = rememberCameraPositionState {}

  GoogleMap(
    modifier = modifier.fillMaxSize(),
    cameraPositionState = cameraPositionState,
    onMapLoaded = onMapLoaded,
    properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
    content = mapContent,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetState.progress(): Float {
  val screenHeightPx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
  val offsetBottomSheet by remember(this) {
    derivedStateOf {
      runCatching { this.requireOffset() }.getOrDefault(0F)
    }
  }
  return (1 - (offsetBottomSheet / screenHeightPx)).coerceIn(0f, 1f)
}
