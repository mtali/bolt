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
package org.mtali.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.mtali.core.models.DevicePrefs
import org.mtali.core.models.Location
import org.mtali.core.models.UserType
import javax.inject.Inject

private object PrefsKeys {
  val DEVICE_LOCATION = stringPreferencesKey("device_location")
  val IS_PASSENGER = booleanPreferencesKey("is_passenger")
}

private const val DEFAULT_IS_PASSENGER = true

class PreferenceDataStore @Inject constructor(private val dataStore: DataStore<Preferences>) {
  val devicePrefs: Flow<DevicePrefs> = dataStore.data
    .map { prefs ->
      val location = prefs[PrefsKeys.DEVICE_LOCATION]
      val isPassenger = prefs[PrefsKeys.IS_PASSENGER] ?: DEFAULT_IS_PASSENGER
      DevicePrefs(
        deviceLocation = location?.let { Json.decodeFromString<Location>(it) },
        userType = if (isPassenger) UserType.PASSENGER else UserType.DRIVER,
      )
    }

  suspend fun updateLocation(location: Location) {
    dataStore.edit { it[PrefsKeys.DEVICE_LOCATION] = Json.encodeToString(location) }
  }

  suspend fun toggleUserType() {
    dataStore.edit {
      val isPassenger = it[PrefsKeys.IS_PASSENGER] ?: DEFAULT_IS_PASSENGER
      it[PrefsKeys.IS_PASSENGER] = !isPassenger
    }
  }
}
