package org.mtali.features.passenger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PassengerRoute(onLogout: () -> Unit) {
    PassengerScreen(onLogout = onLogout)
}

@Composable
private fun PassengerScreen(onLogout: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text(text = "Passenger screen")
            Button(onClick = onLogout) {
                Text(text = "logout")
            }
        }
    }
}