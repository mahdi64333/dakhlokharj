package ir.demoodite.dakhlokharj.ui.components.languageSelection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.data.settings.enums.AppLanguage
import ir.demoodite.dakhlokharj.databinding.FragmentLanguageSelectionBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LanguageSelectionFragment :
    BaseFragment<FragmentLanguageSelectionBinding>(FragmentLanguageSelectionBinding::inflate) {
    private val viewModel: LanguageSelectionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEnglish.setOnClickListener {
            selectLanguage(AppLanguage.EN)
        }
        binding.btnPersian.setOnClickListener {
            selectLanguage(AppLanguage.FA)
        }
    }

    /**
     * Sets language of the application and returns to home fragment.
     */
    private fun selectLanguage(appLanguage: AppLanguage) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectLanguage(appLanguage)
                val action =
                    LanguageSelectionFragmentDirections.actionLanguageSelectionFragmentToHomeFragment()
                findNavController().navigate(action)
            }
        }
    }
}