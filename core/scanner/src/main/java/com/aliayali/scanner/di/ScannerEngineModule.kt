package com.aliayali.scanner.di

import com.aliayali.domain.repository.HttpClient
import com.aliayali.domain.repository.SecurityCheck
import com.aliayali.scanner.DirectoryListingCheck
import com.aliayali.scanner.ScannerEngine
import com.aliayali.scanner.SecurityHeadersCheck
import com.aliayali.scanner.paths.CommonPaths
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScannerEngineModule {

    @Provides
    @Singleton
    fun provideSecurityChecks(): List<SecurityCheck> =
        listOf(
            SecurityHeadersCheck(),
            DirectoryListingCheck(),
        )

    @Provides
    @Singleton
    fun provideScannerEngine(
        httpClient: HttpClient,
        checks: List<@JvmSuppressWildcards SecurityCheck>,
        commonPaths: CommonPaths,
    ): ScannerEngine =
        ScannerEngine(
            httpClient = httpClient,
            checks = checks,
            commonPaths = commonPaths,
        )
}