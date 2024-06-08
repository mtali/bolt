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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.maps.model.DirectionsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
import org.mtali.core.models.CreateRide
import org.mtali.core.models.Location
import org.mtali.core.models.PlacesAutoComplete
import org.mtali.core.models.Ride
import org.mtali.core.models.RideStatus
import org.mtali.core.models.ServiceResult
import org.mtali.core.models.ToastMessage
import org.mtali.core.utils.combineTuple
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PassengerViewModel @Inject constructor(
  deviceRepository: DeviceRepository,
  private val googleRepository: GoogleRepository,
  private val rideRepository: RideRepository,
  private val getUserUseCase: GetUserUseCase,
  private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

  var toastHandler: ((ToastMessage) -> Unit)? = null

  private val _mapIsReady = MutableStateFlow(false)
  private val _passenger = MutableStateFlow<BoltUser?>(null)
  private var _rideResult: Flow<ServiceResult<Ride?>> = rideRepository.rideFlow()

  private val _autoCompletePlaces = MutableStateFlow<List<PlacesAutoComplete>>(emptyList())
  val autoCompletePlaces: StateFlow<List<PlacesAutoComplete>> = _autoCompletePlaces

  private val _destinationQuery = MutableStateFlow("")
  val destinationQuery: StateFlow<String> = _destinationQuery

  private val devicePrefs = deviceRepository.devicePrefs

  /**
   * Conditions
   * a. User/Passenger can never be null
   * b. Ride may be null
   * c. Rode may not be null and have the following states
   *  - SEARCHING_FOR_DRIVER
   *  - PASSENGER_PICK_UP
   *  - EN_ROUTE
   *  - ARRIVED
   */
  val uiState = combineTuple(
    _passenger,
    _rideResult,
    _mapIsReady,
  ).map { (passenger, rideResult, mapIsReady) ->

    if (rideResult is ServiceResult.Failure) return@map PassengerUiState.Error

    val ride = (rideResult as ServiceResult.Value).value

    // only publish updates when map is loaded
    if (passenger == null || !mapIsReady) {
      PassengerUiState.Loading
    } else {
      when {
        // No ride
        ride == null -> {
          PassengerUiState.RideInactive
        }

        // Ride no driver
        ride.driverId == null -> {
          PassengerUiState.SearchingForDriver(
            passengerLat = ride.passengerLatitude,
            passengerLng = ride.passengerLongitude,
            destinationAddress = ride.destinationAddress,
          )
        }

        // Ride (pickup process) and driver
        ride.status == RideStatus.PASSENGER_PICK_UP.value &&
          ride.driverLatitude != null &&
          ride.driverLongitude != null -> {
          PassengerUiState.PassengerPickUp(
            passengerLat = ride.passengerLatitude,
            passengerLng = ride.passengerLongitude,
            driverLat = ride.driverLatitude,
            driverLng = ride.driverLongitude,
            destinationLat = ride.destinationLatitude,
            destinationLng = ride.destinationLongitude,
            destinationAddress = ride.destinationAddress,
            driverName = ride.driverName ?: "Error",
            totalMessages = ride.totalMessages,
            driverRoute = ride.routeToDriver(),
          )
        }

        // Ride (en route) and driver
        ride.status == RideStatus.EN_ROUTE.value &&
          ride.driverLatitude != null &&
          ride.driverLongitude != null -> {
          PassengerUiState.EnRoute(
            passengerLat = ride.passengerLatitude,
            passengerLng = ride.passengerLongitude,
            driverLat = ride.driverLatitude,
            driverLng = ride.driverLongitude,
            destinationLat = ride.destinationLatitude,
            destinationLng = ride.destinationLongitude,
            destinationAddress = ride.destinationAddress,
            driverName = ride.driverName ?: "Error",
            totalMessages = ride.totalMessages,
            destinationRoute = ride.routeToDestination(),
          )
        }

        // Ride (arrived) and driver
        ride.status == RideStatus.ARRIVED.value &&
          ride.driverLatitude != null &&
          ride.driverLongitude != null -> {
          PassengerUiState.Arrive(
            passengerLat = ride.passengerLatitude,
            passengerLng = ride.passengerLongitude,
            destinationLat = ride.destinationLatitude,
            destinationLng = ride.destinationLongitude,
            driverName = ride.driverName ?: "Error",
            totalMessages = ride.totalMessages,
          )
        }

        // I will be dammed
        else -> {
          Timber.e("For fu*k sake: PassengerUiState.Error $passenger, $ride")
          PassengerUiState.Error
        }
      }
    }
  }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = PassengerUiState.Loading,
    )

  init {
    getPassenger()
  }

  fun onMapLoaded() {
    _mapIsReady.update { true }
  }

  private fun requestPlacesAutocomplete() {
    val query = _destinationQuery.value
    if (query.length < 3) return

    viewModelScope.launch {
      when (val result = googleRepository.getPlacesAutocomplete(query)) {
        is ServiceResult.Failure -> toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
        is ServiceResult.Value -> {
          _autoCompletePlaces.update {
            result.value.map { prediction -> prediction.toPlacesAutoComplete() }
          }
        }
      }
    }
  }

  private fun getPassenger() = viewModelScope.launch {
    when (val getUserResult = getUserUseCase()) {
      is ServiceResult.Failure -> {
        toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
        logoutUser()
      }

      is ServiceResult.Value -> {
        if (getUserResult.value == null) {
          logoutUser()
        } else {
          getActiveRideIfItExists(getUserResult.value)
        }
      }
    }
  }

  /**
   * Will be called on initial launch of this view model
   * Be sure to store value of user[BoltUser]
   */
  private suspend fun getActiveRideIfItExists(user: BoltUser) {
    when (val ride = rideRepository.getRideIfInProgress()) {
      is ServiceResult.Failure -> {
        toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
        logoutUser()
      }

      is ServiceResult.Value -> {
        if (ride.value == null) {
          _passenger.update { user }
        } else {
          observeRide(ride.value, user)
        }
      }
    }
  }

  fun onDestinationQueryChange(query: String) {
    _destinationQuery.update { query }
    requestPlacesAutocomplete()
  }

  fun onClickPlaceAutoComplete(place: PlacesAutoComplete) {
    viewModelScope.launch {
      when (val destLatLng = googleRepository.getPlaceLatLng(placeId = place.prediction.placeId)) {
        is ServiceResult.Failure -> {
          toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
        }

        is ServiceResult.Value -> {
          if (destLatLng.value == null) {
            toastHandler?.invoke(ToastMessage.UNABLE_TO_RETRIEVE_COORDINATES)
          } else {
            devicePrefs.first().deviceLocation?.let {
              attemptCreateRide(destLatLng.value, place.address, it)
            }
          }
        }
      }
    }
  }

  fun onCancelRide() {
    viewModelScope.launch {
      rideRepository.cancelRide()
    }
  }

  private fun clearSearch() {
    _destinationQuery.update { "" }
    _autoCompletePlaces.update { emptyList() }
  }

  private suspend fun observeRide(cid: String, user: BoltUser) {
    rideRepository.observeRideById(rideId = cid)
    _passenger.update { user }
  }

  private suspend fun attemptCreateRide(destLatLon: LatLng, destAddress: String, currentLocation: Location) {
    val passenger = checkNotNull(_passenger.value)
    val createRide = CreateRide(
      passengerId = passenger.userId,
      passengerName = passenger.username,
      passengerLat = currentLocation.lat,
      passengerLng = currentLocation.lng,
      destAddress = destAddress,
      destLat = destLatLon.latitude,
      destLng = destLatLon.longitude,
    )
    when (val result = rideRepository.createRide(createRide)) {
      is ServiceResult.Failure -> {
        toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
      }

      is ServiceResult.Value -> {
        observeRide(result.value, _passenger.value!!)
        clearSearch()
      }
    }
  }

  private fun logoutUser() {
    viewModelScope.launch { logoutUseCase() }
  }

  private suspend fun Ride.routeToDriver(): DirectionsRoute? {
    return getDirections(
      orgLng = passengerLongitude,
      orgLat = passengerLatitude,
      dstLng = driverLongitude!!,
      dstLat = driverLatitude!!,
    )
  }

  private suspend fun Ride.routeToDestination(): DirectionsRoute? {
    return getDirections(
      orgLng = passengerLongitude,
      orgLat = passengerLatitude,
      dstLng = destinationLongitude,
      dstLat = destinationLatitude,
    )
  }

  private suspend fun getDirections(
    orgLat: Double,
    orgLng: Double,
    dstLat: Double,
    dstLng: Double,
  ): DirectionsRoute? {
    val directions = googleRepository.getDirectionsRoute(
      originLat = orgLat,
      originLng = orgLng,
      destLat = dstLat,
      destLng = dstLng,
    )
    return when (directions) {
      is ServiceResult.Failure -> {
        toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
        null
      }

      is ServiceResult.Value -> directions.value
    }
  }
}

private fun AutocompletePrediction.toPlacesAutoComplete() = PlacesAutoComplete(
  address = getFullText(null).toString(),
  prediction = this,
)
