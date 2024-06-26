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
package org.mtali.core.domain

import org.mtali.core.data.repositories.FirebaseAuthRepository
import org.mtali.core.data.repositories.LoginResult
import org.mtali.core.data.repositories.StreamUserRepository
import org.mtali.core.models.ServiceResult
import javax.inject.Inject

class LoginUseCase @Inject constructor(
  private val firebaseAuthRepository: FirebaseAuthRepository,
  private val streamUserRepository: StreamUserRepository,
) {
  suspend operator fun invoke(email: String, password: String): ServiceResult<LoginResult> {
    return when (val attempt = firebaseAuthRepository.login(email, password)) {
      is ServiceResult.Failure -> attempt
      is ServiceResult.Value -> {
        when (attempt.value) {
          is LoginResult.Success -> getStreamUser(attempt.value.user.userId)
          else -> attempt
        }
      }
    }
  }

  private suspend fun getStreamUser(uid: String): ServiceResult<LoginResult> {
    return streamUserRepository.getStreamUserById(uid).let { result ->
      when (result) {
        is ServiceResult.Failure -> ServiceResult.Failure(result.exception)
        is ServiceResult.Value -> {
          if (result.value == null) {
            ServiceResult.Failure(Exception("Null user when login"))
          } else {
            ServiceResult.Value(LoginResult.Success(result.value))
          }
        }
      }
    }
  }
}
