package com.limurse.iap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.forEach

class BillingService(
    private val context: Context,
    private val nonConsumableKeys: List<String>,
    private val consumableKeys: List<String>,
    private val subscriptionSkuKeys: List<String>,
) : IBillingService(), PurchasesUpdatedListener, AcknowledgePurchaseResponseListener {

    private lateinit var mBillingClient: BillingClient
    private var decodedKey: String? = null

    private var enableDebug: Boolean = false

    private val productDetails = mutableMapOf<String, ProductDetails?>()

    override fun init(key: String?) {
        decodedKey = key
        mBillingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        mBillingClient.startConnection(object : BillingClientStateListener{
            override fun onBillingServiceDisconnected() {
                log("onBillingServiceDisconnected")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                log("onBillingSetupFinishedOkay: billingResult: $billingResult")

                when {
                    billingResult.isOk() -> {
                        isBillingClientConnected(true, billingResult.responseCode)
                        nonConsumableKeys.queryProductDetails(BillingClient.ProductType.INAPP) {
                            consumableKeys.queryProductDetails(BillingClient.ProductType.INAPP) {
                                subscriptionSkuKeys.queryProductDetails(BillingClient.ProductType.SUBS) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        queryPurchases()
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        isBillingClientConnected(false, billingResult.responseCode)
                    }
                }
            }

        })
    }

    /**
     * Query Google Play Billing for existing purchases.
     * New purchases will be provided to the PurchasesUpdatedListener.
     */
    private suspend fun queryPurchases() {
        val inAppResult: PurchasesResult = mBillingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        )
        processPurchases(inAppResult.purchasesList, isRestore = true)
        val subsResult: PurchasesResult = mBillingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        )
        processPurchases(subsResult.purchasesList, isRestore = true)
    }

    override fun buy(activity: Activity, sku: String, obfuscatedAccountId: String?, obfuscatedProfileId: String?) {
        if (!sku.isProductReady()) {
            log("buy. Google billing service is not ready yet. (SKU is not ready yet -1)")
            return
        }

        launchBillingFlow(activity, sku, BillingClient.ProductType.INAPP, null, obfuscatedAccountId, obfuscatedProfileId)
    }

    override fun subscribe(activity: Activity, sku: String, offerId: String?, obfuscatedAccountId: String?, obfuscatedProfileId: String?) {
        if (!sku.isProductReady()) {
            log("buy. Google billing service is not ready yet. (SKU is not ready yet -2)")
            return
        }

        launchBillingFlow(activity, sku, BillingClient.ProductType.SUBS, offerId, obfuscatedAccountId, obfuscatedProfileId)
    }

    private fun launchBillingFlow(activity: Activity, sku: String, type: String, offerId: String?, obfuscatedAccountId: String?, obfuscatedProfileId: String?) {
        sku.toProductDetails(type) { productDetails ->
            if (productDetails != null) {

                val productDetailsParamsList = mutableListOf<BillingFlowParams.ProductDetailsParams>()
                val builder = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)

                if(type == BillingClient.ProductType.SUBS) {
                    var index = 0
                    productDetails.subscriptionOfferDetails?.indexOfFirst { it.offerId == offerId }?.also {
                        if (it >= 0) {
                            index = it
                        }
                    }
                    productDetails.subscriptionOfferDetails?.getOrNull(index)?.also {
                        builder.setOfferToken(it.offerToken)
                    }
                }
                productDetailsParamsList.add(builder.build())
                val billingFlowParamsBuilder = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList)
                if (obfuscatedAccountId != null) {
                    billingFlowParamsBuilder.setObfuscatedAccountId(obfuscatedAccountId)
                }
                if (obfuscatedProfileId != null) {
                    billingFlowParamsBuilder.setObfuscatedProfileId(obfuscatedProfileId)
                }
                val billingFlowParams = billingFlowParamsBuilder.build()
                
                mBillingClient.launchBillingFlow(activity, billingFlowParams)
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
            Log.w(TAG, "Unsubscribing failed.")
        }
    }

    override fun enableDebugLogging(enable: Boolean) {
        this.enableDebug = enable
    }

    /**
     * Called by the Billing Library when new purchases are detected.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        log("onPurchasesUpdated: responseCode:$responseCode debugMessage: $debugMessage")
        if (!billingResult.isOk()){
            updateFailedPurchases(purchases?.map { getPurchaseInfo(it) }, responseCode)
        }
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
                CoroutineScope(Dispatchers.IO).launch {
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
        if (!purchasesList.isNullOrEmpty()) {
            log("processPurchases: " + purchasesList.size + " purchase(s)")
            purchases@ for (purchase in purchasesList) {
                // The purchase is considered successful in both PURCHASED and PENDING states.
                val purchaseSuccess = purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                        || purchase.purchaseState == Purchase.PurchaseState.PENDING

                if (purchaseSuccess && purchase.products[0].isProductReady()) {
                    if (!isSignatureValid(purchase)) {
                        log("processPurchases. Signature is not valid for: $purchase")
                        updateFailedPurchase(getPurchaseInfo(purchase))
                        continue@purchases
                    }

                    // Grant entitlement to the user.
                    val productDetails = productDetails[purchase.products[0]]
                    val isProductConsumable = consumableKeys.contains(purchase.products[0])
                    when (productDetails?.productType) {
                        BillingClient.ProductType.INAPP -> {
                            // Consume the purchase
                            when {
                                isProductConsumable && purchase.purchaseState == Purchase.PurchaseState.PURCHASED -> {
                                    mBillingClient.consumeAsync(
                                        ConsumeParams.newBuilder()
                                            .setPurchaseToken(purchase.purchaseToken).build()
                                    ) { billingResult, _ ->
                                        when (billingResult.responseCode) {
                                            BillingClient.BillingResponseCode.OK -> {
                                                productOwned(getPurchaseInfo(purchase), false)
                                            }
                                            else -> {
                                                Log.d(
                                                    TAG,
                                                    "Handling consumables : Error during consumption attempt -> ${billingResult.debugMessage}"
                                                )
                                                updateFailedPurchase(getPurchaseInfo(purchase), billingResult.responseCode)
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    productOwned(getPurchaseInfo(purchase), isRestore)
                                }
                            }
                        }
                        BillingClient.ProductType.SUBS -> {
                            subscriptionOwned(getPurchaseInfo(purchase), isRestore)
                        }
                    }

                    // If the state is PURCHASED, acknowledge the purchase if it hasn't been acknowledged yet.
                    if (!purchase.isAcknowledged && !isProductConsumable && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken).build()
                        mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, this)
                    }
                } else {
                    Log.e(
                        TAG, "processPurchases failed. purchase: $purchase " +
                                "purchaseState: ${purchase.purchaseState} isSkuReady: ${purchase.products[0].isProductReady()}"
                    )
                    updateFailedPurchase(getPurchaseInfo(purchase))
                }
            }
        } else {
            log("processPurchases: with no purchases")
        }
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
            purchase.products[0],
            purchase.accountIdentifiers
        )
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        val key = decodedKey ?: return true
        return Security.verifyPurchase(key, purchase.originalJson, purchase.signature)
    }

    /**
     * Update Sku details after initialization.
     * This method has cache functionality.
     */
    private fun List<String>.queryProductDetails(type: String, done: () -> Unit) {
        if (::mBillingClient.isInitialized.not() || !mBillingClient.isReady) {
            log("queryProductDetails. Google billing service is not ready yet.")
            done()
            return
        }

        if (this.isEmpty()) {
            log("queryProductDetails. Sku list is empty.")
            done()
            return
        }

        val productList = mutableListOf<QueryProductDetailsParams.Product>()
        this.forEach {
            productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(type)
                .build())
        }
        
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)

        mBillingClient.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->
            if (billingResult.isOk()) {
                isBillingClientConnected(true, billingResult.responseCode)
                productDetailsList?.let { list ->
                    // Log the type to understand what we're dealing with
                    log("Product details list type: ${list::class.java.name}")
                    log("Product details list: $list")
                    
                    // Try to access using reflection or other methods
                    try {
                        val sizeMethod = list::class.java.getMethod("size")
                        val size = sizeMethod.invoke(list) as Int
                        log("Product details list size: $size")
                        
                        for (i in 0 until size) {
                            val getMethod = list::class.java.getMethod("get", Int::class.java)
                            val productDetail = getMethod.invoke(list, i)
                            if (productDetail is ProductDetails) {
                                productDetails[productDetail.productId] = productDetail
                            }
                        }
                    } catch (e: Exception) {
                        log("Failed to access product details list: ${e.message}")
                    }
                }

                productDetails.mapNotNull { entry ->
                    entry.value?.let {
                        when(it.productType){
                            BillingClient.ProductType.SUBS->{
                                entry.key to  DataWrappers.ProductDetails(
                                    title = it.title,
                                    description = it.description,
                                    offers = it.subscriptionOfferDetails?.map { offerDetails ->
                                        DataWrappers.Offer(
                                            id = offerDetails.offerId,
                                            token = offerDetails.offerToken,
                                            tags = offerDetails.offerTags,
                                            pricingPhases = offerDetails.pricingPhases.pricingPhaseList.map { pricingPhase ->
                                                DataWrappers.PricingPhase(
                                                    priceCurrencyCode = pricingPhase.priceCurrencyCode,
                                                    price = pricingPhase.formattedPrice,
                                                    priceAmount = pricingPhase.priceAmountMicros.div(1000000.0),
                                                    billingCycleCount = pricingPhase.billingCycleCount,
                                                    billingPeriod = pricingPhase.billingPeriod,
                                                    recurrenceMode = pricingPhase.recurrenceMode
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                            else->{
                                entry.key to DataWrappers.ProductDetails(
                                    title = it.title,
                                    description = it.description,
                                    offers = it.oneTimePurchaseOfferDetails?.let { offerDetails ->
                                        listOf(DataWrappers.Offer(
                                            id = null,
                                            token = null,
                                            tags = null,
                                            pricingPhases = listOf(
                                                DataWrappers.PricingPhase(
                                                    priceCurrencyCode = offerDetails.priceCurrencyCode,
                                                    price = offerDetails.formattedPrice,
                                                    priceAmount = offerDetails.priceAmountMicros.div(1000000.0),
                                                    billingCycleCount = null,
                                                    billingPeriod = null,
                                                    recurrenceMode = ProductDetails.RecurrenceMode.NON_RECURRING
                                            ))
                                        ))
                                    } ?: listOf()
                                )
                            }
                        }
                    }
                }.let {
                    updatePrices(it.toMap())
                }
            }
            done()
        }
    }

    /**
     * Get Sku details by sku and type.
     * This method has cache functionality.
     */
    private fun String.toProductDetails(type: String, done: (productDetails: ProductDetails?) -> Unit = {}) {
        if (::mBillingClient.isInitialized.not() || !mBillingClient.isReady) {
            log("buy. Google billing service is not ready yet.(mBillingClient is not ready yet - 001)")
            done(null)
            return
        }

        val productDetailsCached = productDetails[this]
        if (productDetailsCached != null) {
            done(productDetailsCached)
            return
        }

        val productList = mutableListOf<QueryProductDetailsParams.Product>()
        this.forEach {
            productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it.toString())
                .setProductType(type)
                .build())
        }

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)

        mBillingClient.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->
            when {
                billingResult.isOk() -> {
                    isBillingClientConnected(true, billingResult.responseCode)
                    val productDetails: ProductDetails? = productDetailsList?.let { list ->
                        // Log the type to understand what we're dealing with
                        log("Product details list type: ${list::class.java.name}")
                        log("Product details list: $list")
                        
                        // Try to access using reflection or other methods
                        try {
                            val sizeMethod = list::class.java.getMethod("size")
                            val size = sizeMethod.invoke(list) as Int
                            log("Product details list size: $size")
                            
                            for (i in 0 until size) {
                                val getMethod = list::class.java.getMethod("get", Int::class.java)
                                val productDetail = getMethod.invoke(list, i)
                                if (productDetail is ProductDetails && productDetail.productId == this) {
                                    return@let productDetail
                                }
                            }
                        } catch (e: Exception) {
                            log("Failed to access product details list: ${e.message}")
                        }
                        null
                    }
                   // productDetails[this] = productDetails
                    done(productDetails)
                }
                else -> {
                    log("launchBillingFlow. Failed to get details for sku: $this")
                    done(null)
                }
            }
        }
    }

    private fun String.isProductReady(): Boolean {
        return productDetails.containsKey(this) && productDetails[this] != null
    }

    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
        log("onAcknowledgePurchaseResponse: billingResult: $billingResult")
        if(!billingResult.isOk()){
            updateFailedPurchase(billingResponseCode =  billingResult.responseCode)
        }
    }

    override fun close() {
        mBillingClient.endConnection()
        super.close()
    }

    private fun BillingResult.isOk(): Boolean {
        return this.responseCode == BillingClient.BillingResponseCode.OK
    }

    private fun log(message: String) {
        when {
            enableDebug -> {
                Log.d(TAG, message)
            }
        }
    }

    companion object {
        const val TAG = "GoogleBillingService"
    }
}
