package io.falu.identity.documents

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.camera.CameraPermissionsFragment
import io.falu.identity.databinding.FragmentDocumentCaptureMethodsBinding
import io.falu.identity.utils.updateVerification
import software.tingle.api.patch.JsonPatchDocument

internal class DocumentCaptureMethodsFragment : CameraPermissionsFragment() {
    private var _binding: FragmentDocumentCaptureMethodsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IdentityVerificationViewModel by activityViewModels()
    private lateinit var identityDocumentType: IdentityDocumentType

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentCaptureMethodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        identityDocumentType =
            (requireArguments().getSerializable(DocumentSelectionFragment.KEY_IDENTITY_DOCUMENT_TYPE) as IdentityDocumentType?)!!

        binding.tvDocumentCaptureMethod.text =
            getString(
                R.string.document_capture_method_subtitle,
                identityDocumentType.getIdentityDocumentName(requireContext())
            )

        binding.viewCaptureMethodScan.setOnClickListener {
            checkCameraPermissions(identityDocumentType.toUploadDestination())
        }

        binding.viewCaptureMethodPhoto.setOnClickListener {
            checkCameraPermissions(identityDocumentType.toPhotoUploadDestination())
        }

        binding.viewCaptureMethodUpload.setOnClickListener {
            navigateToDestination(identityDocumentType.toUploadDestination())
        }

        viewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onSuccess = { onVerificationSuccessful(it) },
            onError = {}
        )
    }

    /**
     *
     */
    private fun navigateToDestination(@IdRes destinationId: Int) {
        val bundle =
            bundleOf(DocumentSelectionFragment.KEY_IDENTITY_DOCUMENT_TYPE to identityDocumentType)
        findNavController().navigate(destinationId, bundle)
    }

    /**
     *
     */
    private fun onCameraPermissionGranted(@IdRes destinationId: Int) {
        navigateToDestination(destinationId)
    }

    /**
     *
     */
    private fun onCameraPermissionDenied() {
        // navigate to camera permissions denied view
    }

    /**
     *
     */
    private fun checkCameraPermissions(@IdRes destinationId: Int) {
        requestCameraPermissions(
            onCameraPermissionGranted = { onCameraPermissionGranted(destinationId) },
            onCameraPermissionDenied = { onCameraPermissionDenied() }
        )
    }

    /**
     *
     */
    private fun onVerificationSuccessful(verification: Verification) {
        val allowUploads = verification.options.allowUploads
        binding.viewCaptureMethodUpload.visibility =
            if (allowUploads) View.VISIBLE else View.VISIBLE
    }

    internal companion object {
        @IdRes
        private fun IdentityDocumentType.toUploadDestination() =
            when (this) {
                IdentityDocumentType.IDENTITY_CARD -> R.id.action_fragment_document_capture_methods_to_fragment_document_upload
                IdentityDocumentType.PASSPORT -> R.id.action_fragment_document_capture_methods_to_fragment_document_upload
                IdentityDocumentType.DRIVING_LICENSE -> R.id.action_fragment_document_capture_methods_to_fragment_document_upload
            }

        @IdRes
        private fun IdentityDocumentType.toPhotoUploadDestination() =
            when (this) {
                IdentityDocumentType.IDENTITY_CARD -> R.id.action_fragment_document_capture_methods_to_fragment_photo_upload
                IdentityDocumentType.PASSPORT -> R.id.action_fragment_document_capture_methods_to_fragment_photo_upload
                IdentityDocumentType.DRIVING_LICENSE -> R.id.action_fragment_document_capture_methods_to_fragment_photo_upload
            }

        fun IdentityDocumentType.getIdentityDocumentName(context: Context) =
            context.getString(this.titleRes)
    }
}