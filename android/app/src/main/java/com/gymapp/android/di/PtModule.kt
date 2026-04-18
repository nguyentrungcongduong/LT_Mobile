package com.gymapp.android.di

import com.gymapp.android.data.remote.api.PtApi
import com.gymapp.android.data.repository.PtRepositoryImpl
import com.gymapp.android.domain.repository.PtRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PtRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPtRepository(
        ptRepositoryImpl: PtRepositoryImpl
    ): PtRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PtNetworkModule {
    @Provides
    @Singleton
    fun providePtApi(retrofit: Retrofit): PtApi {
        return retrofit.create(PtApi::class.java)
    }
}
