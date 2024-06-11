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

import android.Manifest
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.TimeUnit

/**
 * An effect that request location updates based on the provided request and ensures that the
 * updates are added and removed whenever the composable enters or exists the composition.
 */
@Composable
@RequiresPermission(
  allOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
fun LocationUpdatesEffect(
  usePreciseLocation: Boolean,
  locationRequest: LocationRequest = rememberLocationRequest(usePreciseLocation),
  lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
  onUpdate: (result: LocationResult) -> Unit,
) {
  val context = LocalContext.current
  val currentOnUpdate by rememberUpdatedState(newValue = onUpdate)
  DisposableEffect(locationRequest, locationRequest) {
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationCallback: LocationCallback = object : LocationCallback() {
      override fun onLocationResult(result: LocationResult) {
        currentOnUpdate(result)
      }
    }
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_START) {
        locationClient.requestLocationUpdates(
          locationRequest,
          locationCallback,
          Looper.getMainLooper(),
        )
      } else if (event == Lifecycle.Event.ON_STOP) {
        locationClient.removeLocationUpdates(locationCallback)
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose {
      locationClient.removeLocationUpdates(locationCallback)
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }
}

@Composable
private fun rememberLocationRequest(usePreciseLocation: Boolean) = remember(usePreciseLocation) {
  val priority = if (usePreciseLocation) {
    Priority.PRIORITY_HIGH_ACCURACY
  } else {
    Priority.PRIORITY_BALANCED_POWER_ACCURACY
  }
  LocationRequest.Builder(priority, TimeUnit.SECONDS.toMillis(3)).build()
}
