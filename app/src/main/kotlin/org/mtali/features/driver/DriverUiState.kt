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

import com.google.maps.model.DirectionsRoute

sealed interface DriverUiState {
  data object SearchingForPassengers : DriverUiState

  data class PassengerPickUp(
    val passengerLat: Double,
    val passengerLng: Double,
    val driverLat: Double,
    val driverLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val destinationAddress: String,
    val passengerName: String,
    val totalMessages: Int,
    val directionsRoute: DirectionsRoute?
  ) : DriverUiState

  data class EnRoute(
    val driverLat: Double,
    val driverLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val destinationAddress: String,
    val passengerName: String,
    val totalMessages: Int,
  ) : DriverUiState

  data class Arrive(
    val driverLat: Double,
    val driverLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val destinationAddress: String,
    val passengerName: String,
    val totalMessages: Int,
  ) : DriverUiState

  data object Loading : DriverUiState

  data object Error : DriverUiState
}
