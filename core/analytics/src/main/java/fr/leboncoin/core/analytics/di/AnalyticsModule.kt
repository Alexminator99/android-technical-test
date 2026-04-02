package fr.leboncoin.core.analytics.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.leboncoin.core.analytics.AnalyticsHelper
import fr.leboncoin.core.analytics.TimberAnalyticsHelper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsHelper(impl: TimberAnalyticsHelper): AnalyticsHelper
}
