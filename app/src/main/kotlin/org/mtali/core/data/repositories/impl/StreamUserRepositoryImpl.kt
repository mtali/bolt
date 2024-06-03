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
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.models.User
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.mtali.core.data.repositories.StreamUserRepository
import org.mtali.core.dispatcher.BoltDispatchers.IO
import org.mtali.core.dispatcher.Dispatcher
import org.mtali.core.keys.KEY_ROLE
import org.mtali.core.keys.KEY_STATUS
import org.mtali.core.keys.KEY_TYPE
import org.mtali.core.models.BoltUser
import org.mtali.core.models.ServiceResult
import org.mtali.core.models.UserStatus
import org.mtali.core.models.UserType
import timber.log.Timber
import javax.inject.Inject

private const val CONNECT_TIMEOUT = 5_000L

class StreamUserRepositoryImpl @Inject constructor(
  @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
  private val client: ChatClient,
) : StreamUserRepository {

  override val streamUser: Flow<BoltUser?> = callbackFlow {
    val subscription = client.subscribeFor(ConnectedEvent::class.java) { event ->
      trySend(client.getCurrentUser()?.let { BoltUser(userId = it.id) })
    }
    trySend(null)
    awaitClose {
      subscription.dispose()
    }
  }

  /**
   * Make sure to update Roles & Permission on the Dashboard
   * for this to work (by default users can't update their roles)
   */
  private suspend fun roleToAdmin(userId: String): Result<User> {
    return client.partialUpdateUser(id = userId, mapOf(KEY_ROLE to "admin")).await()
  }

  override suspend fun initSteamUser(user: BoltUser): ServiceResult<BoltUser> = withContext(ioDispatcher) {
    disconnectUser(userId = user.userId)
    val streamUser = User(
      id = user.userId,
      name = user.username,
      extraData = mapOf(KEY_STATUS to user.status, KEY_TYPE to user.type),
    )
    val token = client.devToken(user.userId)
    try {
      val result = client.connectUser(streamUser, token, CONNECT_TIMEOUT).await()
      if (result.isSuccess) {
        ServiceResult.Value(user)
      } else {
        ServiceResult.Failure(exception = Exception(result.errorOrNull()?.message))
      }
    } catch (e: Exception) {
      ServiceResult.Failure(exception = e)
    }
  }

  override suspend fun getStreamUserById(userId: String): ServiceResult<BoltUser?> = withContext(ioDispatcher) {
    val currentUser = client.getCurrentUser()
    if (currentUser != null && currentUser.id == userId) { // User is logged-in
      val extraData = currentUser.extraData
      val type: String? = extraData[KEY_TYPE] as String?
      val status: String? = extraData[KEY_STATUS] as String?

      if (currentUser.role != "admin") {
        roleToAdmin(currentUser.id)
      }

      ServiceResult.Value(
        BoltUser(
          userId = userId,
          username = currentUser.name,
          createdAt = currentUser.createdAt.toString(),
          updatedAt = currentUser.updatedAt.toString(),
          status = status ?: "",
          type = type ?: "",
        ),
      )
    } else if (currentUser != null) { // Different user is logged-in
      val streamUser = User(id = userId)
      val devToken = client.devToken(userId)
      val getUserResult = client.switchUser(user = streamUser, devToken).await()
      if (getUserResult.isSuccess) {
        getStreamUserById(userId)
      } else {
        val message = getUserResult.errorOrNull()?.message ?: "Stream error to switch user"
        Timber.e(message)
        ServiceResult.Failure(Exception(message))
      }
    } else { // No user at all
      val streamUser = User(
        id = userId,
        extraData = mapOf(
          KEY_TYPE to UserType.PASSENGER.value,
          KEY_STATUS to UserStatus.INACTIVE.value,
        ),
      )

      val devToken = client.devToken(userId)
      val getUserResult = client.connectUser(streamUser, devToken, CONNECT_TIMEOUT).await()
      if (getUserResult.isSuccess) {
        getStreamUserById(userId)
      } else {
        val message = getUserResult.errorOrNull()?.message ?: "Stream error connecting user"
        Timber.e(message)
        ServiceResult.Failure(Exception(message))
      }
    }
  }

  override suspend fun logout() = withContext(ioDispatcher) {
    val result = client.disconnect(true).await()
    if (result.isFailure) {
      Timber.e("Failed to logout stream user. Message: ${result.errorOrNull()?.message}")
    }
  }

  private suspend fun disconnectUser(userId: String) {
    val user = client.getCurrentUser()
    if (user != null && userId == user.id) {
      client.disconnect(false).await()
    }
  }
}
