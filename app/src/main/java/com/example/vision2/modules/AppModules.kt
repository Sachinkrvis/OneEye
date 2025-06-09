package com.example.vision2.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class appModules{

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://visionserver-ehve.onrender.com/api/")
            .build()

    }
    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): LocationCall {
        return retrofit.create(LocationCall::class.java)
    }
}