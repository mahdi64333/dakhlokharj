package ir.demoodite.dakhlokharj.ui.components.settings

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.settings.SettingsDataStore
import ir.demoodite.dakhlokharj.data.settings.enums.AppLanguage
import ir.demoodite.dakhlokharj.data.settings.enums.PurchasesOrderBy

@AndroidEntryPoint
class SettingsFragment() : PreferenceFragmentCompat() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = viewModel.settingsDataStore
        setPreferencesFromResource(R.xml.settings, rootKey)

        // Setting up the language preference from AppLanguage enum
        findPreference<ListPreference>(SettingsDataStore.LANGUAGE_KEY)?.let {
            it.entries = AppLanguage.values().map { appLanguage ->
                getString(appLanguage.stringRes)
            }.toTypedArray()
            it.entryValues = AppLanguage.values().map { appLanguage ->
                appLanguage.name
            }.toTypedArray()
        }

        // Setting up the language preference from AppLanguage enum
        findPreference<ListPreference>(SettingsDataStore.ORDER_BY_KEY)?.let {
            it.entries = PurchasesOrderBy.values().map { orderBy ->
                getString(orderBy.stringRes)
            }.toTypedArray()
            it.entryValues = PurchasesOrderBy.values().map { orderBy ->
                orderBy.name
            }.toTypedArray()
        }

        // Setting up the database manager preference to launch DatabaseManager fragment destination
        findPreference<Preference>("database_manager")?.let {
            it.setOnPreferenceClickListener {
                val action =
                    SettingsFragmentDirections.actionSettingsFragmentToDatabaseManagerFragment()
                findNavController().navigate(action)
                true
            }
        }
    }
}