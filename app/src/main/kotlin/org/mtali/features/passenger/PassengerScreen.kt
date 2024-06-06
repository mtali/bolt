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
package org.mtali.features.passenger

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import org.mtali.R
import org.mtali.core.designsystem.components.DrawerMenu
import org.mtali.core.designsystem.components.Height
import org.mtali.core.designsystem.components.Width
import org.mtali.core.designsystem.components.height
import org.mtali.core.models.PlacesAutoComplete
import org.mtali.core.utils.handleToast
import timber.log.Timber

@Composable
fun PassengerRoute(
  viewModel: PassengerViewModel = hiltViewModel(),
  locationPermissionGranted: Boolean,
  onClickDrawerMenu: () -> Unit,
) {
  val context = LocalContext.current
  val destinationQuery by viewModel.destinationQuery.collectAsStateWithLifecycle()
  val autoCompletePlaces by viewModel.autoCompletePlaces.collectAsStateWithLifecycle()

  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(key1 = uiState) {
    Timber.tag("wakanda:PassengerRoute").d("$uiState")
  }

  viewModel.toastHandler = { context.handleToast(it) }

  PassengerScreen(
    onMapLoaded = viewModel::onMapLoaded,
    locationPermissionGranted = locationPermissionGranted,
    destinationQuery = destinationQuery,
    onDestinationQueryChange = viewModel::onDestinationQueryChange,
    autoCompletePlaces = autoCompletePlaces,
    onClickPlaceAutoComplete = viewModel::onClickPlaceAutoComplete,
    onClickDrawerMenu = onClickDrawerMenu,
    onCancelRide = viewModel::onCancelRide,
    uiState = uiState
  )
}

private val DEFAULT_CORNER = 10.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PassengerScreen(
  onMapLoaded: () -> Unit,
  locationPermissionGranted: Boolean,
  destinationQuery: String,
  onDestinationQueryChange: (String) -> Unit,
  autoCompletePlaces: List<PlacesAutoComplete>,
  onClickPlaceAutoComplete: (PlacesAutoComplete) -> Unit,
  onClickDrawerMenu: () -> Unit,
  uiState: PassengerUiState,
  onCancelRide: () -> Unit
) {
  var fullHeight by remember { mutableStateOf(false) }

  val sheetState = rememberStandardBottomSheetState(skipHiddenState = true, confirmValueChange = { false })
  val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
  val progress = sheetState.progress()
  val sheetExpanded = sheetState.currentValue == SheetValue.Expanded
  val corner = if (progress > 0.85f) 0.dp else DEFAULT_CORNER

  LaunchedEffect(Unit) { sheetState.expand() }

  Box(
    modifier = Modifier.fillMaxSize(),
  ) {
    BottomSheetScaffold(
      sheetContent = {
        Column(modifier = Modifier.animateContentSize()) {
          when (uiState) {
            is PassengerUiState.RideInactive -> {
              Column(modifier = if (fullHeight) Modifier.fillMaxSize() else Modifier) {
                LocationSearch(
                  progress = progress,
                  onClickSearch = { fullHeight = true },
                  onClickClose = { fullHeight = false },
                  sheetExpanded = fullHeight,
                  destinationQuery = destinationQuery,
                  onDestinationQueryChange = onDestinationQueryChange,
                  autoCompletePlaces = autoCompletePlaces,
                  onClickPlaceAutoComplete = onClickPlaceAutoComplete,
                )
              }
            }

            is PassengerUiState.SearchingForDriver -> {
              SearchingForDriver(
                onCancelRide = {
                  fullHeight = false
                  onCancelRide()
                }
              )
            }

            is PassengerUiState.Arrive -> {

            }

            is PassengerUiState.EnRoute -> {

            }

            is PassengerUiState.Error -> {

            }

            is PassengerUiState.Loading -> {

            }

            is PassengerUiState.PassengerPickUp -> {

            }
          }
        }
      },
//      sheetPeekHeight = 120.dp,
      sheetShape = RoundedCornerShape(topEnd = corner, topStart = corner),
      modifier = Modifier.fillMaxSize(),
      scaffoldState = scaffoldState,
      sheetDragHandle = {
        if (!fullHeight || uiState !is PassengerUiState.RideInactive) {
          BottomSheetDefaults.DragHandle()
        }
      },
    ) {
      Map(onMapLoaded = onMapLoaded, locationPermissionGranted = locationPermissionGranted)
    }

    DrawerMenu(
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(16.dp),
      visible = !sheetExpanded || uiState !is PassengerUiState.RideInactive,
      onClick = onClickDrawerMenu,
    )
  }
}

