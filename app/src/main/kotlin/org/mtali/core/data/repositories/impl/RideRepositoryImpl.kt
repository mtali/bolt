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
package org.mtali.core.data.repositories.impl

import org.mtali.core.data.repositories.RideRepository
import org.mtali.core.models.Ride
import org.mtali.core.models.ServiceResult
import org.mtali.core.utils.newUUID
import timber.log.Timber
import javax.inject.Inject

class RideRepositoryImpl @Inject constructor() : RideRepository {
  override suspend fun createRide(ride: Ride): ServiceResult<String> {
    val channelId = newUUID()
    Timber.tag("wakanda").d("Ride: $ride")
    return ServiceResult.Value(channelId)
  }
}
