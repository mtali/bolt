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
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.result.onErrorSuspend
import io.getstream.result.onSuccessSuspend
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.mtali.core.data.repositories.RideRepository
import org.mtali.core.dispatcher.BoltDispatchers.IO
import org.mtali.core.dispatcher.Dispatcher
import org.mtali.core.keys.FILTER_CREATED_AT
import org.mtali.core.keys.FILTER_UPDATED_AT
import org.mtali.core.keys.KEY_DEST_ADDRESS
import org.mtali.core.keys.KEY_DEST_LAT
import org.mtali.core.keys.KEY_DEST_LON
import org.mtali.core.keys.KEY_DRIVER_ID
import org.mtali.core.keys.KEY_DRIVER_LAT
import org.mtali.core.keys.KEY_DRIVER_LON
import org.mtali.core.keys.KEY_DRIVER_NAME
import org.mtali.core.keys.KEY_PASSENGER_ID
import org.mtali.core.keys.KEY_PASSENGER_LAT
import org.mtali.core.keys.KEY_PASSENGER_LON
import org.mtali.core.keys.KEY_PASSENGER_NAME
import org.mtali.core.keys.KEY_STATUS
import org.mtali.core.keys.STREAM_CHANNEL_TYPE_LIVESTREAM
import org.mtali.core.models.BoltUser
import org.mtali.core.models.CreateRide
import org.mtali.core.models.Ride
import org.mtali.core.models.RideStatus
import org.mtali.core.models.ServiceResult
import org.mtali.core.utils.newUUID
import timber.log.Timber
import javax.inject.Inject

const val tag = "wakanda:RideRepository"

