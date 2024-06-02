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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.mtali.core.data.repositories.AuthRepository
import org.mtali.core.data.repositories.DeviceRepository
import org.mtali.core.models.Location
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
  private val authRepository: AuthRepository,
  private val deviceRepository: DeviceRepository,
) : ViewModel() {

  val uiState = authRepository.currentUser
    .map { user ->
      MainUiState.Success(isLoggedIn = user != null)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = MainUiState.Loading,
    )

  fun onLogout() {
    authRepository.logout()
  }

  fun updatePassengerLocation(latLng: LatLng) {
    viewModelScope.launch { deviceRepository.updateLocation(latLng.asLocation()) }
  }
}

sealed interface MainUiState {
  data object Loading : MainUiState
  data class Success(val isLoggedIn: Boolean = false) : MainUiState
}

private fun LatLng.asLocation() = Location(lat = latitude, lon = longitude)
