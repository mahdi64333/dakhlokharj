package ir.demoodite.dakhlokharj.dependencyinjection

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.demoodite.dakhlokharj.data.DataRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideDataRepository(
        @ApplicationContext app: Context
    ): DataRepository = DataRepository.getDatabase(app)
}