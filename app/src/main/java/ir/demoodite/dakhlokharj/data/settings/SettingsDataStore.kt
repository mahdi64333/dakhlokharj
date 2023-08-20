package ir.demoodite.dakhlokharj.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException

class SettingsDataStore(private val context: Context) : PreferenceDataStore() {
    private val Context.settingsDataStore by preferencesDataStore(name = DATASTORE_NAME)

    /*
    * The putString and getString methods have to be overridden in order to use PreferenceDataStore
    * within a PreferenceScreen.
    */
    override fun putString(key: String?, value: String?) {
        runBlocking {
            value?.let {
                when (key) {
                    LANGUAGE_PREFERENCE_KEY.name -> {
                        setLanguage(value)
                    }
                    ORDER_BY_PREFERENCE_KEY.name -> {
                        setOrderBy(value)
                    }
                    else -> return@let
                }
            }
        }
    }

    override fun getString(key: String?, defValue: String?): String? {
        return runBlocking {
            when (key) {
                LANGUAGE_KEY -> getLanguageFlow().first()
                ORDER_BY_KEY -> getOrderByFlow().first()
                else -> defValue
            }
        }
    }

    suspend fun setLanguage(language: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[LANGUAGE_PREFERENCE_KEY] = language
        }
    }

    fun getLanguageFlow(): Flow<String> {
        return context.settingsDataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[LANGUAGE_PREFERENCE_KEY] ?: ""
        }
    }


    suspend fun setOrderBy(orderBy: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[ORDER_BY_PREFERENCE_KEY] = orderBy
        }
    }

    fun getOrderByFlow(): Flow<String> {
        return context.settingsDataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[ORDER_BY_PREFERENCE_KEY] ?: "TIME_DESC"
        }
    }


    suspend fun setCurrentDbAlias(currentDbAlias: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[CURRENT_DB_ALIAS_PREFERENCE_KEY] = currentDbAlias
        }
    }

    fun getCurrentDbAliasFlow(): Flow<String> {
        return context.settingsDataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[CURRENT_DB_ALIAS_PREFERENCE_KEY] ?: ""
        }
    }

    companion object {
        private const val DATASTORE_NAME = "settings"
        const val LANGUAGE_KEY = "language"
        const val ORDER_BY_KEY = "order_by"
        private const val CURRENT_DB_ALIAS_KEY = "CURRENT_DB_ALIAS"
        private val LANGUAGE_PREFERENCE_KEY = stringPreferencesKey(LANGUAGE_KEY)
        private val ORDER_BY_PREFERENCE_KEY = stringPreferencesKey(ORDER_BY_KEY)
        private val CURRENT_DB_ALIAS_PREFERENCE_KEY = stringPreferencesKey(CURRENT_DB_ALIAS_KEY)
    }
}