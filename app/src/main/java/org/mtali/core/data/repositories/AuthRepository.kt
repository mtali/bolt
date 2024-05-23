package org.mtali.core.data.repositories

import org.mtali.core.models.BoltUser
import org.mtali.core.models.ServiceResult

interface AuthRepository {
    suspend fun signup(email: String, password: String): ServiceResult<SignupResult>
    suspend fun login(email: String, password: String): ServiceResult<LoginResult>
    fun logout(): ServiceResult<Unit>

    /**
     * Will return null if no active user session
     */
    fun getSession(): ServiceResult<BoltUser?>
}

sealed interface SignupResult {
    data class Success(val uid: String) : SignupResult
    data object AlreadySignup : SignupResult
    data object InvalidCredentials : SignupResult
}

sealed interface LoginResult {
    data class Success(val user: BoltUser) : LoginResult
    data object InvalidCredentials : LoginResult
    data object InvalidInput : LoginResult
}