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

import io.getstream.chat.android.client.ChatClient
import org.mtali.core.data.repositories.RideRepository
import org.mtali.core.keys.KEY_DEST_ADDRESS
import org.mtali.core.keys.KEY_DEST_LAT
import org.mtali.core.keys.KEY_DEST_LON
import org.mtali.core.keys.KEY_PASSENGER_ID
import org.mtali.core.keys.KEY_PASSENGER_LAT
import org.mtali.core.keys.KEY_PASSENGER_LON
import org.mtali.core.keys.KEY_PASSENGER_NAME
import org.mtali.core.keys.KEY_STATUS
import org.mtali.core.keys.STREAM_CHANNEL_TYPE_LIVESTREAM
import org.mtali.core.models.Ride
import org.mtali.core.models.RideStatus
import org.mtali.core.models.ServiceResult
import org.mtali.core.utils.newUUID
import timber.log.Timber
import javax.inject.Inject

class RideRepositoryImpl @Inject constructor(
  private val client: ChatClient,
) : RideRepository {
  override suspend fun createRide(ride: Ride): ServiceResult<String> {
    val channelId = newUUID()
    val result = client.createChannel(
      channelType = STREAM_CHANNEL_TYPE_LIVESTREAM,
      channelId = channelId,
      memberIds = listOf(ride.passengerId),
      extraData = mapOf(
        KEY_STATUS to RideStatus.SEARCHING_FOR_DRIVER,
        KEY_PASSENGER_ID to ride.passengerId,
        KEY_PASSENGER_NAME to ride.passengerName,
        KEY_PASSENGER_LAT to ride.passengerLat,
        KEY_PASSENGER_LON to ride.passengerLng,
        KEY_DEST_ADDRESS to ride.destAddress,
        KEY_DEST_LAT to ride.destLat,
        KEY_DEST_LON to ride.destLng,
      ),
    ).await()
    val cid = result.getOrNull()?.cid
    Timber.tag("wakanda").d("Channel created: $cid")
    return if (cid != null) {
      ServiceResult.Value(cid)
    } else {
      ServiceResult.Failure(Exception(result.errorOrNull()?.message))
    }
  }
}
