package com.aemerse.iap

interface PurchaseServiceListener : BillingServiceListener {
    /**
     * Callback will be triggered upon obtaining information about product prices
     *
     * @param iapKeyPrices - a map with available products
     */
    override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.ProductDetails>)

    /**
     * Callback will be triggered when a product purchased successfully
     *
     * @param purchaseInfo - specifier of owned product
     */
    fun onProductPurchased(purchaseInfo: DataWrappers.PurchaseInfo)

    /**
     * Callback will be triggered upon owned products restore
     *
     * @param purchaseInfo - specifier of owned product
     */
    fun onProductRestored(purchaseInfo: DataWrappers.PurchaseInfo)
}