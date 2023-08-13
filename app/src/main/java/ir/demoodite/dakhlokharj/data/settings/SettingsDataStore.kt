package ir.demoodite.dakhlokharj.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
    private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val ORDER_BY_KEY = stringPreferencesKey("order_by")
        val CURRENT_DB_ALIAS = stringPreferencesKey("current_db_alias")
    }

    override fun putString(key: String?, value: String?) {
        runBlocking {
            value?.let {
                when (key) {
                    LANGUAGE_KEY.name -> {
                        setLanguage(value)
                    }
                    ORDER_BY_KEY.name -> {
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
                LANGUAGE_KEY.name -> getLanguageFlow().first()
                ORDER_BY_KEY.name -> getOrderByFlow().first()
                else -> defValue
            }
        }
    }

    suspend fun setLanguage(language: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    fun getLanguageFlow(): Flow<String> {
        return context.settingsDataStore.data
            .catch {
                if (it is IOException) {
                    it.printStackTrace()
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map { preferences ->
                preferences[LANGUAGE_KEY] ?: ""
            }
    }


    suspend fun setOrderBy(orderBy: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[ORDER_BY_KEY] = orderBy
        }
    }

    fun getOrderByFlow(): Flow<String> {
        return context.settingsDataStore.data
            .catch {
                if (it is IOException) {
                    it.printStackTrace()
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map { preferences ->
                preferences[ORDER_BY_KEY] ?: "TIME_DESC"
            }
    }


    suspend fun setCurrentDbAlias(currentDbAlias: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[CURRENT_DB_ALIAS] = currentDbAlias
        }
    }

    fun getCurrentDbAliasFlow(): Flow<String> {
        return context.settingsDataStore.data
            .catch {
                if (it is IOException) {
                    it.printStackTrace()
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map { preferences ->
                preferences[CURRENT_DB_ALIAS] ?: ""
            }
    }
}