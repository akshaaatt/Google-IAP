package com.limerse.iap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BillingService(private val context: Context,
                     private val nonConsumableKeys: List<String>,
                     private val consumableKeys: List<String>,
                     private val subscriptionSkuKeys: List<String>)
    : IBillingService(), PurchasesUpdatedListener, BillingClientStateListener, AcknowledgePurchaseResponseListener {

    private lateinit var mBillingClient: BillingClient
    private var decodedKey: String? = null

    private var enableDebug: Boolean = false

    private val skusDetails = mutableMapOf<String, SkuDetails?>()

    override fun init(key: String?) {
        decodedKey = key

        mBillingClient = BillingClient.newBuilder(context).setListener(this).enablePendingPurchases().build()
        mBillingClient.startConnection(this)
    }

    @DelicateCoroutinesApi
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        log("onBillingSetupFinishedOkay: billingResult: $billingResult")

        if (billingResult.isOk()) {
            nonConsumableKeys.querySkuDetails(BillingClient.SkuType.INAPP) {
                consumableKeys.querySkuDetails(BillingClient.SkuType.INAPP) {
                    subscriptionSkuKeys.querySkuDetails(BillingClient.SkuType.SUBS) {
                        GlobalScope.launch {
                            queryPurchases()
                        }
                    }
                }
            }
        }
    }

    /**
     * Query Google Play Billing for existing purchases.
     * New purchases will be provided to the PurchasesUpdatedListener.
     */
    private suspend fun queryPurchases() {
        val inappResult: PurchasesResult = mBillingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP)
        processPurchases(inappResult.purchasesList, isRestore = true)
        val subsResult: PurchasesResult = mBillingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS)
        processPurchases(subsResult.purchasesList, isRestore = true)
    }

    override fun buy(activity: Activity, sku: String) {
        if (!sku.isSkuReady()) {
            log("buy. Google billing service is not ready yet.")
            return
        }

        launchBillingFlow(activity, sku, BillingClient.SkuType.INAPP)
    }

    override fun subscribe(activity: Activity, sku: String) {
        if (!sku.isSkuReady()) {
            log("buy. Google billing service is not ready yet.")
            return
        }

        launchBillingFlow(activity, sku, BillingClient.SkuType.SUBS)
    }

    private fun launchBillingFlow(activity: Activity, sku: String, type: String) {
        sku.toSkuDetails(type) { skuDetails ->
            if (skuDetails != null) {
                val purchaseParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails).build()
                mBillingClient.launchBillingFlow(activity, purchaseParams)
            }
        }
    }

    override fun unsubscribe(activity: Activity, sku: String) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            val subscriptionUrl = ("http://play.google.com/store/account/subscriptions"
                    + "?package=" + activity.packageName
                    + "&sku=" + sku)
            intent.data = Uri.parse(subscriptionUrl)
            activity.startActivity(intent)
            activity.finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun enableDebugLogging(enable: Boolean) {
        this.enableDebug = enable
    }

    /**
     * Called by the Billing Library when new purchases are detected.
     */
    @DelicateCoroutinesApi
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        log("onPurchasesUpdated: responseCode:$responseCode debugMessage: $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                log("onPurchasesUpdated. purchase: $purchases")
                processPurchases(purchases)
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                log("onPurchasesUpdated: User canceled the purchase")
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                log("onPurchasesUpdated: The user already owns this item")
                //item already owned? call queryPurchases to verify and process all such items
                GlobalScope.launch {
                    queryPurchases()
                }
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
                Log.e(
                    TAG, "onPurchasesUpdated: Developer error means that Google Play " +
                        "does not recognize the configuration. If you are just getting started, " +
                        "make sure you have configured the application correctly in the " +
                        "Google Play Console. The SKU product ID must match and the APK you " +
                        "are using must be signed with release keys."
                )
        }
    }

    private fun processPurchases(purchasesList: List<Purchase>?, isRestore: Boolean = false) {
        if (purchasesList != null) {
            log("processPurchases: " + purchasesList.size + " purchase(s)")
            purchases@ for (purchase in purchasesList) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && purchase.skus[0].isSkuReady()) {
                    if (!isSignatureValid(purchase)) {
                        log("processPurchases. Signature is not valid for: $purchase")
                        continue@purchases
                    }

                    // Grant entitlement to the user.
                    val skuDetails = skusDetails[purchase.skus[0]]
                    when (skuDetails?.type) {
                        BillingClient.SkuType.INAPP -> {
                            /**
                             * Consume the purchase
                             */
                            if(consumableKeys.contains(purchase.skus[0])){
                                mBillingClient.consumeAsync(
                                    ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                                ) { billingResult, _ ->
                                    when (billingResult.responseCode) {
                                        BillingClient.BillingResponseCode.OK -> {
                                            productOwned(skuDetails.sku, false)
                                        }
                                        else -> {
                                            Log.d(TAG, "Handling consumables : Error during consumption attempt -> ${billingResult.debugMessage}")
                                        }
                                    }
                                }
                            }
                           else{
                                productOwned(skuDetails.sku, isRestore)
                            }
                        }
                        BillingClient.SkuType.SUBS -> {
                            subscriptionOwned(skuDetails.sku, isRestore)
                        }
                    }

                    // Acknowledge the purchase if it hasn't already been acknowledged.
                    if (!purchase.isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken).build()
                        mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, this)
                    }
                } else {
                    Log.e(
                        TAG, "processPurchases failed. purchase: $purchase " +
                            "purchaseState: ${purchase.purchaseState} isSkuReady: ${purchase.skus[0].isSkuReady()}")
                }
            }
        } else {
            log("processPurchases: with no purchases")
        }
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        val key = decodedKey ?: return true
        return Security.verifyPurchase(key, purchase.originalJson, purchase.signature)
    }

    /**
     * Update Sku details after initialization.
     * This method has cache functionality.
     */
    private fun List<String>.querySkuDetails(type: String, done: () -> Unit) {
        if (::mBillingClient.isInitialized.not() || !mBillingClient.isReady) {
            log("querySkuDetails. Google billing service is not ready yet.")
            done()
            return
        }

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(this).setType(type)

        mBillingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            if (billingResult.isOk()) {
                skuDetailsList?.forEach { skusDetails[it.sku] = it }

                skusDetails.mapNotNull { entry ->
                    entry.value?.price?.let { entry.key to it }
                }.let { updatePrices(it.toMap()) }
            }
            done()
        }
    }

    /**
     * Get Sku details by sku and type.
     * This method has cache functionality.
     */
    private fun String.toSkuDetails(type: String, done: (skuDetails: SkuDetails?) -> Unit = {}) {
        if (::mBillingClient.isInitialized.not() || !mBillingClient.isReady) {
            log("buy. Google billing service is not ready yet.")
            done(null)
            return
        }

        val skuDetailsCached = skusDetails[this]
        if (skuDetailsCached != null) {
            done(skuDetailsCached)
            return
        }

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(listOf(this)).setType(type)

        mBillingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            if (billingResult.isOk()) {
                val skuDetails: SkuDetails? = skuDetailsList?.find { it.sku == this }
                skusDetails[this] = skuDetails
                done(skuDetails)
            } else {
                log("launchBillingFlow. Failed to get details for sku: $this")
                done(null)
            }
        }
    }

    private fun String.isSkuReady(): Boolean {
        return skusDetails.containsKey(this) && skusDetails[this] != null
    }

    override fun onBillingServiceDisconnected() {
        log("onBillingServiceDisconnected")
    }

    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
        log("onAcknowledgePurchaseResponse: billingResult: $billingResult")
    }

    override fun close() {
        mBillingClient.endConnection()
        super.close()
    }

    private fun BillingResult.isOk(): Boolean {
        return this.responseCode == BillingClient.BillingResponseCode.OK
    }

    private fun log(message: String) {
        if (enableDebug) {
            Log.d(TAG, message)
        }
    }

    companion object {
        const val TAG = "GoogleBillingService"
    }
}