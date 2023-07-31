package ir.demoodite.dakhlokharj.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.settings.SettingsDataStore
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    const val FILES_DIR_PROVIDER = "FILES_DIR_PROVIDER"

    @Singleton
    @Provides
    fun provideDataRepository(
        @ApplicationContext app: Context,
    ): DataRepository = DataRepository.getDatabase(app)

    @Provides
    fun provideDatabaseImporter(
        @ApplicationContext app: Context,
    ): DataRepository.Companion.DatabaseImporter = DataRepository.Companion.DatabaseImporter(app)

    @Singleton
    @Provides
    fun provideSettingsDataStore(
        @ApplicationContext app: Context,
    ): SettingsDataStore = SettingsDataStore(app)

    @Singleton
    @Provides
    fun provideWorkManager(
        @ApplicationContext app: Context,
    ): WorkManager = WorkManager.getInstance(app)

    @Singleton
    @Provides
    @Named(FILES_DIR_PROVIDER)
    fun providesFilesDir(
        @ApplicationContext app: Context,
    ): File = app.filesDir
}