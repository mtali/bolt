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
package org.mtali.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.mtali.R
import org.mtali.core.designsystem.components.boltHeader
import org.mtali.core.designsystem.components.emailField
import org.mtali.core.designsystem.components.height
import org.mtali.core.designsystem.components.passwordField
import org.mtali.core.designsystem.components.primaryButton
import org.mtali.core.utils.ELEMENT_WIDTH
import org.mtali.core.utils.handleToast

@Composable
fun LoginRoute(
  viewModel: LoginViewModel = hiltViewModel(),
  onNavigateToSignup: () -> Unit,
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
    onNavigateToSignup = onNavigateToSignup,
  )
}

@Composable
private fun LoginScreen(
  form: LoginForm,
  isLoading: Boolean,
  onEmailChange: (String) -> Unit,
  onPasswordChange: (String) -> Unit,
  onAttemptLogin: () -> Unit,
  onNavigateToSignup: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top,
  ) {
    height(64.dp)

    boltHeader(subtitle = R.string.need_ride)

    height(30.dp)

    emailField(
      modifier = Modifier.width(ELEMENT_WIDTH),
      email = form.email,
      onEmailChange = onEmailChange,
    )

    height(6.dp)

    passwordField(
      modifier = Modifier.width(ELEMENT_WIDTH),
      password = form.password,
      onPasswordChange = onPasswordChange,
    )

    height(12.dp)

    primaryButton(
      onClick = onAttemptLogin,
      modifier = Modifier.width(ELEMENT_WIDTH),
      isLoading = isLoading,
    )

    height(25.dp)

    signupText(onClick = onNavigateToSignup)
  }
}

private fun LazyListScope.signupText(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) = item {
  TextButton(modifier = modifier, onClick = onClick) {
    Text(
      text = buildAnnotatedString {
        append(stringResource(id = R.string.no_account))
        append(" ")
        withStyle(
          SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
          ),
        ) {
          append(stringResource(id = R.string.signup))
        }
        append(" ")
        append(stringResource(id = R.string.here).lowercase())
        append(".")
      },
      fontSize = 15.sp,
    )
  }
}
