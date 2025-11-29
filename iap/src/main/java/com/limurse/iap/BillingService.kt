package com.limurse.iap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class BillingService(
    private val context: Context,
    private val nonConsumableKeys: List<String>,
    private val consumableKeys: List<String>,
    private val subscriptionSkuKeys: List<String>,
) : IBillingService(), PurchasesUpdatedListener, AcknowledgePurchaseResponseListener {

    private lateinit var mBillingClient: BillingClient
    private var decodedKey: String? = null
    private var enableDebug: Boolean = false

    // Cache for ProductDetails
    private val productDetails = mutableMapOf<String, ProductDetails?>()

    override fun init(key: String?) {
        decodedKey = key
        mBillingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build()

        mBillingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                log("onBillingServiceDisconnected")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                log("onBillingSetupFinished: $billingResult")

                if (billingResult.isOk()) {
                    isBillingClientConnected(true, billingResult.responseCode)
                    // Query products sequentially to ensure cache is built
                    nonConsumableKeys.queryProductDetails(BillingClient.ProductType.INAPP) {
                        consumableKeys.queryProductDetails(BillingClient.ProductType.INAPP) {
                            subscriptionSkuKeys.queryProductDetails(BillingClient.ProductType.SUBS) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    queryPurchases()
                                }
                            }
                        }
                    }
                } else {
                    isBillingClientConnected(false, billingResult.responseCode)
                }
            }
        })
    }

    private suspend fun queryPurchases() {
        if (!mBillingClient.isReady) return

        // Query INAPP purchases
        val inAppResult = mBillingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        )
        processPurchases(inAppResult.purchasesList, isRestore = true)

        // Query SUBS purchases
        val subsResult = mBillingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        )
        processPurchases(subsResult.purchasesList, isRestore = true)
    }

    override fun buy(activity: Activity, sku: String, obfuscatedAccountId: String?, obfuscatedProfileId: String?) {
        if (!isProductReady(sku)) {
            // Try to fetch it just in case it was missed
            sku.toProductDetails(BillingClient.ProductType.INAPP) {
                if (it != null) {
                    launchBillingFlow(activity, sku, BillingClient.ProductType.INAPP, null, obfuscatedAccountId, obfuscatedProfileId)
                } else {
                    log("buy failed: SKU $sku not ready and could not be fetched.")
                }
            }
            return
        }
        launchBillingFlow(activity, sku, BillingClient.ProductType.INAPP, null, obfuscatedAccountId, obfuscatedProfileId)
    }

    override fun subscribe(activity: Activity, sku: String, offerId: String?, obfuscatedAccountId: String?, obfuscatedProfileId: String?) {
        if (!isProductReady(sku)) {
            sku.toProductDetails(BillingClient.ProductType.SUBS) {
                if (it != null) {
                    launchBillingFlow(activity, sku, BillingClient.ProductType.SUBS, offerId, obfuscatedAccountId, obfuscatedProfileId)
                } else {
                    log("subscribe failed: SKU $sku not ready.")
                }
            }
            return
        }
        launchBillingFlow(activity, sku, BillingClient.ProductType.SUBS, offerId, obfuscatedAccountId, obfuscatedProfileId)
    }

    private fun launchBillingFlow(activity: Activity, sku: String, type: String, offerId: String?, obfuscatedAccountId: String?, obfuscatedProfileId: String?) {
        val details = productDetails[sku] ?: return

        val productDetailsParamsList = mutableListOf<BillingFlowParams.ProductDetailsParams>()
        val builder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        if (type == BillingClient.ProductType.SUBS && details.subscriptionOfferDetails != null) {
            // Find the specific offer if provided, otherwise default to the first one (or handle logic as needed)
            val offer = details.subscriptionOfferDetails?.firstOrNull { it.offerId == offerId }
                ?: details.subscriptionOfferDetails?.firstOrNull()

            offer?.let {
                builder.setOfferToken(it.offerToken)
            }
        }

        productDetailsParamsList.add(builder.build())

        val flowParamsBuilder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)

        if (obfuscatedAccountId != null) flowParamsBuilder.setObfuscatedAccountId(obfuscatedAccountId)
        if (obfuscatedProfileId != null) flowParamsBuilder.setObfuscatedProfileId(obfuscatedProfileId)

        mBillingClient.launchBillingFlow(activity, flowParamsBuilder.build())
    }

    override fun unsubscribe(activity: Activity, sku: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data =
                    "https://play.google.com/store/account/subscriptions?package=${activity.packageName}&sku=$sku".toUri()
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Unsubscribing failed.", e)
        }
    }

    override fun enableDebugLogging(enable: Boolean) {
        this.enableDebug = enable
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        val responseCode = billingResult.responseCode
        log("onPurchasesUpdated: $responseCode ${billingResult.debugMessage}")

        if (billingResult.isOk()) {
            processPurchases(purchases)
        } else {
            if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                CoroutineScope(Dispatchers.IO).launch { queryPurchases() }
            } else if (responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                updateFailedPurchases(purchases?.map { getPurchaseInfo(it) }, responseCode)
            }
        }
    }

    private fun processPurchases(purchasesList: List<Purchase>?, isRestore: Boolean = false) {
        if (purchasesList.isNullOrEmpty()) {
            log("processPurchases: No purchases to process")
            return
        }

        log("processPurchases: ${purchasesList.size} purchase(s)")

        for (purchase in purchasesList) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED || purchase.purchaseState == Purchase.PurchaseState.PENDING) {

                val sku = purchase.products.firstOrNull() ?: continue

                // CRITICAL FIX: If product details are missing, fetch them now instead of failing
                if (!isProductReady(sku)) {
                    log("processPurchases: Details missing for $sku. Fetching...")
                    val type = if (subscriptionSkuKeys.contains(sku)) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP

                    sku.toProductDetails(type) { details ->
                        if (details != null) {
                            // Retry processing with loaded details
                            processSinglePurchase(purchase, isRestore)
                        } else {
                            log("processPurchases failed: Could not fetch details for $sku")
                            updateFailedPurchase(getPurchaseInfo(purchase))
                        }
                    }
                } else {
                    processSinglePurchase(purchase, isRestore)
                }
            }
        }
    }

    private fun processSinglePurchase(purchase: Purchase, isRestore: Boolean) {
        if (!isSignatureValid(purchase)) {
            log("Signature invalid for: ${purchase.orderId}")
            updateFailedPurchase(getPurchaseInfo(purchase))
            return
        }

        val sku = purchase.products[0]
        val details = productDetails[sku] ?: return
        val isConsumable = consumableKeys.contains(sku)

        // Grant Entitlement
        when (details.productType) {
            BillingClient.ProductType.INAPP -> {
                if (isConsumable && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                    mBillingClient.consumeAsync(consumeParams) { result, _ ->
                        if (result.isOk()) {
                            productOwned(getPurchaseInfo(purchase), false)
                        } else {
                            updateFailedPurchase(getPurchaseInfo(purchase), result.responseCode)
                        }
                    }
                } else {
                    productOwned(getPurchaseInfo(purchase), isRestore)
                }
            }
            BillingClient.ProductType.SUBS -> {
                subscriptionOwned(getPurchaseInfo(purchase), isRestore)
            }
        }

        // Acknowledge if needed
        if (!purchase.isAcknowledged && !isConsumable && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val ackParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            mBillingClient.acknowledgePurchase(ackParams, this)
        }
    }

    /**
     * FIXED: Removed 'this.forEach' bug.
     * Previous code iterated over characters of the String, breaking the SKU.
     */
    private fun String.toProductDetails(type: String, done: (productDetails: ProductDetails?) -> Unit) {
        if (!::mBillingClient.isInitialized || !mBillingClient.isReady) {
            done(null)
            return
        }

        // Check cache first
        productDetails[this]?.let {
            done(it)
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(this) // Use 'this' (the string) directly
                .setProductType(type)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        mBillingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            productDetailsResult.productDetailsList
            if (billingResult.isOk() && productDetailsResult.productDetailsList.isNotEmpty()) {
                val item = productDetailsResult.productDetailsList.first()
                productDetails[this] = item
                done(item)
            } else {
                done(null)
            }
        }
    }

    /**
     * FIXED: Removed Java Reflection. Directly accessing the list.
     */
    private fun List<String>.queryProductDetails(type: String, done: () -> Unit) {
        if (!::mBillingClient.isInitialized || !mBillingClient.isReady || this.isEmpty()) {
            done()
            return
        }

        val productList = this.map { sku ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(type)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        mBillingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            if (billingResult.isOk()) {
                productDetailsResult.productDetailsList.forEach { details ->
                    productDetails[details.productId] = details
                }

                // Convert to your custom wrapper
                val wrapperMap = productDetailsResult.productDetailsList.associate { details ->
                    details.productId to mapToWrapper(details)
                }
                updatePrices(wrapperMap)
            }
            done()
        }
    }

    private fun mapToWrapper(details: ProductDetails): DataWrappers.ProductDetails {
        val offers = if (details.productType == BillingClient.ProductType.SUBS) {
            details.subscriptionOfferDetails?.map { offer ->
                DataWrappers.Offer(
                    id = offer.offerId,
                    token = offer.offerToken,
                    tags = offer.offerTags,
                    pricingPhases = offer.pricingPhases.pricingPhaseList.map { phase ->
                        DataWrappers.PricingPhase(
                            priceCurrencyCode = phase.priceCurrencyCode,
                            price = phase.formattedPrice,
                            priceAmount = phase.priceAmountMicros / 1_000_000.0,
                            billingCycleCount = phase.billingCycleCount,
                            billingPeriod = phase.billingPeriod,
                            recurrenceMode = phase.recurrenceMode
                        )
                    }
                )
            } ?: emptyList()
        } else {
            // ONE-TIME PRODUCTS (INAPP) logic
            details.oneTimePurchaseOfferDetails?.let { offer ->
                listOf(DataWrappers.Offer(
                    id = offer.offerId,
                    token = offer.offerToken,
                    tags = offer.offerTags,
                    pricingPhases = listOf(
                        DataWrappers.PricingPhase(
                            priceCurrencyCode = offer.priceCurrencyCode,
                            price = offer.formattedPrice,
                            priceAmount = offer.priceAmountMicros / 1_000_000.0,
                            billingCycleCount = null,
                            billingPeriod = null,
                            recurrenceMode = ProductDetails.RecurrenceMode.NON_RECURRING
                        )
                    )
                ))
            } ?: emptyList()
        }

        return DataWrappers.ProductDetails(
            title = details.title,
            description = details.description,
            offers = offers
        )
    }

    private fun isProductReady(sku: String): Boolean {
        return productDetails.containsKey(sku) && productDetails[sku] != null
    }

    private fun getPurchaseInfo(purchase: Purchase): DataWrappers.PurchaseInfo {
        return DataWrappers.PurchaseInfo(
            purchase.purchaseState,
            purchase.developerPayload,
            purchase.isAcknowledged,
            purchase.isAutoRenewing,
            purchase.orderId,
            purchase.originalJson,
            purchase.packageName,
            purchase.purchaseTime,
            purchase.purchaseToken,
            purchase.signature,
            purchase.products.firstOrNull() ?: "",
            purchase.accountIdentifiers
        )
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        val key = decodedKey ?: return true
        return Security.verifyPurchase(key, purchase.originalJson, purchase.signature)
    }

    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
        log("onAcknowledgePurchaseResponse: $billingResult")
        if (!billingResult.isOk()) {
            updateFailedPurchase(billingResponseCode = billingResult.responseCode)
        }
    }

    override fun close() {
        if (::mBillingClient.isInitialized) {
            mBillingClient.endConnection()
        }
        super.close()
    }

    private fun BillingResult.isOk(): Boolean = responseCode == BillingClient.BillingResponseCode.OK

    private fun log(message: String) {
        if (enableDebug) Log.d(TAG, message)
    }

    companion object {
        const val TAG = "GoogleBillingService"
    }
}