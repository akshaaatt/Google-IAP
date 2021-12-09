package com.aemerse.iap

interface SubscriptionServiceListener : BillingServiceListener {
    /**
     * Callback will be triggered upon owned subscription restore
     *
     * @param purchaseInfo - specifier of owned subscription
     */
    fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo)

    /**
     * Callback will be triggered when a subscription purchased successfully
     *
     * @param purchaseInfo - specifier of purchased subscription
     */
    fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo)
}