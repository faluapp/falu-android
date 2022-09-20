package io.falu.identity.capture

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.documents.DocumentSelectionFragment

internal abstract class AbstractCaptureFragment : Fragment() {
    protected val captureDocumentViewModel: CaptureDocumentViewModel by activityViewModels()
    protected var identityDocumentType: IdentityDocumentType? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        identityDocumentType =
            requireArguments().getSerializable(DocumentSelectionFragment.KEY_IDENTITY_DOCUMENT_TYPE) as? IdentityDocumentType
    }

    internal companion object {
        fun IdentityDocumentType.getIdentityDocumentName(context: Context) =
            context.getString(this.titleRes)
    }
}