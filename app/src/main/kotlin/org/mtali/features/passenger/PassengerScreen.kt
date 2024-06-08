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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import org.mtali.R
import org.mtali.core.designsystem.components.AnimatedDrawerMenu
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
  LaunchedEffect(uiState) {
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
                  onExpandSearchChange = { expandSearch = it },
                )
              }

              is PassengerUiState.SearchingForDriver -> {
                SearchNearbyDriver()
              }

              else -> Unit
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
private fun SearchNearbyDriver() {
  Text(text = "Search near")
}

@Composable
private fun SearchingForDriver(onCancelRide: () -> Unit) {
  Column(
    Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top,
    ) {
      Column {
        Text(text = stringResource(id = R.string.search_driver), fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text(text = stringResource(id = R.string.wait_for_driver), fontSize = 14.sp)
      }
      Button(onClick = onCancelRide) { Text(text = stringResource(id = R.string.cancel)) }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
    ) {
      CircularProgressIndicator(modifier = Modifier.size(60.dp))
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
    Text(text = stringResource(id = R.string.slogan), fontSize = 16.sp)

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
