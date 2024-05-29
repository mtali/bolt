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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.mtali.R
import org.mtali.core.designsystem.components.Width

@Composable
fun PassengerRoute(onLogout: () -> Unit) {
  PassengerScreen(onLogout = onLogout)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PassengerScreen(onLogout: () -> Unit) {
  BottomSheetScaffold(
    sheetContent = {
      LocationSearch()
    },
    sheetPeekHeight = 120.dp,
    sheetShape = RoundedCornerShape(topEnd = 10.dp, topStart = 10.dp),
  ) {
    Map()
  }
}

@Composable
private fun Map(modifier: Modifier = Modifier, onMapLoaded: () -> Unit = {}) {
  val singapore = LatLng(1.35, 103.87)
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(singapore, 10f)
  }
  GoogleMap(
    modifier = modifier.fillMaxSize(),
    cameraPositionState = cameraPositionState,
    onMapLoaded = onMapLoaded,
  ) {
    Marker(
      state = MarkerState(position = singapore),
      title = "Singapore",
      snippet = "Marker in Singapore",
    )
  }
}

@Composable
private fun LocationSearch(modifier: Modifier = Modifier) {
  LazyColumn(
    modifier = modifier
      .fillMaxHeight()
      .padding(horizontal = 16.dp),
  ) {
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(5.dp))
          .height(62.dp)
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
}
