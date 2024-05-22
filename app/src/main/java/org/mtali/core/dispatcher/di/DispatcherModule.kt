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