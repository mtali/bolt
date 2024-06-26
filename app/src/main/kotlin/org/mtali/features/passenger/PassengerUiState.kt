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

import com.google.maps.model.DirectionsRoute

sealed interface PassengerUiState {
  data object RideInactive : PassengerUiState

  data class SearchingForDriver(
    val passengerLat: Double,
    val passengerLng: Double,
    val destinationAddress: String,
  ) : PassengerUiState

  data class PassengerPickUp(
    val passengerLat: Double,
    val passengerLng: Double,
    val driverLat: Double,
    val driverLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val destinationAddress: String,
    val driverName: String,
    val totalMessages: Int,
    val driverRoute: DirectionsRoute?,
  ) : PassengerUiState

  data class EnRoute(
    val passengerLat: Double,
    val passengerLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val driverLat: Double,
    val driverLng: Double,
    val destinationAddress: String,
    val driverName: String,
    val totalMessages: Int,
    val destinationRoute: DirectionsRoute?,
  ) : PassengerUiState

  data class Arrive(
    val passengerLat: Double,
    val passengerLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val driverName: String,
    val totalMessages: Int,
  ) : PassengerUiState

  data object Error : PassengerUiState

  data object Loading : PassengerUiState
}
