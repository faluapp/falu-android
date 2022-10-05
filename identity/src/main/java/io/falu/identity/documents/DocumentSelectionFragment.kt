package io.falu.identity.documents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.falu.identity.R
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.databinding.FragmentDocumentSelectionBinding

class DocumentSelectionFragment : Fragment() {
    private var _binding: FragmentDocumentSelectionBinding? = null
    private val binding get() = _binding!!
    private var identityDocumentType: IdentityDocumentType? = null

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

        binding.buttonContinue.setOnClickListener {
            val bundle = bundleOf(KEY_IDENTITY_DOCUMENT_TYPE to identityDocumentType)
            findNavController().navigate(
                R.id.action_fragment_document_selection_to_fragment_document_capture_methods,
                bundle
            )
        }

        binding.groupDocumentTypes.setOnCheckedStateChangeListener { group, _ ->
            when (group.checkedChipId) {
                R.id.chip_passport -> identityDocumentType = IdentityDocumentType.PASSPORT
                R.id.chip_identity_card -> identityDocumentType = IdentityDocumentType.IDENTITY_CARD
                R.id.chip_driving_license -> identityDocumentType =
                    IdentityDocumentType.DRIVING_LICENSE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    internal companion object {
        const val KEY_IDENTITY_DOCUMENT_TYPE = ":document-type"
    }
}