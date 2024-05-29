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
package org.mtali.core.designsystem.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.mtali.R

fun LazyListScope.emailField(
  modifier: Modifier = Modifier,
  email: String,
  onEmailChange: (String) -> Unit,
) = item {
  OutlinedTextField(
    modifier = modifier,
    value = email,
    onValueChange = onEmailChange,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    label = {
      Text(text = stringResource(id = R.string.email))
    },
  )
}

fun LazyListScope.passwordField(
  modifier: Modifier = Modifier,
  password: String,
  onPasswordChange: (String) -> Unit,
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
    },
  )
}

fun LazyListScope.primaryButton(
  modifier: Modifier = Modifier,
  @StringRes title: Int = R.string.cont,
  isLoading: Boolean,
  onClick: () -> Unit,
) = item {
  Button(
    modifier = modifier,
    onClick = { if (!isLoading) onClick() },
    shape = OutlinedTextFieldDefaults.shape,
  ) {
    if (!isLoading) {
      Text(text = stringResource(id = title))
    } else {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.size(25.dp),
        strokeWidth = 1.5.dp,
      )
    }
  }
}
