package com.aemerse.iap

import com.android.billingclient.api.AccountIdentifiers

class DataWrappers {

    data class SkuInfo(
        val sku: String,
        val iconUrl: String,
        val originalJson: String,
        val type: String,
        val skuDetails: SkuDetails,
        var isConsumable: Boolean = false
    )

    data class SkuDetails(
        val title: String?,
        val description: String?,
        val freeTrailPeriod: String?,
        val introductoryPrice: String?,
        val introductoryPriceAmount: Double?,
        val introductoryPriceCycles: Int?,
        val introductoryPricePeriod: String?,
        val originalPrice: String?,
        val originalPriceAmount: Double?,
        val price: String?,
        val priceAmount: Double?,
        val priceCurrencyCode: String?,
        val subscriptionPeriod: String?,
    )

    data class PurchaseInfo(
        val skuInfo: SkuInfo,
        val purchaseState: Int,
        val developerPayload: String,
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
}