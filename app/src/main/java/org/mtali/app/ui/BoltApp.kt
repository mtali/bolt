package org.mtali.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.mtali.R
import org.mtali.app.main.MainUiState
import org.mtali.app.navigator.BoltNavHost
import org.mtali.features.login.navigation.navigation.loginRoute
import org.mtali.features.passenger.navigation.passengerRoute

@Composable
fun BoltApp(
    appState: BoltAppState,
    uiState: MainUiState,
    onLogout: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal,
                    ),
                )
        ) {
            when (uiState) {
                MainUiState.Loading -> {
                    LoadingPage()
                }

                is MainUiState.Success -> {
                    BoltNavHost(
                        startDestination = if (uiState.isLoggedIn) passengerRoute else loginRoute,
                        appState = appState,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingPage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(id = R.string.loading))
    }
}