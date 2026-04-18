package com.gymapp.android.di

import com.gymapp.android.data.remote.api.MembershipApi
import com.gymapp.android.data.repository.MembershipRepositoryImpl
import com.gymapp.android.domain.repository.MembershipRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MembershipRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMembershipRepository(
        membershipRepositoryImpl: MembershipRepositoryImpl
    ): MembershipRepository
}

@Module
@InstallIn(SingletonComponent::class)
object MembershipApiModule {

    @Provides
    @Singleton
    fun provideMembershipApi(retrofit: Retrofit): MembershipApi {
        return retrofit.create(MembershipApi::class.java)
    }
}
