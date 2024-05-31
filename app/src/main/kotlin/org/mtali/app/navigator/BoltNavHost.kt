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
package org.mtali.app.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import org.mtali.app.ui.BoltAppState
import org.mtali.features.driver.navigation.driverScreen
import org.mtali.features.login.navigation.navigation.loginRoute
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
  onLogout: () -> Unit,
  locationPermissionGranted: Boolean,
) {
  val navController = appState.navController

  NavHost(
    modifier = modifier,
    navController = navController,
    startDestination = startDestination,
  ) {
    loginScreen(onNavigateToSignup = { navController.navigateToSignup(singleTop()) })

    signupScreen(
      onClose = { navController.navigateToLogin(popUp(loginRoute, inclusive = false)) },
      onSignupSuccess = { navController.navigateToLogin(clear()) },
    ) // TODO: restore state when navigating to login

    driverScreen(locationPermissionGranted = locationPermissionGranted)

    passengerScreen(onLogout = onLogout, locationPermissionGranted = locationPermissionGranted)
  }
}

private fun singleTop() = navOptions { launchSingleTop = true }

private fun clear(inclusive: Boolean = true) = navOptions {
  launchSingleTop = true
  popUpTo(0) { this.inclusive = inclusive }
}

private fun popUp(popUp: String, inclusive: Boolean = true) = navOptions {
  launchSingleTop = true
  popUpTo(popUp) { this.inclusive = inclusive }
}
