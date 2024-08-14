package io.falu.identity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.bundleOf
import io.falu.identity.utils.parcelable
import kotlinx.parcelize.Parcelize

internal class IdentityVerificationViewContract :
    ActivityResultContract<ContractArgs, IdentityVerificationResult>() {
    override fun createIntent(context: Context, input: ContractArgs): Intent {
        return Intent(context, IdentityVerificationActivity::class.java)
            .putExtras(input.contractArgsBundle)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): IdentityVerificationResult {
        return IdentityVerificationResult.getFromIntent(intent)
    }
}

@Parcelize
internal data class ContractArgs(
    var temporaryKey: String,
    var verificationId: String,
    var maxNetworkRetries: Int,
    var workspaceLogo: Uri
) : Parcelable {

    val contractArgsBundle: Bundle
        get() = bundleOf(KEY_CONTRACT_ARGS to this)

    companion object {
        private const val KEY_CONTRACT_ARGS = ":contract_args"

        fun getFromIntent(intent: Intent): ContractArgs? {
            return intent.parcelable(KEY_CONTRACT_ARGS)
        }

        fun getFromBundle(bundle: Bundle): ContractArgs? {
            return bundle.parcelable(KEY_CONTRACT_ARGS)
        }
    }
}