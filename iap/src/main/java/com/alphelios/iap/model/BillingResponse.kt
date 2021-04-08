package com.alphelios.iap.model

import com.alphelios.iap.type.ErrorType
import com.android.billingclient.api.BillingResult

data class BillingResponse(
    val errorType: ErrorType,
    val message: String,
    val responseCode: Int = 99
) {
    constructor(errorType: ErrorType, billingResult: BillingResult) : this(errorType, billingResult.debugMessage, billingResult.responseCode)

    override fun toString(): String {
        return "BillingResponse : errorType:$errorType responseCode:$responseCode message:$message"
    }

}