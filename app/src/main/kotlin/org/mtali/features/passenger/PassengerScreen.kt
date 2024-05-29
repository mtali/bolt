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

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun PassengerRoute(onLogout: () -> Unit) {
  PassengerScreen(onLogout = onLogout)
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
private fun PassengerScreen(onLogout: () -> Unit) {
  var showInput by remember { mutableStateOf(false) }
  Scaffold { _ ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(0.dp),
    ) {
      Map(
        onMapLoaded = {
          showInput = true
        },
      )
    }
  }
}

@Composable
private fun Map(modifier: Modifier = Modifier, onMapLoaded: () -> Unit) {
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