class RideRepositoryImpl @Inject constructor(
  @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
  private val client: ChatClient,
) : RideRepository {

  private val _rideUpdates: MutableStateFlow<ServiceResult<Ride?>> = MutableStateFlow(ServiceResult.Value(null))

  private val _openRides: MutableStateFlow<ServiceResult<List<Ride>>> = MutableStateFlow(ServiceResult.Value(emptyList()))

  override fun rideFlow(): Flow<ServiceResult<Ride?>> = _rideUpdates

  override fun openRides(): Flow<ServiceResult<List<Ride>>> = _openRides

  override suspend fun getRideIfInProgress(): ServiceResult<String?> = withContext(ioDispatcher) {
    Timber.tag(tag).d("getRideIfInProgress() :-> called")
    val currentUserId = client.getCurrentUser()?.id ?: ""
    val request = QueryChannelsRequest(
      filter = Filters.`in`("members", currentUserId),
      querySort = QuerySortByField.descByName(FILTER_UPDATED_AT),
      limit = 1,
    )
    val result = client.queryChannels(request).await()
    if (result.isSuccess) {
      val channels = result.getOrNull() ?: emptyList()
      if (channels.isEmpty()) {
        Timber.tag(tag).d("getRideIfInProgress() :-> no ride/channel exist")
        ServiceResult.Value(null)
      } else {
        channels.first().let {
          Timber.tag(tag).d("getRideIfInProgress() :-> channel found cid=${it.cid}")
          ServiceResult.Value(it.cid)
        }
      }
    } else {
      Timber.tag(tag).e("getRideIfInProgress() :-> failed with ${result.errorOrNull()}")
      ServiceResult.Failure(Exception(result.errorOrNull()?.message))
    }
  }

  override suspend fun createRide(createRide: CreateRide): ServiceResult<String> {
    Timber.tag(tag).d("createRide() :-> called")
    val channelId = newUUID()
    val result = client.createChannel(
      channelType = STREAM_CHANNEL_TYPE_LIVESTREAM,
      channelId = channelId,
      memberIds = listOf(createRide.passengerId),
      extraData = mapOf(
        KEY_STATUS to RideStatus.SEARCHING_FOR_DRIVER,
        KEY_PASSENGER_ID to createRide.passengerId,
        KEY_PASSENGER_NAME to createRide.passengerName,
        KEY_PASSENGER_LAT to createRide.passengerLat,
        KEY_PASSENGER_LON to createRide.passengerLng,
        KEY_DEST_ADDRESS to createRide.destAddress,
        KEY_DEST_LAT to createRide.destLat,
        KEY_DEST_LON to createRide.destLng,
      ),
    ).await()
    val cid = result.getOrNull()?.cid
    return if (cid != null) {
      Timber.tag(tag).d("createRide() :-> ride created with cid=$cid")
      ServiceResult.Value(cid)
    } else {
      Timber.tag(tag).e("createRide() :-> failed to create ride with error ${result.errorOrNull()}")
      ServiceResult.Failure(Exception(result.errorOrNull()?.message))
    }
  }

  override suspend fun observeRideById(rideId: String): Unit = withContext(ioDispatcher) {
    Timber.tag(tag).d("observeRideById(rideId=$rideId) :-> called")
    val channelClient = client.channel(cid = rideId)

    Timber.tag(tag).d("observeRideById() :-> adding member ${client.getCurrentUser()?.id} to channel")
    val result = channelClient.addMembers(listOf(client.getCurrentUser()?.id ?: "")).await()

    result.onSuccessSuspend { channel ->
      Timber.tag(tag).d("observeRideById() :-> member added")
      observeChannelEvents(channelClient)
      _rideUpdates.emit(ServiceResult.Value(streamChannelToRide(channel)))
    }

    result.onErrorSuspend { error ->
      Timber.tag(tag).e("observeRideById() :-> failed to add member with error $error")
      _rideUpdates.emit(ServiceResult.Failure(Exception(error.message)))
    }
  }

  override suspend fun observeOpenRides() = withContext(ioDispatcher) {
    Timber.tag(tag).d("observeOpenRides() :-> called")
    val request = QueryChannelsRequest(
      filter = Filters.and(Filters.eq(KEY_STATUS, RideStatus.SEARCHING_FOR_DRIVER.value)),
      querySort = QuerySortByField.descByName(FILTER_CREATED_AT),
      limit = 10,
    )
    val result = client.queryChannels(request).await()
    if (result.isSuccess) {
      Timber.tag(tag).d("observeOpenRides() :-> success")
      _openRides.emit(ServiceResult.Value(result.getOrNull()!!.map { streamChannelToRide(it) }))
    } else {
      Timber.tag(tag).e("observeOpenRides() :-> failed with error ${result.errorOrNull()}")
      _openRides.emit(ServiceResult.Failure(Exception(result.errorOrNull()?.message)))
    }
  }

  override suspend fun cancelRide(): ServiceResult<Unit> = withContext(ioDispatcher) {
    Timber.tag(tag).d("cancelRide() :-> called")
    val currentUserId = client.getCurrentUser()?.id ?: ""
    val request = QueryChannelsRequest(
      filter = Filters.`in`("members", currentUserId),
      querySort = QuerySortByField.descByName(FILTER_UPDATED_AT),
      limit = 1,
    )
    val result = client.queryChannels(request).await()
    if (result.isSuccess) {
      val channels = result.getOrNull() ?: emptyList()
      if (channels.isEmpty()) {
        Timber.tag(tag).w("cancelRide() :-> no channel/ride to cancel")
        ServiceResult.Value(Unit)
      } else {
        val channelClient = client.channel(cid = channels.first().cid)
        if (channelClient.hide().await().isSuccess) {
          Timber.tag(tag).e("cancelRide() :-> channel/ride hidden")
          val deleteResult = channelClient.delete().await()
          if (deleteResult.isSuccess) {
            Timber.tag(tag).d("cancelRide() :-> channel/ride deleted")
            _rideUpdates.emit(ServiceResult.Value(null))
            ServiceResult.Value(Unit)
          } else {
            Timber.tag(tag).e("cancelRide() :-> failed tp delete channel/ride")
            ServiceResult.Failure(Exception(result.errorOrNull()?.message))
          }
        } else {
          Timber.tag(tag).e("cancelRide() :-> failed to hide channel/ride")
          ServiceResult.Failure(Exception("Unable to hide channel"))
        }
      }
    } else {
      Timber.tag(tag).e("cancelRide() :-> failed to retrieve channel")
      ServiceResult.Failure(Exception(result.errorOrNull()?.message))
    }
  }

  // TODO: Pass ride id only
  override suspend fun completeRide(ride: Ride): ServiceResult<Unit> {
    Timber.tag(tag).d("completeRide(${ride.rideId}) :-> called")
    val channelClient = client.channel(cid = ride.rideId)
    channelClient.delete().await()
    Timber.tag(tag).d("completeRide(${ride.rideId}) :-> done")
    return ServiceResult.Value(Unit)
  }

  override suspend fun advanceRide(rideId: String, newState: String): ServiceResult<Unit> =
    withContext(ioDispatcher) {
      Timber.tag(tag).d("advanceRide(id=$rideId, to=$newState) :-> start")
      val advanceRide = client.updateChannelPartial(
        channelType = STREAM_CHANNEL_TYPE_LIVESTREAM,
        channelId = getChannelIdOnly(rideId),
        set = mapOf(
          KEY_STATUS to newState,
        ),
      ).await()
      if (advanceRide.isSuccess) {
        Timber.tag(tag).d("advanceRide() :-> advanced")
        ServiceResult.Value(Unit)
      } else {
        Timber.tag(tag).d("advanceRide() :-> failed to advance with error ${advanceRide.errorOrNull()}")
        ServiceResult.Failure(Exception(advanceRide.errorOrNull()?.message))
      }
    }

  // TODO: Pass ride id
  override suspend fun updateDriverLocation(ride: Ride, lat: Double, lng: Double): ServiceResult<Unit> =
    withContext(ioDispatcher) {
      Timber.tag(tag).d("updateDriverLocation(rideId-${ride.rideId}, lat=$lat, lng=$lng) :-> called")
      val updateRide = client.updateChannelPartial(
        channelType = STREAM_CHANNEL_TYPE_LIVESTREAM,
        channelId = getChannelIdOnly(ride.rideId),
        set = mapOf(
          KEY_DRIVER_LAT to lat,
          KEY_PASSENGER_LON to lng,
        ),
      ).await()
      if (updateRide.isSuccess) {
        // Update will trigger event global but not locally
        val currentRide = _rideUpdates.value
        if (currentRide is ServiceResult.Value && currentRide.value != null) {
          Timber.tag(tag).d("updateDriverLocation() :-> channel/ride updated")
          _rideUpdates.value = ServiceResult.Value(
            currentRide.value.copy(
              driverLatitude = lat,
              driverLongitude = lng,
            ),
          )
        }
        ServiceResult.Value(Unit)
      } else {
        Timber.tag(tag).e("updateDriverLocation() :-> failed to update ride with error ${updateRide.errorOrNull()}")
        ServiceResult.Failure(Exception(updateRide.errorOrNull()?.message))
      }
    }

  override suspend fun updatePassengerLocation(ride: Ride, lat: Double, lng: Double): ServiceResult<Unit> =
    withContext(ioDispatcher) {
      Timber.tag(tag).d("updatePassengerLocation(rideId-${ride.rideId}, lat=$lat, lng=$lng) :-> called")
      val updateRide = client.updateChannelPartial(
        channelType = STREAM_CHANNEL_TYPE_LIVESTREAM,
        channelId = getChannelIdOnly(ride.rideId),
        set = mapOf(
          KEY_PASSENGER_LAT to lat,
          KEY_PASSENGER_LON to lng,
        ),
      ).await()
      if (updateRide.isSuccess) {
        // Update will trigger event global but not locally
        Timber.tag(tag).d("updatePassengerLocation() :-> location updated, now updating local ride/channel cache")
        val currentRide = _rideUpdates.value
        if (currentRide is ServiceResult.Value && currentRide.value != null) {
          _rideUpdates.value = ServiceResult.Value(
            currentRide.value.copy(
              driverLatitude = lat,
              driverLongitude = lng,
            ),
          )
        }
        ServiceResult.Value(Unit)
      } else {
        Timber.tag(tag).e("updatePassengerLocation() :-> failed to update ride/channel with error ${updateRide.errorOrNull()}")
        ServiceResult.Failure(Exception(updateRide.errorOrNull()?.message))
      }
    }

  override suspend fun connectDriverToRide(ride: Ride, driver: BoltUser): ServiceResult<String> =
    withContext(ioDispatcher) {
      Timber.tag(tag).d("connectDriverToRide(ride=${ride.rideId}, driver=${driver.userId}) :-> called")
      val channelClient = client.channel(cid = ride.rideId)
      Timber.tag(tag).d("connectDriverToRide() :-> adding driver to ride/channel members")
      val addToChannel = channelClient.addMembers(listOf(client.getCurrentUser()?.id ?: "")).await()
      if (addToChannel.isSuccess) {
        Timber.tag(tag).d("connectDriverToRide() :-> adding driver to ride/channel success .. now updating channel details")
        val updateDetails = channelClient.updatePartial(
          set = mapOf(
            KEY_STATUS to RideStatus.PASSENGER_PICK_UP.value,
            KEY_DRIVER_ID to driver.userId,
            KEY_DRIVER_NAME to driver.username,
            KEY_DRIVER_LAT to ride.driverLatitude!!,
            KEY_DRIVER_LON to ride.driverLongitude!!,
          ),
        ).await()
        if (updateDetails.isSuccess) {
          Timber.tag(tag).d("connectDriverToRide() :-> channel/ride updated, we have a driver now")
          ServiceResult.Value(channelClient.cid)
        } else {
          Timber.tag(tag).e("connectDriverToRide() :-> failed to update ride driver info with error ${updateDetails.getOrNull()}")
          ServiceResult.Failure(Exception(updateDetails.errorOrNull()?.message))
        }
      } else {
        Timber.tag(tag).e("connectDriverToRide() :-> failed add driver to channel/ride members with error ${addToChannel.getOrNull()}")
        ServiceResult.Failure(Exception(addToChannel.errorOrNull()?.message))
      }
    }

  // TODO: Find a way to dispose this
  private fun observeChannelEvents(channelClient: ChannelClient) {
    Timber.tag(tag).i("observeChannelEvents() :-> called and subscribed")
    channelClient.subscribe { event ->
      Timber.tag(tag).d("observeChannelEvents() :-> received event of type = ${event.type}")
      when (event) {
        is ChannelDeletedEvent -> {
          _openRides.update { ServiceResult.Value(emptyList()) }
          _rideUpdates.update { ServiceResult.Value(null) }
        }

        is ChannelUpdatedByUserEvent -> {
          _rideUpdates.update { ServiceResult.Value(streamChannelToRide(event.channel)) }
        }

        is NewMessageEvent -> {
          val currentRide = _rideUpdates.value
          if (currentRide is ServiceResult.Value && currentRide.value != null) {
            client.channel(cid = event.cid).create(emptyList(), mapOf()).enqueue { result ->
              if (result.isSuccess) {
                val lastMessageAt = result.getOrNull()?.lastMessageAt
                val totalMessages = if (lastMessageAt == null) 0 else 1
                _rideUpdates.update { ServiceResult.Value(currentRide.value.copy(totalMessages = totalMessages)) }
              }
            }
          }
        }

        else -> {
          Timber.tag(tag).w("observeChannelEvents() :-> event of type = ${event.type} was not processed")
        }
      }
    }
  }

  private fun streamChannelToRide(channel: Channel): Ride {
    Timber.tag(tag).d("streamChannelToRide(cid=${channel.cid}) :-> called")
    val extraData = channel.extraData

    val destAddress = extraData[KEY_DEST_ADDRESS] as String?
    val destLat = extraData[KEY_DEST_LAT] as Double?
    val destLng = extraData[KEY_DEST_LON] as Double?

    val driverId = extraData[KEY_DRIVER_ID] as String?
    val driverLat = extraData[KEY_DRIVER_LAT] as Double?
    val driverLng = extraData[KEY_DRIVER_LON] as Double?
    val driverName = extraData[KEY_DRIVER_NAME] as String?

    val passengerId = extraData[KEY_PASSENGER_ID] as String?
    val passengerLat = extraData[KEY_PASSENGER_LAT] as Double?
    val passengerLng = extraData[KEY_PASSENGER_LON] as Double?
    val passengerName = extraData[KEY_PASSENGER_NAME] as String?
    val status = extraData[KEY_STATUS] as String?

    val ride = Ride(
      rideId = channel.cid,
      status = status ?: RideStatus.SEARCHING_FOR_DRIVER.value,
      destinationLatitude = destLat ?: 999.9,
      destinationLongitude = destLng ?: 999.9,
      destinationAddress = destAddress ?: "",
      passengerId = passengerId ?: "",
      passengerLatitude = passengerLat ?: 999.0,
      passengerLongitude = passengerLng ?: 999.0,
      passengerName = passengerName ?: "",
      driverId = driverId,
      driverLatitude = driverLat,
      driverLongitude = driverLng,
      driverName = driverName,
      totalMessages = if (channel.lastMessageAt == null) 0 else 1,
    )
    Timber.tag(tag).d("streamChannelToRide() :-> ride=$ride")
    return ride
  }

  private fun getChannelIdOnly(cid: String): String = cid.split(":").last()
}
