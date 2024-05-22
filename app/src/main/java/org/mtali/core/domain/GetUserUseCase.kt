package org.mtali.core.domain

import org.mtali.core.data.repositories.AuthRepository
import org.mtali.core.models.BoltUser
import org.mtali.core.models.ServiceResult
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): ServiceResult<BoltUser?> {
        return authRepository.getSession()
    }
}