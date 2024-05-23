package org.mtali.features.signup.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.mtali.features.signup.SignupRoute

const val signupRoute = "signup_route"

fun NavController.navigateToSignup(navOptions: NavOptions? = null) {
    navigate(signupRoute, navOptions = navOptions)
}

fun NavGraphBuilder.signupScreen(onClose: () -> Unit) {
    composable(signupRoute) {
        SignupRoute(onClose = onClose)
    }
}