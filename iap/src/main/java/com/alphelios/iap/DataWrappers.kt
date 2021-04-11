package com.alphelios.iap

import com.android.billingclient.api.AccountIdentifiers

class DataWrappers {

    data class SkuInfo(
        val sku: String,
        val description: String,
        val freeTrailPeriod: String,
        val iconUrl: String,
        val introductoryPrice: String,
        val introductoryPriceAmountMicros: Long,
        val introductoryPriceCycles: Int,
        val introductoryPricePeriod: String,
        val originalJson: String,
        val originalPrice: String,
        val originalPriceAmountMicros: Long,
        val price: String,
        val priceAmountMicros: Long,
        val priceCurrencyCode: String,
        val subscriptionPeriod: String,
        val title: String,
        val type: String,
        var isConsumable: Boolean = false
    )

    data class PurchaseInfo(
        val skuInfo: SkuInfo,
        val purchaseState: Int,
        val developerPayload: String?,
        val isAcknowledged: Boolean,
        val isAutoRenewing: Boolean,
        val orderId: String,
        val originalJson: String,
        val packageName: String,
        val purchaseTime: Long,
        val purchaseToken: String,
        val signature: String,
        val sku: String,
        val accountIdentifiers: AccountIdentifiers?
    )

    data class BillingResponse(
        val message: String,
        val responseCode: Int = 99
    )
}