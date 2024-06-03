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

import org.mtali.core.data.repositories.AuthRepository
import org.mtali.core.data.repositories.SignupResult
import org.mtali.core.data.repositories.UsersRepository
import org.mtali.core.models.BoltUser
import org.mtali.core.models.ServiceResult
import javax.inject.Inject

class SignupUseCase @Inject constructor(
  private val authRepository: AuthRepository,
  private val usersRepository: UsersRepository,
) {
  suspend operator fun invoke(
    name: String,
    email: String,
    password: String,
  ): ServiceResult<SignupResult> {
    return when (val authAttempt = authRepository.signup(email, password)) {
      is ServiceResult.Failure -> authAttempt
      is ServiceResult.Value -> {
        when (authAttempt.value) {
          is SignupResult.Success -> initStreamUser(name, authAttempt.value.uid)
          else -> authAttempt
        }
      }
    }
  }

  private suspend fun initStreamUser(username: String, uid: String): ServiceResult<SignupResult> {
    return usersRepository.initSteamUser(BoltUser(userId = uid, username = username)).let { result ->
      when (result) {
        is ServiceResult.Failure -> ServiceResult.Failure(result.exception)
        is ServiceResult.Value -> ServiceResult.Value(SignupResult.Success(uid))
      }
    }
  }
}
