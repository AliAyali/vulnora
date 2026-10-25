package com.aliayali.scanner.di

import com.aliayali.domain.repository.ScanRepository
import com.aliayali.scanner.repository.ScanRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScanRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindScanRepository(
        implementation: ScanRepositoryImpl,
    ): ScanRepository
}
