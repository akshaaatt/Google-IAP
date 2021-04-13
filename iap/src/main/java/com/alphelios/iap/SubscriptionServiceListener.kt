package com.alphelios.iap

interface SubscriptionServiceListener : BillingServiceListener {
    /**
     * Callback will be triggered upon owned subscription restore
     *
     * @param sku - specificator of owned subscription
     */
    fun onSubscriptionRestored(sku: String?)

    /**
     * Callback will be triggered when a subscription purchased successfully
     *
     * @param sku - specificator of purchased subscription
     */
    fun onSubscriptionPurchased(sku: String?)
}