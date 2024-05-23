package org.mtali.features.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mtali.core.data.repositories.LoginResult
import org.mtali.core.domain.LoginUseCase
import org.mtali.core.models.BoltUser
import org.mtali.core.models.ServiceResult
import org.mtali.core.models.ToastMessage
import org.mtali.core.utils.update
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _form = mutableStateOf(LoginForm())
    val form: State<LoginForm> = _form

    var toastHandler: ((ToastMessage) -> Unit)? = null

    private var loginJob: Job? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun onAttemptLogin() {
        if (loginJob?.isActive == true) return // Prevent parallel operations
        loginJob = viewModelScope.launch {
            wrapWithLoading {
                when (val attempt = loginUseCase(form.value.email, form.value.password)) {
                    is ServiceResult.Failure -> toastHandler?.invoke(ToastMessage.SERVICE_ERROR)
                    is ServiceResult.Value -> {
                        when (val result = attempt.value) {
                            LoginResult.InvalidCredentials -> toastHandler?.invoke(ToastMessage.INVALID_CREDENTIALS)
                            LoginResult.InvalidInput -> toastHandler?.invoke(ToastMessage.INVALID_INPUT)
                            is LoginResult.Success -> {
                                loginUser(result.user)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun loginUser(user: BoltUser) {
        delay(1000)
        Timber.d("User logged in")
    }

    fun onEmailChange(email: String) = _form.update { it.copy(email = email) }

    fun onPasswordChange(password: String) = _form.update { it.copy(password = password) }

    private inline fun wrapWithLoading(block: () -> Unit) {
        _isLoading.update { true }
        try {
            block()
        } finally {
            _isLoading.update { false }
        }
    }

}

data class LoginForm(val email: String = "", val password: String = "")

