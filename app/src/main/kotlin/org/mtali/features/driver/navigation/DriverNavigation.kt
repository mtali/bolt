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

fun NavGraphBuilder.driverScreen(locationPermissionGranted: Boolean, onClickDrawerMenu: () -> Unit) {
  composable(driverRoute) {
    DriverRoute(
      locationPermissionGranted = locationPermissionGranted,
      onClickDrawerMenu = onClickDrawerMenu,
    )
  }
}
