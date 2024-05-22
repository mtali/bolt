package features.login

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


@Composable
fun LoginRoute() {
    LoginScreen()
}

@Composable
private fun LoginScreen() {
    Text(text = "Login screen")
}