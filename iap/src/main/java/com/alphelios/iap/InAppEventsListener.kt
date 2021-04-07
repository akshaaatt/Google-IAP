package com.alphelios.iap

import com.alphelios.iap.DataWrappers.*

/**
 * Establishes communication bridge between caller and [IapConnector].
 * [onProductsPurchased] provides recent purchases
 * [onPurchaseAcknowledged] provides a callback after a purchase is acknowledged
 * [onSubscriptionsFetched] and [onInAppProductsFetched] is to listen data about product IDs retrieved from Play console.
 * [onError] is used to notify caller about possible errors.
 */
interface InAppEventsListener {
    fun onSubscriptionsFetched(skuDetailsList: List<SkuInfo>)
    fun onInAppProductsFetched(skuDetailsList: List<SkuInfo>)
    fun onPurchaseAcknowledged(purchase: PurchaseInfo)
    fun onProductsPurchased(purchases: List<PurchaseInfo>)
    fun onError(inAppConnector: IapConnector, result: BillingResponse? = null)
}