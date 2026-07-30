package com.aimobile.di

import android.content.Context
import androidx.room.Room
import com.aimobile.data.local.AppDao
import com.aimobile.data.local.AppDatabase
import com.aimobile.data.local.MacroDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "aimobile_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideAppDao(db: AppDatabase): AppDao {
        return db.appDao()
    }

    @Provides
    @Singleton
    fun provideMacroDao(db: AppDatabase): MacroDao {
        return db.macroDao()
    }
}
