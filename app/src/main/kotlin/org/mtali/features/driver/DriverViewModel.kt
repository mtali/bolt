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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.model.DirectionsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mtali.core.data.repositories.DeviceRepository
import org.mtali.core.data.repositories.GoogleRepository
import org.mtali.core.data.repositories.RideRepository
import org.mtali.core.domain.GetUserUseCase
import org.mtali.core.domain.LogoutUseCase
import org.mtali.core.models.BoltUser
import org.mtali.core.models.Ride
import org.mtali.core.models.RideStatus
import org.mtali.core.models.ServiceResult
import org.mtali.core.models.ToastMessage
import org.mtali.core.utils.combineTuple
import org.mtali.core.utils.isRunning
import timber.log.Timber
import javax.inject.Inject

private const val tag = "wakanda:DriverViewModel"
private const val IMPOSSIBLE_LAT_LON = 999.0

@HiltViewModel
class DriverViewModel @Inject constructor(
  private val getUserUseCase: GetUserUseCase,
  private val logoutUseCase: LogoutUseCase,
  private val rideRepository: RideRepository,
  private val googleRepository: GoogleRepository,
  deviceRepository: DeviceRepository,
) : ViewModel() {

  var toastHandler: ((ToastMessage) -> Unit)? = null

  private val _driver = MutableStateFlow<BoltUser?>(null)
  private val _ride = rideRepository.rideFlow()
  private val _mapIsReady = MutableStateFlow(false)

  private val _driverLocation = deviceRepository.devicePrefs.map { it.deviceLocation }.distinctUntilChanged()
  private val _passengersSearching = rideRepository.openRides()

  private var refreshPassengersJob: Job? = null

  private var rideSelectedJob: Job? = null

  val uiState = combineTuple(
    _ride,
    _driver,
    _mapIsReady,
  ).map { (rideResult, driver, mapIsReady) ->

    if (rideResult is ServiceResult.Failure) return@map DriverUiState.Error

    val ride = (rideResult as ServiceResult.Value).value

    if (driver == null || !mapIsReady) {
      return@map DriverUiState.Loading
    } else {
      when {
        // Not active on a ride
        ride == null -> DriverUiState.SearchingForPassengers
        ride.status == RideStatus.PASSENGER_PICK_UP.value &&
          ride.driverLatitude != null &&
          ride.driverLongitude != null -> {
          val directionsRoute = getDirectionsRoute(ride)
          DriverUiState.PassengerPickUp(
            passengerLat = ride.passengerLatitude,
            passengerLng = ride.passengerLongitude,
            driverLat = ride.driverLatitude,
            driverLng = ride.driverLongitude,
            destinationLat = ride.destinationLatitude,
            destinationLng = ride.destinationLongitude,
            destinationAddress = ride.destinationAddress,
            passengerName = ride.passengerName,
            totalMessages = ride.totalMessages,
            directionsRoute = directionsRoute
          )
        }

        ride.status == RideStatus.EN_ROUTE.value &&
          ride.driverLatitude != null &&
          ride.driverLongitude != null -> {
          DriverUiState.EnRoute(
            driverLat = ride.driverLatitude,
            driverLng = ride.driverLongitude,
            destinationLat = ride.destinationLatitude,
            destinationLng = ride.destinationLongitude,
            destinationAddress = ride.destinationAddress,
            passengerName = ride.passengerName,
            totalMessages = ride.totalMessages,
          )
        }

        ride.status == RideStatus.ARRIVED.value &&
          ride.driverLatitude != null &&
          ride.driverLongitude != null -> {
          DriverUiState.Arrive(
            driverLat = ride.driverLatitude,
            driverLng = ride.driverLongitude,
            destinationLat = ride.destinationLatitude,
            destinationLng = ride.destinationLongitude,
            destinationAddress = ride.destinationAddress,
            passengerName = ride.passengerName,
            totalMessages = ride.totalMessages,
          )
        }

        else -> {
          Timber.e("For fu*k sake: DriverUiState.Error $driver, .......... Ride $ride")
          DriverUiState.Error
        }
      }
    }
  }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = DriverUiState.Loading,
    )

  val locationAwarePassengers = combine(
    _driverLocation,
    _passengersSearching,
  ) { driverLocation, passengersResult ->
    if (driverLocation == null || (driverLocation.lat == IMPOSSIBLE_LAT_LON || driverLocation.lng == IMPOSSIBLE_LAT_LON)) { // Possible bug: we might get cached location in prefs
      emptyList()
    } else {
      when (passengersResult) {
        is ServiceResult.Failure -> {
          if (uiState.value is DriverUiState.SearchingForPassengers) logoutDriver()
          emptyList()
        }

        is ServiceResult.Value -> {
          passengersResult.value.map { ride -> Pair(ride, LatLng(driverLocation.lat, driverLocation.lng)) }
        }
      }
    }
  }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptyList(),
    )

  init {
    getDriver()
  }

  private fun getDriver() = viewModelScope.launch {
    when (val driverResult = getUserUseCase()) {
      is ServiceResult.Failure -> {
        toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
        logoutDriver()
      }

      is ServiceResult.Value -> {
        if (driverResult.value == null) {
          logoutDriver()
        } else {
          getActiveRideIfExist(driverResult.value)
        }
      }
    }
  }

  private suspend fun getDirectionsRoute(ride: Ride): DirectionsRoute? {
    val directions = googleRepository.getDirectionsRoute(
      originLat = ride.driverLatitude!!,
      originLng = ride.driverLongitude!!,
      destLat = ride.destinationLatitude,
      destLng = ride.destinationLongitude,
    )
    return when (directions) {
      is ServiceResult.Failure -> {
        toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
        null
      }

      is ServiceResult.Value -> directions.value
    }
  }

  private suspend fun getActiveRideIfExist(driver: BoltUser) {
    when (val activeRideResult = rideRepository.getRideIfInProgress()) {
      is ServiceResult.Failure -> {
        toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
        logoutDriver()
      }

      is ServiceResult.Value -> {
        if (activeRideResult.value == null) {
          _driver.update { driver }
          getPassengerList()
        } else {
          observeRide(activeRideResult.value, driver)
        }
      }
    }
  }

  private suspend fun observeRide(rideId: String, driver: BoltUser) {
    // Get result with _ride
    rideRepository.observeRideById(rideId)
    _driver.update { driver }
  }

  private suspend fun getPassengerList() {
    rideRepository.observeOpenRides()
  }

  private fun logoutDriver() {
    Timber.tag(tag).d("forced logout")
    viewModelScope.launch { logoutUseCase() }
  }

  fun onMapLoaded() {
    _mapIsReady.update { true }
  }

  fun onRefreshPassengers() {
    if (refreshPassengersJob.isRunning()) return
    refreshPassengersJob = viewModelScope.launch {
      getPassengerList()
    }
  }

  fun onRideSelected(selectedRide: Ride) {
    if (rideSelectedJob.isRunning()) return
    rideSelectedJob = viewModelScope.launch {
      val driverLocation = _driverLocation.first()

      if (driverLocation == null) {
        toastHandler?.invoke(ToastMessage.UNABLE_TO_RETRIEVE_COORDINATES)
      } else {
        val result = rideRepository.connectDriverToRide(
          driver = _driver.value!!,
          ride = selectedRide.copy(
            driverLatitude = driverLocation.lat,
            driverLongitude = driverLocation.lng,
          ),
        )

        when (result) {
          is ServiceResult.Failure -> toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
          is ServiceResult.Value -> {
            rideRepository.observeRideById(result.value)
          }
        }
      }
    }
  }
}
