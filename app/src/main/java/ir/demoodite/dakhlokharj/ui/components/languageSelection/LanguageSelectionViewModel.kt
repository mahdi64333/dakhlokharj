package ir.demoodite.dakhlokharj.ui.components.languageSelection

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.settings.SettingsDataStore
import ir.demoodite.dakhlokharj.data.settings.enums.AppLanguage
import javax.inject.Inject

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {
    /**
     * Sets language of the application.
     */
    suspend fun selectLanguage(appLanguage: AppLanguage) {
        settingsDataStore.setLanguage(appLanguage.name)
    }
}