package org.mtali.core.dispatcher

import javax.inject.Qualifier


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val niaDispatcher: BoltDispatchers)

enum class BoltDispatchers {
    IO, Default, Main
}