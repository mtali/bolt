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
package org.mtali.core.data.repositories

import kotlinx.coroutines.flow.Flow
import org.mtali.core.models.CreateRide
import org.mtali.core.models.Ride
import org.mtali.core.models.ServiceResult

interface RideRepository {
  fun rideFlow(): Flow<ServiceResult<Ride?>>
  fun openRides(): Flow<ServiceResult<List<Ride>>>
  suspend fun getRideIfInProgress(): ServiceResult<String?>
  suspend fun createRide(createRide: CreateRide): ServiceResult<String>
  suspend fun observeRideById(rideId: String)
  suspend fun observeOpenRides()
  suspend fun cancelRide(): ServiceResult<Unit>
  suspend fun completeRide(ride: Ride): ServiceResult<Unit>
  suspend fun advanceRide(rideId: String, newState: String): ServiceResult<Unit>
  suspend fun updateDriverLocation(ride: Ride, lat: Double, lng: Double): ServiceResult<Unit>
  suspend fun updatePassengerLocation(ride: Ride, lat: Double, lng: Double): ServiceResult<Unit>
}
