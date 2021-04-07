package com.alphelios.iap

import com.android.billingclient.api.AccountIdentifiers
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
    }

    data class BillingResponse(
        val message: String,
        val responseCode: Int = 99
    )
}