@Composable
private fun Map(
  modifier: Modifier = Modifier,
  onMapLoaded: () -> Unit,
  locationPermissionGranted: Boolean,
) {
  val singapore = LatLng(1.35, 103.87)
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(singapore, 10f)
  }
  GoogleMap(
    modifier = modifier.fillMaxSize(),
    cameraPositionState = cameraPositionState,
    onMapLoaded = onMapLoaded,
    properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
  ) {
  }
}

@Composable
private fun SearchingForDriver(onCancelRide: () -> Unit) {
  Column(
    Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      Column {
        Text(text = stringResource(id = R.string.search_driver), fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text(text = stringResource(id = R.string.wait_for_driver), fontSize = 14.sp)
      }
      Button(onClick = onCancelRide) { Text(text = stringResource(id = R.string.cancel)) }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center
    ) {
      CircularProgressIndicator(modifier = Modifier.size(60.dp))
    }
  }
}

@Composable
private fun LocationSearch(
  modifier: Modifier = Modifier,
  progress: Float,
  sheetExpanded: Boolean,
  onClickSearch: () -> Unit,
  onClickClose: () -> Unit,
  destinationQuery: String,
  onDestinationQueryChange: (String) -> Unit,
  autoCompletePlaces: List<PlacesAutoComplete>,
  onClickPlaceAutoComplete: (PlacesAutoComplete) -> Unit,
) {
  LazyColumn(
    modifier = modifier
      .animateContentSize()
      .padding(horizontal = 16.dp),
  ) {
    if (!sheetExpanded) {
      searchDummy(progress = progress, onClickSearch = onClickSearch)
    } else {
      item {
        Column(
          modifier = Modifier.alpha(if (progress < 0.7f) 0f else 1f),
        ) {
          Height(height = 4.dp)
          Row(
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(onClick = onClickClose) {
              Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = null,
                modifier = Modifier.size(27.dp),
              )
            }
            Text(
              text = stringResource(id = R.string.your_route),
              fontSize = 18.sp,
              fontWeight = FontWeight.SemiBold,
            )
          }

          Height(height = 10.dp)

          OutlinedTextField(
            value = stringResource(id = R.string.current),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { CurrentDot() },
            placeholder = { Text(text = stringResource(id = R.string.search_pickup_loc)) },
            enabled = false,
          )

          Height(height = 12.dp)

          OutlinedTextField(
            value = destinationQuery,
            onValueChange = onDestinationQueryChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(imageVector = Icons.Sharp.Search, contentDescription = null) },
            placeholder = { Text(text = stringResource(id = R.string.destination)) },
          )
        }
        BackHandler { onClickClose() }
      }

      height(10.dp)
      items(autoCompletePlaces) {
        PlaceAutoCompleteListItem(address = it.address, onClick = { onClickPlaceAutoComplete(it) })
      }
    }
  }
}

@Composable
private fun CurrentDot(modifier: Modifier = Modifier) {
  val rounded = RoundedCornerShape(100)

  Box(
    modifier = modifier
      .size(21.dp)
      .clip(rounded)
      .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .size(18.dp)
        .clip(rounded)
        .background(Color.White),
      contentAlignment = Alignment.Center,
    ) {
      Box(
        modifier = Modifier
          .size(14.5.dp)
          .clip(rounded)
          .background(MaterialTheme.colorScheme.primary),
      )
    }
  }
}

@Composable
private fun PlaceAutoCompleteListItem(address: String, onClick: () -> Unit) {
  val parts = address.split(",").map { it.trim() }
  ListItem(
    headlineContent = { Text(text = parts[0]) },
    supportingContent = { Text(text = parts.subList(1, parts.size).joinToString(", ")) },
    modifier = Modifier.clickable {
      onClick()
    },
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

private fun LazyListScope.searchDummy(progress: Float, onClickSearch: () -> Unit) = item {
  AnimatedVisibility(
    visible = progress < 0.8f,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(5.dp))
        .alpha(1 - progress)
        .height(62.dp)
        .clickable { onClickSearch() }
        .background(MaterialTheme.colorScheme.surface),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .fillParentMaxHeight(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Width(width = 16.dp)
        Icon(
          imageVector = Icons.Outlined.Search,
          contentDescription = "search",
          modifier = Modifier.size(28.dp),
        )
        Width(width = 8.dp)
        Text(text = stringResource(id = R.string.where_to), fontSize = 17.sp)
      }
    }
  }
}
