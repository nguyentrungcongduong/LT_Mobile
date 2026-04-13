package com.gymapp.android.di

import com.gymapp.android.data.remote.api.PaymentApi
import com.gymapp.android.data.repository.PaymentRepositoryImpl
import com.gymapp.android.domain.repository.PaymentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentModule {

    @Provides
    @Singleton
    fun providePaymentRepository(api: PaymentApi): PaymentRepository {
        return PaymentRepositoryImpl(api)
    }
}
