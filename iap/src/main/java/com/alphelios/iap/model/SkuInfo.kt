package com.alphelios.iap.model

import com.alphelios.iap.type.SkuProductType
import com.android.billingclient.api.SkuDetails

data class SkuInfo(
    val skuProductType: SkuProductType,
    val skuDetails: SkuDetails,
) {
    val skuId = skuDetails.sku
}