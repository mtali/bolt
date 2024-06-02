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
import com.google.android.libraries.places.api.model.AutocompletePrediction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mtali.core.data.repositories.DeviceRepository
import org.mtali.core.data.repositories.GoogleRepository
import org.mtali.core.models.PlacesAutoComplete
import org.mtali.core.models.ServiceResult
import org.mtali.core.models.ToastMessage
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PassengerViewMode @Inject constructor(
  deviceRepository: DeviceRepository,
  private val googleRepository: GoogleRepository,
) : ViewModel() {

  var toastHandler: ((ToastMessage) -> Unit)? = null

  private val _mapIsReady = MutableStateFlow(false)
  private val _autoCompletePlaces = MutableStateFlow<List<PlacesAutoComplete>>(emptyList())
  val autoCompletePlaces: StateFlow<List<PlacesAutoComplete>> = _autoCompletePlaces

  private val _destinationQuery = MutableStateFlow("")
  val destinationQuery: StateFlow<String> = _destinationQuery

  val deviceLocation = deviceRepository.deviceLocation

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

  fun onDestinationQueryChange(query: String) {
    _destinationQuery.update { query }
    requestPlacesAutocomplete()
  }

  fun onClickPlaceAutoComplete(place: PlacesAutoComplete) {
    Timber.tag("wakanda").d("$place")
  }
}

private fun AutocompletePrediction.toPlacesAutoComplete() = PlacesAutoComplete(
  address = getFullText(null).toString(),
  prediction = this,
)
