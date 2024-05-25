package org.mtali.core.data.repositories.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
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
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<BoltUser?>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                this.trySend(auth.currentUser?.let { BoltUser(userId = it.uid) })
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    override suspend fun signup(email: String, password: String): ServiceResult<SignupResult> =
        withContext(ioDispatcher) {
            try {
                val attempt = auth.createUserWithEmailAndPassword(email, password).await()
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