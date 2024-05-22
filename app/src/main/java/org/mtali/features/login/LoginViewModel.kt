package org.mtali.features.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.mtali.core.domain.LoginUseCase
import org.mtali.core.utils.update
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _form = mutableStateOf(LoginForm())
    val form: State<LoginForm> = _form

    fun onAttemptLogin() {

    }

    fun onEmailChange(email: String) = _form.update { it.copy(email = email) }

    fun onPasswordChange(password: String) = _form.update { it.copy(password = password) }

}

data class LoginForm(val email: String = "", val password: String = "")

