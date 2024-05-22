package org.mtali.core.domain

import org.mtali.core.data.repositories.AuthRepository
import org.mtali.core.data.repositories.LoginResult
import org.mtali.core.models.ServiceResult
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): ServiceResult<LoginResult> {
        return authRepository.login(email, password)
    }
}