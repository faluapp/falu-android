package io.falu.identity.documents

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.analytics.AnalyticsDisposition
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.camera.CameraPermissionsFragment
import io.falu.identity.databinding.FragmentDocumentCaptureMethodsBinding

internal class DocumentCaptureMethodsFragment(private val factory: ViewModelProvider.Factory) :
    CameraPermissionsFragment() {

    private var _binding: FragmentDocumentCaptureMethodsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IdentityVerificationViewModel by activityViewModels { factory }
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
            (requireArguments().getSerializable(DocumentSelectionFragment.KEY_IDENTITY_DOCUMENT_TYPE)
                    as IdentityDocumentType?)!!

        binding.tvDocumentCaptureMethod.text =
            getString(
                R.string.document_capture_method_subtitle,
                identityDocumentType.getIdentityDocumentName(requireContext())
            )

        binding.viewCaptureMethodScan.setOnClickListener {
            viewModel.resetDocumentUploadDisposition()
            reportUploadMethodTelemetry(UploadMethod.AUTO)
            checkCameraPermissions(identityDocumentType.toScanCaptureDestination())
        }

        binding.viewCaptureMethodPhoto.setOnClickListener {
            viewModel.resetDocumentUploadDisposition()
            reportUploadMethodTelemetry(UploadMethod.MANUAL)
            checkCameraPermissions(identityDocumentType.toManualCaptureDestination())
        }

        binding.viewCaptureMethodUpload.setOnClickListener {
            viewModel.resetDocumentUploadDisposition()
            reportUploadMethodTelemetry(UploadMethod.UPLOAD)
            navigateToDestination(identityDocumentType.toUploadCaptureDestination())
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
    private fun reportUploadMethodTelemetry(uploadMethod: UploadMethod) {
        viewModel.modifyAnalyticsDisposition(disposition = AnalyticsDisposition(uploadMethod = uploadMethod))
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
        viewModel.reportTelemetry(viewModel.analyticsRequestBuilder.cameraPermissionGranted(identityDocumentType))
    }

    /**
     *
     */
    private fun onCameraPermissionDenied() {
        viewModel.reportTelemetry(viewModel.analyticsRequestBuilder.cameraPermissionDenied(identityDocumentType))
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
            if (allowUploads) View.VISIBLE else View.GONE
    }

    internal companion object {
        @IdRes
        private fun IdentityDocumentType.toUploadCaptureDestination() =
            when (this) {
                IdentityDocumentType.IDENTITY_CARD,
                IdentityDocumentType.PASSPORT,
                IdentityDocumentType.DRIVING_LICENSE ->
                    R.id.action_fragment_document_capture_methods_to_fragment_upload_capture
            }

        @IdRes
        private fun IdentityDocumentType.toManualCaptureDestination() =
            when (this) {
                IdentityDocumentType.IDENTITY_CARD,
                IdentityDocumentType.PASSPORT,
                IdentityDocumentType.DRIVING_LICENSE ->
                    R.id.action_fragment_document_capture_methods_to_fragment_manual_capture
            }

        private fun IdentityDocumentType.toScanCaptureDestination() =
            when (this) {
                IdentityDocumentType.IDENTITY_CARD,
                IdentityDocumentType.PASSPORT,
                IdentityDocumentType.DRIVING_LICENSE ->
                    R.id.action_fragment_document_capture_methods_to_fragment_scan_capture_side
            }

        fun IdentityDocumentType.getIdentityDocumentName(context: Context) =
            context.getString(this.titleRes)
    }
}