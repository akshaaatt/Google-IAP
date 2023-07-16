package com.limurse.iap

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * Initialize billing service.
 *
 * @param context Application context.
 * @param nonConsumableKeys SKU list for non-consumable one-time products.
 * @param consumableKeys SKU list for consumable one-time products.
 * @param subscriptionKeys SKU list for subscriptions.
 * @param key Key to verify purchase messages. Leave it empty if you want to skip verification.
 * @param obfuscatedAccountId Specifies an optional obfuscated string that is uniquely associated with the user's account in your app.
 * @param obfuscatedProfileId Specifies an optional obfuscated string that is uniquely associated with the user's profile in your app.
 * @param enableLogging Log operations/errors to the logcat for debugging purposes.
 */
@OptIn(DelicateCoroutinesApi::class)
class IapConnector @JvmOverloads constructor(
    context: Context,
    nonConsumableKeys: List<String> = emptyList(),
    consumableKeys: List<String> = emptyList(),
    subscriptionKeys: List<String> = emptyList(),
    key: String? = null,
    obfuscatedAccountId: String? = null,
    obfuscatedProfileId: String? = null,
    enableLogging: Boolean = false
) {

    private var mBillingService: IBillingService? = null

    init {
        val contextLocal = context.applicationContext ?: context
        mBillingService = BillingService(contextLocal, nonConsumableKeys, consumableKeys, subscriptionKeys, obfuscatedAccountId, obfuscatedProfileId)
        getBillingService().init(key)
        getBillingService().enableDebugLogging(enableLogging)
    }

    fun addBillingClientConnectionListener(billingClientConnectionListener: BillingClientConnectionListener) {
        getBillingService().addBillingClientConnectionListener(billingClientConnectionListener)
    }

    fun removeBillingClientConnectionListener(billingClientConnectionListener: BillingClientConnectionListener) {
        getBillingService().removeBillingClientConnectionListener(billingClientConnectionListener)
    }

    fun addPurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        getBillingService().addPurchaseListener(purchaseServiceListener)
    }

    fun removePurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        getBillingService().removePurchaseListener(purchaseServiceListener)
    }

    fun addSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        getBillingService().addSubscriptionListener(subscriptionServiceListener)
    }

    fun removeSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        getBillingService().removeSubscriptionListener(subscriptionServiceListener)
    }

    fun purchase(activity: Activity, sku: String) {
        getBillingService().buy(activity, sku)
    }

    fun subscribe(activity: Activity, sku: String) {
        getBillingService().subscribe(activity, sku)
    }

    fun unsubscribe(activity: Activity, sku: String) {
        getBillingService().unsubscribe(activity, sku)
    }

    fun destroy() {
        getBillingService().close()
    }

    private fun getBillingService(): IBillingService {
        return mBillingService ?: let {
            throw RuntimeException("Call IapConnector to initialize billing service")
        }
    }
}
