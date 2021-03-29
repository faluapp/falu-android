package io.falu.android.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * [The payment request object](https://falu.io)
 */
data class PaymentRequest(
    /**
     * Amount of the payment in smallest currency unit.
     */
    var amount: Int,

    /**
     * Three-letter ISO currency code, in lowercase.
     */
    var currency: String = "kes",

    /**
     * Represents the provider details for a MPESA payment
     */
    var mpesa: PaymentInitiationMpesa? = null,
) : FaluModel()


/**
 * [Mpesa payment initiation object](https://falu.io)
 */
class PaymentInitiationMpesa {
    /**
     * Phone number representing the account to be charged, in E.164 format.
     */
    var phone: String = ""

    /**
     * The reference that the payment will be made in. This can be an account number.
     */
    var reference: String = ""

    /**
     * The kind of mpesa STK push to be sent out
     */
    var kind: MpesaStkPushTransactionType = MpesaStkPushTransactionType.CUSTOMER_BUYS_GOODS_ONLINE

    /**
     * The shortcode of the receiver. When not provided, it defaults to the default recipient.
     * When not provided, either the default incoming business code or the first business code for the workspace is used depending on the kind.
     * This value is usually required and different from the business short code when using BuyGood
     */
    var destination: String? = null
}

enum class MpesaStkPushTransactionType {
    @SerializedName("customerBuyGoodsOnline")
    CUSTOMER_BUYS_GOODS_ONLINE,

    @SerializedName("customerPayBillOnline")
    CUSTOMER_PAYS_BILL_ONLINE,
}
