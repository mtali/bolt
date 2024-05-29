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
package org.mtali.core.data.repositories

import kotlinx.coroutines.flow.Flow
import org.mtali.core.models.BoltUser
import org.mtali.core.models.ServiceResult

interface AuthRepository {
  val currentUser: Flow<BoltUser?>

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
