package org.mtali.core.domain

import org.mtali.core.data.repositories.AuthRepository
import org.mtali.core.data.repositories.SignupResult
import org.mtali.core.models.ServiceResult
import javax.inject.Inject

class SignupUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String
    ): ServiceResult<SignupResult> {
        return authRepository.signup(email, password)
    }
}