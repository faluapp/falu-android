package io.falu.android.models.payments

/**
 * Represents the provider details for a MPESA payment
 */
data class MpesaPaymentRequest(
    /**
     * Phone number representing the account to be charged, in E.164 format.
     */
    var phone: String,

    /**
     * The reference that the payment will be made in. This can be an account number.
     */
    var reference: String,

    /**
     * Set true if the payment to be initiated is to be made to a paybill; false, the payment is made to a BuyGoods till
     */
    var paybill: Boolean,

    /**
     * The shortcode of the receiver. When not provided, it defaults to the default recipient.
     * When not provided, either the default incoming business code or the first business code for the workspace is used depending on the kind.
     * This value is usually required and different from the business short code when using BuyGood
     */
    var destination: String?
)