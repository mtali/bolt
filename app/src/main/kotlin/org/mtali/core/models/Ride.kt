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
package org.mtali.core.models

data class Ride(
  val rideId: String,
  val status: String = RideStatus.SEARCHING_FOR_DRIVER.value,
  val destinationLatitude: Double = 0.0,
  val destinationLongitude: Double = 0.0,
  val destinationAddress: String = "",
  val passengerId: String = "",
  val passengerLatitude: Double = 0.0,
  val passengerLongitude: Double = 0.0,
  val passengerName: String = "",
  val passengerAvatarUrl: String? = null,
  val driverId: String? = null,
  val driverLatitude: Double? = null,
  val driverLongitude: Double? = null,
  val driverName: String? = null,
  val driverAvatarUrl: String? = null,
  val createAt: String = "",
  val updatedAt: String = "",
  val totalMessages: Int = 0,
)
