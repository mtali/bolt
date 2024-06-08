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
package org.mtali.core.data.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.maps.GeoApiContext
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.getstream.chat.android.client.ChatClient
import org.mtali.BuildConfig
import org.mtali.core.data.repositories.DeviceRepository
import org.mtali.core.data.repositories.FirebaseAuthRepository
import org.mtali.core.data.repositories.GoogleRepository
import org.mtali.core.data.repositories.RideRepository
import org.mtali.core.data.repositories.StreamUserRepository
import org.mtali.core.data.repositories.impl.DeviceRepositoryImpl
import org.mtali.core.data.repositories.impl.FirebaseFirebaseAuthRepositoryImpl
import org.mtali.core.data.repositories.impl.GoogleRepositoryImpl
import org.mtali.core.data.repositories.impl.RideRepositoryImpl
import org.mtali.core.data.repositories.impl.StreamUserRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
  @Binds
  abstract fun bindsAuthRepo(repo: FirebaseFirebaseAuthRepositoryImpl): FirebaseAuthRepository

  @Binds
  abstract fun bindsDeviceRepo(repo: DeviceRepositoryImpl): DeviceRepository

  @Binds
  @Singleton
  abstract fun bindsGoogleRepo(repo: GoogleRepositoryImpl): GoogleRepository

  @Binds
  @Singleton
  abstract fun bindUsersRepo(repo: StreamUserRepositoryImpl): StreamUserRepository

  @Binds
  @Singleton
  abstract fun rideRepo(repository: RideRepositoryImpl): RideRepository

  companion object {
    @Provides
    fun providesFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    fun providesStreamClient(): ChatClient = ChatClient.instance()

    @Provides
    @Singleton
    fun providesGeoContext(): GeoApiContext {
      return GeoApiContext.Builder()
        .apiKey(BuildConfig.MAPS_API_KEY)
        .build()
    }
  }
}
