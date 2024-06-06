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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.mtali.core.data.repositories.DeviceRepository
import org.mtali.core.data.repositories.FirebaseAuthRepository
import org.mtali.core.data.repositories.StreamUserRepository
import org.mtali.core.domain.LogoutUseCase
import org.mtali.core.models.Location
import org.mtali.core.models.ServiceResult
import org.mtali.core.models.ToastMessage
import org.mtali.core.models.UserType
import org.mtali.core.utils.isRunning
import org.mtali.features.driver.navigation.driverRoute
import org.mtali.features.login.navigation.navigation.loginRoute
import org.mtali.features.passenger.navigation.passengerRoute
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
  private val firebaseAuthRepository: FirebaseAuthRepository,
  private val streamUserRepository: StreamUserRepository,
  private val deviceRepository: DeviceRepository,
  private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

  private var toggleStateJob: Job? = null

  private val _userType = deviceRepository.devicePrefs.map { it.userType }.distinctUntilChanged()

  val uiState = combine(
    firebaseAuthRepository.currentUser,
    streamUserRepository.streamUser,
    _userType,
  ) { firebase, stream, userType ->
    if (firebase != null && stream == null) reAuthStream(firebase.userId)
    val isLoggedIn = firebase != null && stream != null
    MainUiState.Success(isLoggedIn = isLoggedIn, userType = userType)
  }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = MainUiState.Loading,
    )

  var toastHandle: ((ToastMessage) -> Unit)? = null

  fun onLogout() {
    firebaseAuthRepository.logout()
  }

  fun updatePassengerLocation(latLng: LatLng) {
    viewModelScope.launch { deviceRepository.updateLocation(latLng.asLocation()) }
  }

  fun onToggleUserType() {
    if (toggleStateJob.isRunning()) return
    toggleStateJob = viewModelScope.launch { deviceRepository.toggleUserType() }
  }

  private suspend fun reAuthStream(userId: String) {
    val result = streamUserRepository.getStreamUserById(userId)
    if (result is ServiceResult.Failure || (result is ServiceResult.Value && result.value == null)) {
      toastHandle?.invoke(ToastMessage.FAILED_TO_REAUTH)
      logoutUseCase()
    }
  }
}

sealed interface MainUiState {
  data object Loading : MainUiState
  data class Success(
    val isLoggedIn: Boolean = false,
    val userType: UserType,
  ) : MainUiState
}

fun MainUiState.Success.getRoute(): String {
  return when {
    !isLoggedIn -> loginRoute
    userType == UserType.DRIVER -> driverRoute
    else -> passengerRoute
  }
}

private fun LatLng.asLocation() = Location(lat = latitude, lng = longitude)
