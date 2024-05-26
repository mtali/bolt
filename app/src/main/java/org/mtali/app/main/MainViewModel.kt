package org.mtali.app.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.mtali.core.data.repositories.AuthRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState = authRepository.currentUser
        .map { user ->
        MainUiState.Success(isLoggedIn = user != null)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState.Loading
        )

    fun onLogout() {
        authRepository.logout()
    }
}

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Success(val isLoggedIn: Boolean = false) : MainUiState
}