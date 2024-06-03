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
package org.mtali.app

import android.app.Application
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import org.mtali.BuildConfig
import timber.log.Timber

@HiltAndroidApp
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    initPlaces()
    initStream()
    if (isDebug()) Timber.plant(Timber.DebugTree())
  }

  private fun initPlaces() {
    Places.initialize(this, BuildConfig.MAPS_API_KEY)
  }

  private fun initStream() {
    val logLevel = if (org.mtali.core.utils.isDebug()) ChatLogLevel.ALL else ChatLogLevel.ERROR
    val offlinePlugin = StreamOfflinePluginFactory(appContext = this)
    ChatClient.Builder(BuildConfig.STREAM_KEY, this)
      .withPlugins(offlinePlugin)
      .logLevel(logLevel)
      .build()
  }

  private fun isDebug() = BuildConfig.DEBUG
}
