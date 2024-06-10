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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import org.mtali.R
import org.mtali.core.designsystem.components.AnimatedDrawerMenu
import org.mtali.core.designsystem.components.Height
import org.mtali.core.designsystem.components.TypewriterText
import org.mtali.core.designsystem.components.Width
import org.mtali.core.designsystem.components.height
import org.mtali.core.models.PlacesAutoComplete
import org.mtali.core.utils.animateToBounds
import org.mtali.core.utils.handleToast

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
    uiState = uiState,
  )
}

@Composable
private fun PassengerScreen(
  onMapLoaded: () -> Unit,
  locationPermissionGranted: Boolean,
  destinationQuery: String,
  onDestinationQueryChange: (String) -> Unit,
  autoCompletePlaces: List<PlacesAutoComplete>,
  onClickPlaceAutoComplete: (PlacesAutoComplete) -> Unit,
  onClickDrawerMenu: () -> Unit,
  uiState: PassengerUiState,
  onCancelRide: () -> Unit,
) {
  Scaffold {
    Column(
      modifier = Modifier
        .padding(it)
        .fillMaxSize(),
    ) {
      val cameraPosition = rememberCameraPositionState()
      var expandSearch by remember { mutableStateOf(false) }

      Box(modifier = Modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
          GoogleMap(
            modifier = Modifier.weight(1f),
            onMapLoaded = onMapLoaded,
            properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
            cameraPositionState = cameraPosition,
          ) {
            when (uiState) {
              is PassengerUiState.SearchingForDriver -> {
                MapSearchingForDriver(uiState, cameraPosition)
              }

              is PassengerUiState.PassengerPickUp -> {
                MapPassengerPickUp(uiState, cameraPosition)
              }

              is PassengerUiState.EnRoute -> {
                MapPassengerEnRoute(uiState, cameraPosition)
              }

              else -> Unit
            }
          }

          Column(
            modifier = Modifier
              .padding(16.dp)
              .heightIn(min = 100.dp)
              .animateContentSize(),
          ) {
            when (uiState) {
              PassengerUiState.RideInactive -> {
                SearchDestinationCard(
                  destinationQuery = destinationQuery,
                  onDestinationQueryChange = onDestinationQueryChange,
                  autoCompletePlaces = autoCompletePlaces,
                  onClickPlaceAutoComplete = onClickPlaceAutoComplete,
                  expandSearch = expandSearch,
                  onExpandSearchChange = { expand -> expandSearch = expand },
                )
              }

              is PassengerUiState.SearchingForDriver -> {
                LaunchedEffect(Unit) { expandSearch = false }
                SearchNearbyDriver(uiState, onCancelRide)
              }

              is PassengerUiState.PassengerPickUp -> {
                PassengerPickUpCard(uiState, onCancelRide)
              }

              is PassengerUiState.EnRoute -> {
                PassengerEnRoute(uiState)
              }

              is PassengerUiState.Loading -> {
                LoadingCard()
              }

              is PassengerUiState.Arrive -> {
                PassengerArriveCard()
              }

              is PassengerUiState.Error -> {
                ErrorCard()
              }
            }
          }
        }
        AnimatedDrawerMenu(
          modifier = Modifier.padding(16.dp),
          visible = !expandSearch,
          onClick = onClickDrawerMenu,
        )
      }
    }
  }
}

@Composable
fun MapPassengerEnRoute(
  uiState: PassengerUiState.EnRoute,
  cameraPosition: CameraPositionState,
) {
  val route = uiState.destinationRoute
  if (route != null) {
    Polyline(
      points = PolyUtil.decode(route.overviewPolyline.encodedPath),
      clickable = false,
      color = MaterialTheme.colorScheme.primary,
    )
  }

  MarkerComposable(
    state = MarkerState(position = LatLng(uiState.driverLat, uiState.driverLng)),
    title = "Driver",
  ) {
    Image(
      painter = painterResource(id = R.drawable.ic_car_marker),
      modifier = Modifier.size(30.dp),
      contentDescription = null,
    )
  }

  Marker(
    state = MarkerState(position = LatLng(uiState.destinationLat, uiState.destinationLng)),
    title = uiState.destinationAddress,
  )

  LaunchedEffect(Unit) {
    uiState.apply {
      cameraPosition.animateToBounds(driverLat, driverLng, destinationLat, destinationLng)
    }
  }
}

@Composable
private fun MapPassengerPickUp(
  uiState: PassengerUiState.PassengerPickUp,
  cameraPositionState: CameraPositionState,
) {
  val route = uiState.driverRoute
  if (route != null) {
    Polyline(
      points = PolyUtil.decode(route.overviewPolyline.encodedPath),
      clickable = false,
      color = MaterialTheme.colorScheme.primary,
    )
  }
  MarkerComposable(
    state = MarkerState(position = LatLng(uiState.driverLat, uiState.driverLng)),
    title = stringResource(id = R.string.driver),
  ) {
    Image(
      painter = painterResource(id = R.drawable.ic_car_marker),
      modifier = Modifier.size(35.dp),
      contentDescription = null,
    )
  }

  Marker(
    state = MarkerState(position = LatLng(uiState.passengerLat, uiState.passengerLng)),
    title = stringResource(id = R.string.you),
  )

  LaunchedEffect(Unit) {
    uiState.apply {
      cameraPositionState.animateToBounds(driverLat, driverLng, passengerLat, passengerLng)
    }
  }
}

