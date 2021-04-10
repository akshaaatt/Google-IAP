package com.alphelios.iap

import com.alphelios.iap.model.BillingResponse
import com.alphelios.iap.model.PurchaseInfo
import com.alphelios.iap.model.SkuInfo

/**
 * Establishes communication bridge between caller and [IapConnector].
 * [onProductsFetched] listen to data about all product IDs retrieved from Play console.
 * [onPurchasedProductsFetched] listen to data about all purchases retrieved from Play console.
 * [onProductsPurchased] provides recent purchases
 * [onPurchaseAcknowledged] provides a callback after a purchase is acknowledged
 * [onError] is used to notify caller about possible errors.
 */
interface BillingEventListener {
    fun onProductsFetched(skuDetailsList: List<SkuInfo>)
    fun onPurchasedProductsFetched(purchases: List<PurchaseInfo>)
    fun onProductsPurchased(purchases: List<PurchaseInfo>)
    fun onPurchaseAcknowledged(purchase: PurchaseInfo)
    fun onConsumed(purchase: PurchaseInfo)
    fun onError(inAppConnector: IapConnector, result: BillingResponse)
}