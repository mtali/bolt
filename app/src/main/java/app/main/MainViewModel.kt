package app.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val isLoggedIn = MutableStateFlow(false)

    val uiState = isLoggedIn.map {
        MainUiState.Success(isLoggedIn = it)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState.Loading
        )


    fun onLogout() {

    }
}

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Success(val isLoggedIn: Boolean = false) : MainUiState
}