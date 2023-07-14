package ir.demoodite.dakhlokharj.fragments

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.SettingsDataStore
import ir.demoodite.dakhlokharj.enums.AppLanguage
import ir.demoodite.dakhlokharj.enums.OrderBy
import ir.demoodite.dakhlokharj.models.viewmodels.SettingsViewModel

@AndroidEntryPoint
class SettingsFragment() : PreferenceFragmentCompat() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = viewModel.settingsDataStore
        setPreferencesFromResource(R.xml.settings, rootKey)
        findPreference<ListPreference>(SettingsDataStore.LANGUAGE_KEY.name)?.let {
            it.entries = AppLanguage.values().map { appLanguage ->
                getString(appLanguage.stringRes)
            }.toTypedArray()
            it.entryValues = AppLanguage.values().map { appLanguage ->
                appLanguage.name
            }.toTypedArray()
        }
        findPreference<ListPreference>(SettingsDataStore.ORDER_BY_KEY.name)?.let {
            it.entries = OrderBy.values().map { orderBy ->
                getString(orderBy.stringRes)
            }.toTypedArray()
            it.entryValues = OrderBy.values().map { orderBy ->
                orderBy.name
            }.toTypedArray()
        }
    }
}