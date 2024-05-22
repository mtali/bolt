package org.mtali.features.login.navigation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.mtali.features.login.LoginRoute


const val loginRoute = "login_route"

fun NavController.navigateToLogin(navOptions: NavOptions? = null) {
    navigate(loginRoute, navOptions = navOptions)
}

fun NavGraphBuilder.loginScreen(onNavigateToSignup: () -> Unit) {
    composable(loginRoute) {
        LoginRoute(onNavigateToSignup = onNavigateToSignup)
    }
}