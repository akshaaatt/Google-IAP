package com.alphelios.iap

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails

class DataWrappers {

    enum class ErrorType {
        CLIENT_NOT_READY_ERROR, CLIENT_DISCONNECTED, ITEM_ALREADY_OWNED_ERROR, CONSUME_ERROR, ACKNOWLEDGE_ERROR, FETCH_PURCHASED_PRODUCTS_ERROR,
        BILLING_ERROR
    }

    enum class SupportState {
        SUPPORTED, NOT_SUPPORTED, DISCONNECTED
    }

    enum class SkuProductType {
        CONSUMABLE, NON_CONSUMABLE, SUBSCRIPTION
    }

    data class SkuInfo(
        val skuProductType: SkuProductType,
        val skuDetails: SkuDetails,
    ) {
        val skuId = skuDetails.sku
    }

    data class PurchaseInfo(
        val skuInfo: SkuInfo,
        val purchase: Purchase
    ) {
        val skuId = skuInfo.skuId
        val skuProductType = skuInfo.skuProductType
    }

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
}