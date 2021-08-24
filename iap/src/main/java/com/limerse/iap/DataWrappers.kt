package com.limerse.iap

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

    data class PurchaseInfo @JvmOverloads constructor(
        val skuInfo: SkuInfo? = null,
        val purchaseState: Int = 100,
        val developerPayload: String? = null,
        val isAcknowledged: Boolean = true,
        val isAutoRenewing: Boolean = true,
        val orderId: String? = null,
        val originalJson: String? = null,
        val packageName: String = BuildConfig.LIBRARY_PACKAGE_NAME,
        val purchaseTime: Long = 100,
        val purchaseToken: String? = null,
        val signature: String? = null,
        val sku: String? = null,
        val accountIdentifiers: AccountIdentifiers? = null
    )
}