package io.falu.android.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * [The payment request object](https://falu.io)
 */
class PaymentRequest(
    /**
     * Amount of the payment in smallest currency unit.
     */
    amount: Int,
    /**
     * Three-letter ISO currency code, in lowercase.
     */
    var currency: String,
    /**
     * Represents the provider details for a MPESA payment
     */
    var mpesa: PaymentInitiationMpesa? = null
) : FaluModel() {

    private var amount: Int = 0

    init {
        this.amount = Money(
            amount,
            Currency.getInstance(currency.toUpperCase(Locale.getDefault()))
        ).toCents
    }

}


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
    @Deprecated("paybill property is favored instead")
    var kind: MpesaStkPushTransactionType = MpesaStkPushTransactionType.CUSTOMER_BUYS_GOODS_ONLINE

    /**
     * Set true if the payment to be initiated is to be made to a paybill; false, the payment is made to a BuyGoods till
     */
    var paybill: Boolean = false

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

enum class PaymentStatus {
    @SerializedName("pending")
    PENDING,

    @SerializedName("succeeded")
    SUCCEEDED,

    @SerializedName("failed")
    FAILED
}


enum class PaymentType {
    @SerializedName("mpesa")
    MPESA,

    @SerializedName("airtelmoney")
    AIRTEL_MONEY,

    @SerializedName("mtnmoney")
    MTN_MONEY,

    @SerializedName("pesalink")
    PESALINK
}

/**
 * [The payment object](https://falu.io)
 */
class Payment : FaluModel() {
    /**
     * Unique identifier for the object
     */
    var id: String = ""

    /**
     * Amount of the payment in smallest currency unit.
     */
    var amount: Int = 0

    /**
     * Three-letter ISO currency code, in lowercase
     */
    var currency: String = "kes"

    /**
     * The status of a payment
     */
    var status: PaymentStatus = PaymentStatus.PENDING

    /**
     * Time at which the object was created.
     */
    var created: Date = Date()

    /**
     * Time at which the object was last updated
     */
    var updated: Date? = null

    /**
     * The medium used for the payment
     */
    var type: PaymentType? = null

    /**
     * Represents the provider details for a MPESA payment
     */
    var mpesa: MpesaPayment? = null

    /**
     * Represents the provider details for a Pesalink payment
     */
    var pesalink: Any? = null

    /**
     * Details about failure of a payment, transfer or reversal.
     */
    var failure: PaymentFailure? = null

    /**
     * Identifier of the reversal, if the payment has been reversed
     */
    var reversalId: String? = null

    /**
     * Unique identifier for the workspace that the object belongs to
     */
    var workspaceId: String = ""

    /**
     * Indicates if this object belongs in the live environment
     */
    var live: Boolean = false
}

/**
 * [The Mpesa Payment object](https://falu.io)
 */
class MpesaPayment {
    /**
     * Type of payment
     */
    var type: PaymentType? = null

    /**
     * Reference the payment was made in.
     */
    var reference: String? = null

    /**
     * Phone number that made the payment, in E.164 format.
     */
    var phone: String? = null

    /**
     * Time at which the payment was initiated. This is only populated for payments that are initiated by the business such as MPESA's STK push.
     */
    var initiated: Date? = null

    /**
     * Time at which the payment validation was requested. This is only populare for payments that undergo validation such as customer initiate MPESA payments.
     */
    var validated: Date? = null

    /**
     * Whether the payment was marked as valid. This is only popular for payments that undergo validation such as customer initiate MPESA payments
     */
    var valid: Boolean? = null

    /**
     * Name of the entity making or that made the payment.
     */
    var payer: String? = null

    /**
     * The target business short code
     */
    var businessShortCode: String? = null

    /**
     * Unique identifier for request as issued by MPESA. Only populated for flows that initiate the transaction instead of MPESA. The value is only available after the request is sent to MPESA
     */
    var requestId: String? = null

    /**
     * Unique transaction identifier generated by MPESA. Only populated for completed transactions
     */
    var transactionNo: String? = null

    /**
     * Time at which the transaction was successfully completed
     */
    var completed: Date? = null
}

/**
 * [The Payment Failure object](https://falu.io)
 */
class PaymentFailure {
    /**
     * Reason for failure of a payment, transfer or reversal
     */
    var reason: FailureReason = FailureReason.UNKNOWN

    /**
     * Time at which failure occurred
     */
    var timestamp: Date? = null

    /**
     * Failure message as received from teh provider
     */
    var detail: String? = null
}

enum class FailureReason {
    @SerializedName("unknown")
    UNKNOWN,

    @SerializedName("insufficient_balance")
    INSUFFICIENT_BALANCE,

    @SerializedName("authentication_error")
    AUTHENTICATION_ERROR,

    @SerializedName("timeout")
    TIMEOUT,

    @SerializedName("other")
    OTHER
}