package org.mtali.features.passenger.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.mtali.features.passenger.PassengerRoute

const val passengerRoute = "passenger_route"


fun NavController.navigateToPassenger(navOptions: NavOptions? = null) {
    navigate(passengerRoute, navOptions = navOptions)
}


fun NavGraphBuilder.passengerScreen(onLogout: () -> Unit) {
    composable(passengerRoute) {
        PassengerRoute(onLogout = onLogout)
    }
}