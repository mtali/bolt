package org.mtali.core.data.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.mtali.core.data.repositories.AuthRepository
import org.mtali.core.data.repositories.impl.AuthRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindsAuthRepo(repo: AuthRepositoryImpl): AuthRepository

    companion object {
        @Provides
        fun providesFirebaseAuth(): FirebaseAuth = Firebase.auth
    }
}