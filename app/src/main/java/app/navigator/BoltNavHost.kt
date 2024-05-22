package app.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import app.ui.BoltAppState
import features.driver.navigation.driverScreen
import features.login.navigation.navigation.loginScreen
import features.passenger.navigation.passengerScreen
import features.signup.navigation.signupScreen

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