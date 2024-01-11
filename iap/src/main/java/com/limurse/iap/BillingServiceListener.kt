package com.limurse.iap

interface BillingServiceListener {
    /**
     * Callback will be triggered upon obtaining information about product prices
     *
     * @param iapKeyPrices - a map with available products
     */
    fun onPricesUpdated(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>)

    /**
     * Callback will be triggered when a purchase was failed.
     *
     * @param purchaseInfo - specifier of purchase info if exists
     * @param billingResponseCode - response code returned from the billing library if exists
     */
    fun onPurchaseFailed(purchaseInfo: DataWrappers.PurchaseInfo?, billingResponseCode: Int?)
}