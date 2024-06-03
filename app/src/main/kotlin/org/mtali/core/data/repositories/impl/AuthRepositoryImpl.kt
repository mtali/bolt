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
package org.mtali.core.data.repositories.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import io.getstream.chat.android.client.ChatClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.mtali.core.data.repositories.AuthRepository
import org.mtali.core.data.repositories.LoginResult
import org.mtali.core.data.repositories.SignupResult
import org.mtali.core.dispatcher.BoltDispatchers.IO
import org.mtali.core.dispatcher.Dispatcher
import org.mtali.core.models.BoltUser
import org.mtali.core.models.ServiceResult
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
  @Dispatcher(IO) val ioDispatcher: CoroutineDispatcher,
  private val auth: FirebaseAuth,
  private val chatClient: ChatClient,
) : AuthRepository {

  /**
   * Since we use firebase and stream we need to make sure both
   * systems are in sync
   */
  override val currentUser: Flow<BoltUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      val streamUser = chatClient.getCurrentUser()
      val boltUser = if (streamUser != null) {
        auth.currentUser?.let { BoltUser(userId = it.uid) }
      } else {
        null
      }
      trySend(boltUser)
    }
    auth.addAuthStateListener(listener)
    awaitClose { auth.removeAuthStateListener(listener) }
  }

  override suspend fun signup(email: String, password: String): ServiceResult<SignupResult> =
    withContext(ioDispatcher) {
      try {
        val attempt = auth.createUserWithEmailAndPassword(email, password).await()
        auth.signOut() // Prevent automatic user sign-in
        if (attempt.user != null) {
          ServiceResult.Value(SignupResult.Success(attempt.user!!.uid))
        } else {
          ServiceResult.Failure(Exception("Null user"))
        }
      } catch (e: Exception) {
        Timber.e(e)
        when (e) {
          is FirebaseAuthWeakPasswordException -> ServiceResult.Value(SignupResult.InvalidCredentials)
          is FirebaseAuthInvalidCredentialsException -> ServiceResult.Value(SignupResult.InvalidCredentials)
          is FirebaseAuthUserCollisionException -> ServiceResult.Value(SignupResult.AlreadySignup)
          else -> ServiceResult.Failure(e)
        }
      }
    }

  override suspend fun login(email: String, password: String): ServiceResult<LoginResult> =
    withContext(ioDispatcher) {
      try {
        val attempt = auth.signInWithEmailAndPassword(email, password).await()
        if (attempt.user != null) {
          ServiceResult.Value(LoginResult.Success(BoltUser(userId = attempt.user!!.uid)))
        } else {
          ServiceResult.Failure(Exception("Null user"))
        }
      } catch (e: Exception) {
        Timber.e(e)
        when (e) {
          is FirebaseAuthInvalidUserException -> ServiceResult.Value(LoginResult.InvalidCredentials)
          is FirebaseAuthInvalidCredentialsException -> ServiceResult.Value(LoginResult.InvalidCredentials)
          is IllegalArgumentException -> ServiceResult.Value(LoginResult.InvalidInput)
          else -> ServiceResult.Failure(e)
        }
      }
    }

  override fun logout(): ServiceResult<Unit> {
    auth.signOut()
    return ServiceResult.Value(Unit)
  }

  override fun getSession(): ServiceResult<BoltUser?> {
    val firebaseUser = auth.currentUser ?: return ServiceResult.Value(null)
    return ServiceResult.Value(BoltUser(userId = firebaseUser.uid))
  }
}
