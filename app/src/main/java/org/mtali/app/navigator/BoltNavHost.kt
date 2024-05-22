package org.mtali.app.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import org.mtali.app.ui.BoltAppState
import org.mtali.features.driver.navigation.driverScreen
import org.mtali.features.login.navigation.navigation.loginScreen
import org.mtali.features.passenger.navigation.passengerScreen
import org.mtali.features.signup.navigation.signupScreen

@Composable
fun BoltNavHost(
    modifier: Modifier = Modifier,
    startDestination: String,
    appState: BoltAppState,
    onLogout: () -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = appState.navController,
        startDestination = startDestination
    ) {

        loginScreen()

        signupScreen()

        driverScreen()

        passengerScreen()
    }
}