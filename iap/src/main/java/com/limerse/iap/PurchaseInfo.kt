package com.limerse.iap

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails

data class PurchaseInfo(
    val skuDetails: SkuDetails,
    val purchase: Purchase
)
{
    val skuId = skuDetails.sku
}