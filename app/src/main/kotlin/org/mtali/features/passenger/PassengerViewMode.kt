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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.mtali.core.data.repositories.DeviceRepository
import javax.inject.Inject

@HiltViewModel
class PassengerViewMode @Inject constructor(
  deviceRepository: DeviceRepository,
) : ViewModel() {

  private val _mapIsReady = MutableStateFlow(false)

  val deviceLocation = deviceRepository.deviceLocation

  fun onMapLoaded() {
    _mapIsReady.update { true }
  }
}
