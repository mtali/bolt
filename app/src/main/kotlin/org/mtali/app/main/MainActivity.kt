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
package org.mtali.app.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.location.LocationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.mtali.R
import org.mtali.app.ui.BoltApp
import org.mtali.app.ui.rememberBoltAppState
import org.mtali.core.designsystem.BoltTheme
import org.mtali.core.utils.areLocationPermissionGranted
import org.mtali.core.utils.handleToast
import timber.log.Timber

private const val LOCATION_REQUEST_INTERVAL = 10000L

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val viewModel by viewModels<MainViewModel>()
  private var locationRequest: LocationRequest? = null
  private lateinit var fusedLocationClient: FusedLocationProviderClient

  override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    viewModel.toastHandle = { handleToast(it) }

    var uiState: MainUiState by mutableStateOf(MainUiState.Loading)
    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState
          .onEach { uiState = it }
          .collect()
      }
    }

    splashScreen.setKeepOnScreenCondition { uiState !is MainUiState.Success }

    setContent {
      val appState = rememberBoltAppState()
      BoltTheme {
        BoltApp(
          appState = appState,
          uiState = uiState,
          onLogout = viewModel::onLogout,
          shouldShowRequestPermissionRationale = {
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
          },
          onLocationPermissionGranted = { requestLocation() },
          onToggleUserType = viewModel::onToggleUserType,
        )
      }
    }
  }

  /**
   * Get device last location
   */
  @SuppressLint("MissingPermission")
  private fun startRequestingLocationUpdates() {
    if (!areLocationPermissionGranted()) return

    fusedLocationClient.lastLocation
      .addOnCompleteListener { request ->
        if (request.isSuccessful && request.result != null) {
          val location = request.result
          val lat = location.latitude
          val lng = location.longitude
          viewModel.updateDeviceLocation(LatLng(lat, lng))
        } else {
          Timber.e(request.exception)
          Toast.makeText(
            this,
            R.string.unable_to_retrieve_coordinates_user,
            Toast.LENGTH_LONG,
          ).show()
        }
      }
  }

  /**
   * This function manages the overall process of requesting location updates,
   * including ensuring the necessary permissions and system settings are in place.
   */
  private fun requestLocation() {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (LocationManagerCompat.isLocationEnabled(locationManager)) {
      if (!areLocationPermissionGranted()) {
        // TODO: Request permission
      } else {
        locationRequest = LocationRequest.Builder(
          Priority.PRIORITY_HIGH_ACCURACY, LOCATION_REQUEST_INTERVAL,
        ).apply {
          setMinUpdateDistanceMeters(0f)
        }.build()

        // Check for proper device config
        val locationSettingRequest = LocationSettingsRequest.Builder().apply {
          addLocationRequest(locationRequest!!)
        }.build()
        LocationServices.getSettingsClient(this)
          .checkLocationSettings(locationSettingRequest)
          .addOnCompleteListener { task ->
            if (task.isSuccessful) {
              startRequestingLocationUpdates()
            } else {
              Toast.makeText(
                this,
                R.string.system_settings_are_preventing_location_updates,
                Toast.LENGTH_LONG,
              ).show()
            }
          }
      }
    } else {
      Toast.makeText(
        this,
        R.string.location_must_be_enabled,
        Toast.LENGTH_LONG,
      ).show()
    }
  }
}
