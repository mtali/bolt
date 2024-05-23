package org.mtali.features.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mtali.core.models.ToastMessage
import org.mtali.core.utils.update
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor() : ViewModel() {

    private val _form = mutableStateOf(SignupForm())
    val form: State<SignupForm> = _form

    var toastHandler: ((ToastMessage) -> Unit)? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun onAttemptSignup() {

    }

    fun onEmailChange(email: String) = _form.update { it.copy(email = email) }

    fun onPasswordChange(password: String) = _form.update { it.copy(password = password) }

    fun onNameChange(name: String) = _form.update { it.copy(name = name) }


}

data class SignupForm(val name: String = "", val email: String = "", val password: String = "")