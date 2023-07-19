package ir.demoodite.dakhlokharj.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.settings.SettingsDataStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDataRepository(
        @ApplicationContext app: Context,
    ): DataRepository = DataRepository.getDatabase(app)

    @Singleton
    @Provides
    fun provideSettingsDataStore(
        @ApplicationContext app: Context,
    ): SettingsDataStore = SettingsDataStore(app.applicationContext)
}