package com.alphelios.iap

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails

class DataWrappers {

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
        val message: String,
        val responseCode: Int = 99
    ) {
        constructor(billingResult: BillingResult) : this(billingResult.debugMessage, billingResult.responseCode)

        override fun toString(): String {
            return "BillingResponse : responseCode:$responseCode message:$message"
        }
    }
}