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
package org.mtali.features.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mtali.core.data.repositories.SignupResult
import org.mtali.core.domain.SignupUseCase
import org.mtali.core.models.ServiceResult
import org.mtali.core.models.ToastMessage
import org.mtali.core.utils.update
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
  private val signupUseCase: SignupUseCase,
) : ViewModel() {

  private val _form = mutableStateOf(SignupForm())
  val form: State<SignupForm> = _form

  var toastHandler: ((ToastMessage) -> Unit)? = null

  var successSignupHandler: (() -> Unit)? = null

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private var signupJob: Job? = null

  fun onAttemptSignup() {
    if (signupJob?.isActive == true) return
    signupJob = viewModelScope.launch {
      wrapWithLoading {
        _form.update { it.clean() }
        val attempt = signupUseCase(
          name = form.value.name,
          email = form.value.email,
          password = form.value.password,
        )
        when (attempt) {
          is ServiceResult.Failure -> toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
          is ServiceResult.Value -> {
            when (attempt.value) {
              is SignupResult.Success -> {
                toastHandler?.invoke(ToastMessage.ACCOUNT_CREATED)
                successSignupHandler?.invoke()
              }

              SignupResult.AlreadySignup -> toastHandler?.invoke(ToastMessage.ACCOUNT_EXISTS)
              SignupResult.InvalidCredentials -> toastHandler?.invoke(ToastMessage.INVALID_CREDENTIALS)
            }
          }
        }
      }
    }
  }

  fun onEmailChange(email: String) = _form.update { it.copy(email = email) }

  fun onPasswordChange(password: String) = _form.update { it.copy(password = password) }

  fun onNameChange(name: String) = _form.update { it.copy(name = name) }

  private inline fun wrapWithLoading(block: () -> Unit) {
    _isLoading.update { true }
    try {
      block()
    } finally {
      _isLoading.update { false }
    }
  }
}

data class SignupForm(val name: String = "", val email: String = "", val password: String = "")

fun SignupForm.clean(): SignupForm = copy(name = name.trim(), email = email.trim())
