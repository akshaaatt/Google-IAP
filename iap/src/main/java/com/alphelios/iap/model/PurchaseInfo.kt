package com.alphelios.iap.model

import com.android.billingclient.api.Purchase

data class PurchaseInfo(
    val skuInfo: SkuInfo,
    val purchase: Purchase
) {
    val skuId = skuInfo.skuId
    val skuProductType = skuInfo.skuProductType
}