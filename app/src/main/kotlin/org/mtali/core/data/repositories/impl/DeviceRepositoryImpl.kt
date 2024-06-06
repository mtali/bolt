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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.mtali.core.data.repositories.DeviceRepository
import org.mtali.core.datastore.PreferenceDataStore
import org.mtali.core.dispatcher.BoltDispatchers.IO
import org.mtali.core.dispatcher.Dispatcher
import org.mtali.core.models.DevicePrefs
import org.mtali.core.models.Location
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
  private val prefsDataStore: PreferenceDataStore,
  @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : DeviceRepository {

  override val devicePrefs: Flow<DevicePrefs> = prefsDataStore.devicePrefs

  override suspend fun updateLocation(location: Location) = withContext(ioDispatcher) {
    prefsDataStore.updateLocation(location)
  }

  override suspend fun toggleUserType() = withContext(ioDispatcher) {
    prefsDataStore.toggleUserType()
  }
}
