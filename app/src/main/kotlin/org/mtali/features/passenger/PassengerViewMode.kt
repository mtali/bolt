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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
import org.mtali.core.models.ServiceResult
import org.mtali.core.models.ToastMessage
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PassengerViewMode @Inject constructor(
  deviceRepository: DeviceRepository,
  private val googleRepository: GoogleRepository,
  private val rideRepository: RideRepository,
  private val getUserUseCase: GetUserUseCase,
  private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

  var toastHandler: ((ToastMessage) -> Unit)? = null

  private val _mapIsReady = MutableStateFlow(false)
  private val _passenger = MutableStateFlow<BoltUser?>(null)
  private var _ride: Flow<ServiceResult<Ride?>> = rideRepository.rideFlow()

  private val _autoCompletePlaces = MutableStateFlow<List<PlacesAutoComplete>>(emptyList())
  val autoCompletePlaces: StateFlow<List<PlacesAutoComplete>> = _autoCompletePlaces

  private val _destinationQuery = MutableStateFlow("")
  val destinationQuery: StateFlow<String> = _destinationQuery

  private val deviceLocation = deviceRepository.deviceLocation

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
  val uiState = combine(
    _passenger,
    _ride,
    _mapIsReady,
  ) { passenger, ride, mapIsReady ->
  }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = null,
    )

  init {
    getPassenger()
  }

  fun onMapLoaded() = _mapIsReady.update { true }

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
          _passenger.value = getUserResult.value
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
        is ServiceResult.Failure -> toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
        is ServiceResult.Value -> {
          if (destLatLng.value == null) {
            toastHandler?.invoke(ToastMessage.UNABLE_TO_RETRIEVE_COORDINATES)
          } else {
            deviceLocation.first()?.let { currentLocation ->
              attemptCreateRide(destLatLng.value, place.address, currentLocation)
            }
          }
        }
      }
    }
  }

  private fun clearSearch() {
    _destinationQuery.update { "" }
    _autoCompletePlaces.update { emptyList() }
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
    val result = rideRepository.createRide(createRide)
    when (result) {
      is ServiceResult.Failure -> toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
      is ServiceResult.Value -> {
        clearSearch()
      }
    }
  }

  private fun logoutUser() {
    Timber.e("Forced logout")
    viewModelScope.launch { logoutUseCase() }
  }
}

private fun AutocompletePrediction.toPlacesAutoComplete() = PlacesAutoComplete(
  address = getFullText(null).toString(),
  prediction = this,
)
