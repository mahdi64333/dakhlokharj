package ir.demoodite.dakhlokharj.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * A base [BottomSheetDialogFragment] class with support for view binding.
 *
 * @param T The [ViewBinding] class of fragment's layout
 * @param inflateMethod Inflate method from the ViewBinding class of fragment's layout.
 * For example this parameter can be set with passing FragmentBinding::inflate
 * to constructor when you want to use FragmentBinding as your ViewBinding class.
 */
abstract class BaseBottomSheetDialogFragment<T : ViewBinding>(
    private val inflateMethod: (LayoutInflater, ViewGroup?, Boolean) -> T,
) : BottomSheetDialogFragment() {
    /**
     * The [ViewBinding] backing property of the fragment.
     */
    private var _binding: T? = null

    /**
     * The main [ViewBinding] object of the fragment.
     */
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = inflateMethod(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Setting the binding variable to null to prevent memory leak
        _binding = null
    }
}