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
package org.mtali.core.dispatcher.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import org.mtali.core.dispatcher.BoltDispatchers
import org.mtali.core.dispatcher.Dispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
  @Provides
  @Dispatcher(BoltDispatchers.IO)
  fun providesIO() = Dispatchers.IO

  @Provides
  @Dispatcher(BoltDispatchers.Default)
  fun providesDefault() = Dispatchers.Default

  @Provides
  @Dispatcher(BoltDispatchers.Main)
  fun providesMain() = Dispatchers.Main
}
