package org.mtali.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.mtali.R
import org.mtali.core.designsystem.components.boltHeader
import org.mtali.core.designsystem.components.height
import org.mtali.core.utils.ELEMENT_WIDTH
import org.mtali.core.utils.handleToast


@Composable
fun LoginRoute(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToSignup: () -> Unit
) {

    val context = LocalContext.current
    viewModel.toastHandler = {
        context.handleToast(it)
    }

    val form by viewModel.form

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LoginScreen(
        form = form,
        isLoading = isLoading,
        onPasswordChange = viewModel::onPasswordChange,
        onEmailChange = viewModel::onEmailChange,
        onAttemptLogin = viewModel::onAttemptLogin,
        onNavigateToSignup = onNavigateToSignup
    )
}

@Composable
private fun LoginScreen(
    form: LoginForm,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAttemptLogin: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        height(64.dp)

        boltHeader(subtitle = R.string.need_ride)

        height(30.dp)

        emailField(
            modifier = Modifier.width(ELEMENT_WIDTH),
            email = form.email,
            onEmailChange = onEmailChange
        )

        height(6.dp)

        passwordField(
            modifier = Modifier.width(ELEMENT_WIDTH),
            password = form.password,
            onPasswordChange = onPasswordChange
        )

        height(12.dp)

        loginButton(
            onClick = onAttemptLogin,
            modifier = Modifier.width(ELEMENT_WIDTH),
            isLoading = isLoading
        )

        height(25.dp)

        signupText(onClick = onNavigateToSignup)
    }
}

private fun LazyListScope.emailField(
    modifier: Modifier = Modifier,
    email: String,
    onEmailChange: (String) -> Unit
) = item {
    OutlinedTextField(
        modifier = modifier,
        value = email,
        onValueChange = onEmailChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        label = {
            Text(text = stringResource(id = R.string.email))
        }
    )
}

private fun LazyListScope.passwordField(
    modifier: Modifier = Modifier,
    password: String,
    onPasswordChange: (String) -> Unit
) = item {

    var showPassword by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        modifier = modifier,
        value = password,
        onValueChange = onPasswordChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        label = {
            Text(text = stringResource(id = R.string.password))
        },
        trailingIcon = {
            val image = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(imageVector = image, contentDescription = null)
            }
        }
    )
}

private fun LazyListScope.loginButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onClick: () -> Unit
) = item {
    Button(
        modifier = modifier,
        onClick = {
            if (!isLoading) onClick()
        },
        shape = OutlinedTextFieldDefaults.shape
    ) {
        if (!isLoading)
            Text(text = stringResource(id = R.string.cont))
        else
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp),
                strokeWidth = 1.5.dp
            )
    }
}

private fun LazyListScope.signupText(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = item {
    TextButton(modifier = modifier, onClick = onClick) {
        Text(
            text = buildAnnotatedString {
                append(stringResource(id = R.string.no_account))
                append(" ")
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(stringResource(id = R.string.signup))
                }
                append(" ")
                append(stringResource(id = R.string.here).lowercase())
                append(".")
            },
            fontSize = 15.sp
        )
    }
}