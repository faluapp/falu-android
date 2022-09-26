package io.falu.identity.capture.photo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.falu.identity.capture.AbstractCaptureFragment
import io.falu.identity.databinding.FragmentPhotoUploadBinding

internal class PhotoUploadFragment : AbstractCaptureFragment() {
    private var _binding: FragmentPhotoUploadBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewCameraPreview.lifecycleOwner = this

        binding.buttonCapturePhoto.setOnClickListener {
            takePhoto()
        }
    }


    private fun takePhoto() {

    }
}