package com.gymapp.android.di

import com.gymapp.android.data.repository.CheckinRepositoryImpl
import com.gymapp.android.domain.repository.CheckinRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CheckinRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCheckinRepository(
        checkinRepositoryImpl: CheckinRepositoryImpl
    ): CheckinRepository
}
