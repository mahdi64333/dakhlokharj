package ir.demoodite.dakhlokharj.fragments

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.models.viewmodels.SettingsViewModel

@AndroidEntryPoint
class SettingsFragment() : PreferenceFragmentCompat() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = viewModel.settingsDataStore
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}