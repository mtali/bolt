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
import org.mtali.core.models.BoltUser
import org.mtali.core.models.ServiceResult
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
  private val authRepository: AuthRepository,
) {
  operator fun invoke(): ServiceResult<BoltUser?> {
    return authRepository.getSession()
  }
}
