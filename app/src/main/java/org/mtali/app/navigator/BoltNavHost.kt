package org.mtali.app.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import org.mtali.app.ui.BoltAppState
import org.mtali.features.driver.navigation.driverScreen
import org.mtali.features.login.navigation.navigation.loginScreen
import org.mtali.features.login.navigation.navigation.navigateToLogin
import org.mtali.features.passenger.navigation.passengerScreen
import org.mtali.features.signup.navigation.navigateToSignup
import org.mtali.features.signup.navigation.signupScreen

@Composable
fun BoltNavHost(
    modifier: Modifier = Modifier,
    startDestination: String,
    appState: BoltAppState,
    onLogout: () -> Unit
) {

    val navController = appState.navController

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {

        loginScreen(onNavigateToSignup = { navController.navigateToSignup(singleTop()) })

        signupScreen(onClose = { navController.navigateToLogin(singleTop()) })

        driverScreen()

        passengerScreen()
    }
}


private fun singleTop() = navOptions { launchSingleTop = true }