@Composable
private fun MapSearchingForDriver(
  uiState: PassengerUiState.SearchingForDriver,
  cameraPositionState: CameraPositionState,
) {
  Marker(
    state = MarkerState(position = LatLng(uiState.passengerLat, uiState.passengerLng)),
    title = stringResource(id = R.string.you),
  )

  LaunchedEffect(Unit) {
    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(uiState.passengerLat, uiState.passengerLng), 14f))
  }
}

@Composable
private fun ErrorCard() {
  Text(text = "Ops ... error")
}

@Composable
private fun PassengerArriveCard() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = Icons.Outlined.DoneAll,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(55.dp),
    )
    Text(
      text = stringResource(id = R.string.ride_completed),
      fontSize = 18.sp,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Composable
private fun LoadingCard() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    CircularProgressIndicator(modifier = Modifier.size(70.dp))
  }
}

@Composable
private fun PassengerEnRoute(uiState: PassengerUiState.EnRoute) {
  val route = uiState.destinationRoute
  val leg = route?.legs?.first()

  Column(modifier = Modifier.fillMaxWidth()) {
    Row {
      Column(modifier = Modifier.weight(1f)) {
        Text(text = stringResource(id = R.string.destination_location), fontSize = 18.sp, fontWeight = FontWeight.Medium)
        if (leg == null) {
          Text(text = stringResource(id = R.string.unable_to_retrieve_address))
        } else {
          Text(text = leg.endAddress)
        }
      }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Outlined.DirectionsCar,
        contentDescription = null,
        modifier = Modifier
          .size(40.dp)
          .padding(end = 8.dp),
      )
      Column {
        Text(text = uiState.driverName)
        Text(
          text = buildString {
            append(stringResource(id = R.string.destination_is))
            append(" ")
            append(leg?.distance?.humanReadable ?: "? km")
            append(" ")
            append(stringResource(id = R.string.away))
          },
        )
      }
    }
  }
}

@Composable
private fun PassengerPickUpCard(
  uiState: PassengerUiState.PassengerPickUp,
  onCancelRide: () -> Unit,
) {
  val route = uiState.driverRoute
  val leg = route?.legs?.first()

  Column(modifier = Modifier.fillMaxWidth()) {
    Row {
      Column(modifier = Modifier.weight(1f)) {
        Text(text = stringResource(id = R.string.destination_location), fontSize = 18.sp, fontWeight = FontWeight.Medium)
        if (leg == null) {
          Text(text = stringResource(id = R.string.unable_to_retrieve_address))
        } else {
          Text(text = leg.endAddress)
        }
      }
      IconButton(onClick = onCancelRide) {
        Icon(imageVector = Icons.Outlined.Close, contentDescription = "cancel")
      }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Outlined.DirectionsCar,
        contentDescription = null,
        modifier = Modifier
          .size(40.dp)
          .padding(end = 8.dp),
      )
      Column {
        Text(text = uiState.driverName)
        Text(
          text = buildString {
            append(stringResource(id = R.string.driver_is))
            append(" ")
            append(leg?.distance?.humanReadable ?: "? km")
            append(" ")
            append(stringResource(id = R.string.away))
          },
        )
      }
    }
  }
}

@Composable
private fun SearchDestinationCard(
  expandSearch: Boolean,
  destinationQuery: String,
  onDestinationQueryChange: (String) -> Unit,
  autoCompletePlaces: List<PlacesAutoComplete>,
  onClickPlaceAutoComplete: (PlacesAutoComplete) -> Unit,
  onExpandSearchChange: (Boolean) -> Unit,
) {
  if (!expandSearch) {
    SearchPlaceholder(onClickSearch = { onExpandSearchChange(true) })
  } else {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
    ) {
      item {
        Height(height = 4.dp)
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = { onExpandSearchChange(false) }) {
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

        BackHandler {
          onExpandSearchChange(false)
        }
      }

      height(10.dp)

      items(autoCompletePlaces) {
        PlaceAutoCompleteListItem(address = it.address, onClick = { onClickPlaceAutoComplete(it) })
      }
    }
  }
}

@Composable
private fun SearchNearbyDriver(
  uiState: PassengerUiState.SearchingForDriver,
  onCancelRide: () -> Unit,
) {
  Column {
    Row(modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
      ) {
        Text(text = stringResource(id = R.string.destination_location), fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Text(text = uiState.destinationAddress)
      }
      IconButton(onClick = onCancelRide) {
        Icon(imageVector = Icons.Outlined.Close, contentDescription = "cancel")
      }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      CircularProgressIndicator(
        modifier = Modifier
          .size(95.dp)
          .padding(10.dp),
        strokeWidth = 3.dp,
      )

      Text(
        text = stringResource(id = R.string.search_near_driver),
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
      )
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

@Composable
private fun SearchPlaceholder(onClickSearch: () -> Unit) {
  Column {
    TypewriterText(texts = listOf(stringResource(id = R.string.slogan)), fontSize = 16.sp, loop = false)
    Height(height = 7.dp)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .height(62.dp)
        .clickable { onClickSearch() }
        .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(),
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
