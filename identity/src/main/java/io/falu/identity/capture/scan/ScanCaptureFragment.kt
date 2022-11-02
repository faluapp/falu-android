package io.falu.identity.capture.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.capture.AbstractCaptureFragment
import io.falu.identity.databinding.FragmentScanCaptureBinding

internal class ScanCaptureFragment : AbstractCaptureFragment() {
    private var _binding: FragmentScanCaptureBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showDocumentFrontUploading() {
    }

    override fun showDocumentBackUploading() {
    }

    override fun showDocumentFrontDoneUploading(disposition: DocumentUploadDisposition) {
    }

    override fun showDocumentBackDoneUploading() {
    }

    override fun showBothSidesUploaded(disposition: DocumentUploadDisposition) {
    }

    override fun resetViews(documentSide: DocumentSide) {
    }
}