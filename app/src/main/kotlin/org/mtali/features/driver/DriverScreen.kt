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

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import org.mtali.core.models.Ride
import org.mtali.core.utils.handleToast
import timber.log.Timber

@Composable
fun DriverRoute(viewModel: DriverViewModel = hiltViewModel(), locationPermissionGranted: Boolean, onClickDrawerMenu: () -> Unit) {
  val context = LocalContext.current

  viewModel.toastHandler = { context.handleToast(it) }

  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  val passengers by viewModel.locationAwarePassengers.collectAsStateWithLifecycle(initialValue = emptyList())

  LaunchedEffect(uiState) {
    Timber.tag("wakanda:DriverRoute").d("$uiState")
  }

  DriverScreen(
    uiState = uiState,
    passengers = passengers,
    onMapLoaded = viewModel::onMapLoaded,
    onClickDrawerMenu = onClickDrawerMenu,
    locationPermissionGranted = locationPermissionGranted,
    onRefreshPassengers = viewModel::onRefreshPassengers,
    onRideSelected = viewModel::onRideSelected,
    onCancelRide = viewModel::onCancelRide,
    onPickupPassenger = viewModel::advanceRide,
    onArriveToDestination = viewModel::advanceRide,
    onRideCompleted = viewModel::onCancelRide,
  )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
private fun DriverScreen(
  uiState: DriverUiState,
  passengers: List<Pair<Ride, LatLng>>,
  onMapLoaded: () -> Unit,
  onClickDrawerMenu: () -> Unit,
  locationPermissionGranted: Boolean,
  onRefreshPassengers: () -> Unit,
  onRideSelected: (Ride) -> Unit,
  onCancelRide: () -> Unit,
  onPickupPassenger: () -> Unit,
  onArriveToDestination: () -> Unit,
  onRideCompleted: () -> Unit,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(id = R.string.dashboard)) },
        navigationIcon = {
          IconButton(onClick = onClickDrawerMenu) {
            Icon(imageVector = Icons.Outlined.Menu, contentDescription = null)
          }
        },
      )
    },
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      val cameraPosition = rememberCameraPositionState()

      GoogleMap(
        modifier = Modifier.weight(1f),
        onMapLoaded = onMapLoaded,
        properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
        cameraPositionState = cameraPosition,
      ) {
        when (uiState) {
          is DriverUiState.PassengerPickUp -> {
            MapPassengerPickup(uiState, cameraPosition)
          }

          is DriverUiState.EnRoute -> {
            MapEnRoute(uiState, cameraPosition)
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
          is DriverUiState.Arrive -> {
            ArriveCard(onRideCompleted = onRideCompleted)
          }

          is DriverUiState.EnRoute -> {
            EnRouteCard(uiState = uiState, onArriveToDestination = onArriveToDestination)
          }

          is DriverUiState.Error -> {
            ErrorCard()
          }

          is DriverUiState.Loading -> {
            LoadingCard()
          }

          is DriverUiState.PassengerPickUp -> {
            PassengerPickUpCard(
              uiState = uiState,
              onCancelRide = onCancelRide,
              onPickupPassenger = onPickupPassenger,
            )
          }

          is DriverUiState.SearchingForPassengers -> {
            SearchingForPassengersCard(
              passengers = passengers,
              onRefreshPassengers = onRefreshPassengers,
              onRideSelected = onRideSelected,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MapEnRoute(
  uiState: DriverUiState.EnRoute,
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
    title = stringResource(id = R.string.driver),
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
    cameraPosition.animate(
      CameraUpdateFactory.newLatLngZoom(LatLng(uiState.driverLat, uiState.driverLng), 15f),
    )
  }
}

@Composable
private fun ArriveCard(onRideCompleted: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onRideCompleted() }
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
    Text(
      text = stringResource(id = R.string.hold_icon_down),
      fontSize = 18.sp,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Composable
private fun EnRouteCard(
  uiState: DriverUiState.EnRoute,
  onArriveToDestination: () -> Unit,
) {
  val route = uiState.destinationRoute
  val leg = route?.legs?.first()

  Column {
    Column(modifier = Modifier.fillMaxWidth()) {
      Text(text = stringResource(id = R.string.destination_location), fontSize = 18.sp, fontWeight = FontWeight.Medium)
      if (leg == null) {
        Text(text = stringResource(id = R.string.unable_to_retrieve_address))
      } else {
        Text(text = leg.endAddress)
      }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Outlined.AccountCircle,
        contentDescription = null,
        modifier = Modifier
          .size(40.dp)
          .padding(end = 8.dp),
      )
      Column {
        Text(text = uiState.passengerName)
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

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onArriveToDestination() }
        .padding(vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = Icons.Outlined.Download,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(55.dp),
      )
      Text(
        text = stringResource(id = R.string.arrive_to_destination),
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
      )
      Text(
        text = stringResource(id = R.string.hold_icon_down),
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
      )
    }
  }
}

@Composable
private fun ErrorCard() {
  Text(text = "Ops ... error")
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
private fun MapPassengerPickup(
  uiState: DriverUiState.PassengerPickUp,
  cameraPosition: CameraPositionState,
) {
  val route = uiState.passengerRoute
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
    state = MarkerState(position = LatLng(uiState.passengerLat, uiState.passengerLng)),
    title = stringResource(id = R.string.passenger),
  )

  LaunchedEffect(Unit) {
    cameraPosition.animate(
      CameraUpdateFactory.newLatLngZoom(LatLng(uiState.driverLat, uiState.driverLng), 15f),
    )
  }
}

@Composable
private fun PassengerPickUpCard(
  uiState: DriverUiState.PassengerPickUp,
  onCancelRide: () -> Unit,
  onPickupPassenger: () -> Unit,
) {
  val route = uiState.passengerRoute
  val leg = route?.legs?.first()

  Column {
    Row(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.weight(1f)) {
        Text(text = stringResource(id = R.string.passenger_location), fontSize = 18.sp, fontWeight = FontWeight.Medium)
        if (leg == null) {
          Text(text = stringResource(id = R.string.unable_to_retrieve_address))
        } else {
          Text(text = leg.endAddress)
        }
      }

      IconButton(onClick = onCancelRide) {
        Icon(
          imageVector = Icons.Outlined.Close,
          contentDescription = "cancel",
          modifier = Modifier.padding(start = 8.dp),
        )
      }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Outlined.AccountCircle,
        contentDescription = null,
        modifier = Modifier
          .size(40.dp)
          .padding(end = 8.dp),
      )
      Column {
        Text(text = uiState.passengerName)
        Text(
          text = buildString {
            append(stringResource(id = R.string.passenger_is))
            append(" ")
            append(leg?.distance?.humanReadable ?: "? km")
            append(" ")
            append(stringResource(id = R.string.away))
          },
        )
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onPickupPassenger() }
        .padding(vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = Icons.Outlined.PersonAdd,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(55.dp),
      )
      Text(
        text = stringResource(id = R.string.pickup_passenger),
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
      )
      Text(
        text = stringResource(id = R.string.hold_icon_down),
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
      )
    }
  }
}

@Composable
private fun SearchingForPassengersCard(
  passengers: List<Pair<Ride, LatLng>>,
  onRefreshPassengers: () -> Unit,
  onRideSelected: (Ride) -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(text = stringResource(id = R.string.passengers_requests), fontSize = 18.sp)
      Button(onClick = onRefreshPassengers) {
        Text(text = stringResource(id = R.string.refresh))
      }
    }
    if (passengers.isEmpty()) {
      Text(text = stringResource(id = R.string.no_passengers))
    } else {
      HorizontalDivider()
      PassengersList(passengers, onRideSelected)
    }
  }
}

@Composable
private fun PassengersList(passengers: List<Pair<Ride, LatLng>>, onRideSelected: (Ride) -> Unit) {
  LazyColumn {
    items(passengers) { item ->
      ListItem(
        modifier = Modifier.clickable { onRideSelected(item.first) },
        headlineContent = { Text(text = item.first.passengerName) },
        supportingContent = { Text(text = "Going to: ${item.first.destinationAddress}") },
      )
    }
  }
}
