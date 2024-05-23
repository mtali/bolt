package org.mtali.features.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.mtali.R
import org.mtali.core.designsystem.components.boltHeader
import org.mtali.core.designsystem.components.emailField
import org.mtali.core.designsystem.components.height
import org.mtali.core.designsystem.components.passwordField
import org.mtali.core.designsystem.components.primaryButton
import org.mtali.core.utils.ELEMENT_WIDTH
import org.mtali.core.utils.capitalizeWords
import org.mtali.core.utils.handleToast


@Composable
fun SignupRoute(viewModel: SignupViewModel = hiltViewModel()) {

    val context = LocalContext.current
    viewModel.toastHandler = {
        context.handleToast(it)
    }

    val form by viewModel.form

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    SignupScreen(
        form = form,
        isLoading = isLoading,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onAttemptSignup = viewModel::onAttemptSignup,
        onNameChange = viewModel::onNameChange,
        onClose = {}
    )
}

@Composable
private fun SignupScreen(
    form: SignupForm,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAttemptSignup: () -> Unit,
    onNameChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        IconButton(onClick = onClose, modifier = Modifier.padding(8.dp)) {
            Icon(imageVector = Icons.Filled.Close, contentDescription = "close")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            height(64.dp)

            boltHeader(subtitle = R.string.need_ride)

            height(30.dp)

            nameField(
                modifier = Modifier.width(ELEMENT_WIDTH),
                name = form.name,
                onNameChange = onNameChange
            )

            height(12.dp)

            emailField(
                modifier = Modifier.width(ELEMENT_WIDTH),
                email = form.email,
                onEmailChange = onEmailChange
            )

            height(12.dp)

            passwordField(
                modifier = Modifier.width(ELEMENT_WIDTH),
                password = form.password,
                onPasswordChange = onPasswordChange
            )

            height(12.dp)

            primaryButton(
                onClick = onAttemptSignup,
                modifier = Modifier.width(ELEMENT_WIDTH),
                isLoading = isLoading
            )
        }
    }
}

private fun LazyListScope.nameField(
    modifier: Modifier = Modifier,
    name: String,
    onNameChange: (String) -> Unit
) = item {
    OutlinedTextField(
        modifier = modifier,
        value = name,
        onValueChange = {
            onNameChange(it.capitalizeWords())
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        label = {
            Text(text = stringResource(id = R.string.name))
        }
    )
}
