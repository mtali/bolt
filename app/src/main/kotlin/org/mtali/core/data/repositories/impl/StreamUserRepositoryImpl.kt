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
import io.getstream.chat.android.models.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.mtali.core.data.repositories.StreamUserRepository
import org.mtali.core.dispatcher.BoltDispatchers.IO
import org.mtali.core.dispatcher.Dispatcher
import org.mtali.core.keys.KEY_STATUS
import org.mtali.core.keys.KEY_TYPE
import org.mtali.core.models.BoltUser
import org.mtali.core.models.ServiceResult
import javax.inject.Inject

class StreamUserRepositoryImpl @Inject constructor(
  @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
  private val client: ChatClient,
) : StreamUserRepository {

  override suspend fun initSteamUser(user: BoltUser): ServiceResult<BoltUser> = withContext(ioDispatcher) {
    disconnectUser(userId = user.userId)
    val streamUser = User(
      id = user.userId,
      name = user.username,
      extraData = mapOf(KEY_STATUS to user.status, KEY_TYPE to user.type),
    )
    val token = client.devToken(user.userId)
    try {
      val result = client.connectUser(streamUser, token).await()
      if (result.isSuccess) {
        ServiceResult.Value(user)
      } else {
        ServiceResult.Failure(exception = Exception(result.errorOrNull()?.message))
      }
    } catch (e: Exception) {
      ServiceResult.Failure(exception = e)
    }
  }

  private suspend fun disconnectUser(userId: String) {
    val user = client.getCurrentUser()
    if (user != null && userId == user.id) {
      client.disconnect(false).await()
    }
  }
}
