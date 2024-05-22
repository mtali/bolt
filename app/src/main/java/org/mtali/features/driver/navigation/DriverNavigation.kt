package org.mtali.features.driver.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.mtali.features.driver.DriverRoute

const val driverRoute = "driver_route"


fun NavController.navigateToDriver(navOptions: NavOptions? = null) {
    navigate(driverRoute, navOptions = navOptions)
}


fun NavGraphBuilder.driverScreen() {
    composable(driverRoute) {
        DriverRoute()
    }
}