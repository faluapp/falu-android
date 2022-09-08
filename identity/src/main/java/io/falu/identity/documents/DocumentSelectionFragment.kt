package io.falu.identity.documents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.falu.identity.databinding.FragmentDocumentSelectionBinding

class DocumentSelectionFragment : Fragment() {
    private var _binding: FragmentDocumentSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    internal companion object {
        const val ALLOWED_TYPE_DRIVING_LICENSE = "driving_license"
        const val ALLOWED_TYPE_PASSPORT = "passport"
        const val ALLOWED_TYPE_ID_CARD = "id_card"
    }
}