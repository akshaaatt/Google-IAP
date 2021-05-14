package com.limerse.iap

interface SubscriptionServiceListener : BillingServiceListener {
    /**
     * Callback will be triggered upon owned subscription restore
     *
     * @param sku - specifier of owned subscription
     */
    fun onSubscriptionRestored(sku: String?)

    /**
     * Callback will be triggered when a subscription purchased successfully
     *
     * @param sku - specifier of purchased subscription
     */
    fun onSubscriptionPurchased(sku: String?)
}