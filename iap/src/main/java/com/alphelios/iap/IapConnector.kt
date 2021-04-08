package com.alphelios.iap

import android.app.Activity
import android.content.Context
import android.util.Log
import com.alphelios.iap.model.BillingResponse
import com.alphelios.iap.model.PurchaseInfo
import com.alphelios.iap.model.SkuInfo
import com.alphelios.iap.type.ErrorType
import com.alphelios.iap.type.SkuProductType
import com.alphelios.iap.type.SkuProductType.*
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.*
import com.android.billingclient.api.BillingClient.FeatureType.SUBSCRIPTIONS
import com.android.billingclient.api.BillingClient.SkuType.INAPP
import com.android.billingclient.api.BillingClient.SkuType.SUBS
import java.lang.IllegalArgumentException

/**
 * Wrapper class for Google In-App purchases.
 * Handles vital processes while dealing with IAP.
 */
class IapConnector(context: Context, private val base64Key: String) {

    private val tag = "InAppLog"
    private lateinit var billingClient: BillingClient

    private var fetchedSkuInfosList = mutableListOf<SkuInfo>()
    private var purchasedProductsList = mutableListOf<PurchaseInfo>()
    private var billingEventListener: BillingEventListener? = null

    private var nonConsumableIds: List<String>? = null
    private var subIds: List<String>? = null
    private var consumableIds: List<String>? = null
    private var shouldAutoAcknowledge: Boolean = false
    private var shouldAutoConsume: Boolean = false

    private var connected = false
    private var checkedForPurchasesAtStart = false
    private var fetchedPurchasedProducts = false

    init {
        init(context)
    }


    fun isReady(): Boolean {
        if (!connected)
            Log.d(tag, "Billing client : is not ready because no connection is established yet")

        if (!billingClient.isReady)
            Log.d(tag, "Billing client : is not ready because iapClient is not ready yet")

        if (fetchedSkuInfosList.isEmpty())
            Log.d(tag, "Billing client : is not ready because fetchedSkuDetailsList is empty or not fetched yet")


        return connected && billingClient.isReady && fetchedSkuInfosList.isNotEmpty()
    }

    /**
     * To set non-consumable product IDs.
     */
    fun setNonConsumableIds(nonConsumableIds: List<String>): IapConnector {
        this.nonConsumableIds = nonConsumableIds
        return this
    }

    /**
     * To set consumable product IDs.
     * Rest of the IDs will be considered non-consumable.
     */
    fun setConsumableIds(consumableIds: List<String>): IapConnector {
        this.consumableIds = consumableIds
        return this
    }

    /**
     * To set subscription product IDs.
     */
    fun setSubscriptionIds(subIds: List<String>): IapConnector {
        this.subIds = subIds
        return this
    }

    /**
     * Iap will auto acknowledge the purchase
     */
    fun autoAcknowledge(): IapConnector {
        shouldAutoAcknowledge = true
        return this
    }

    /**
     * Iap will auto consume consumable purchases
     */
    fun autoConsume(): IapConnector {
        shouldAutoConsume = true
        return this
    }

    /**
     * Called to purchase an item.
     */
    fun purchase(activity: Activity, skuId: String) {
        if (checkBeforeUserInteraction()) {

            val skuDetails = fetchedSkuInfosList.find { it.skuId == skuId }!!.skuDetails
            billingClient.launchBillingFlow(
                activity,
                BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build()
            )
        }
    }

    private fun checkBeforeUserInteraction(skuId: String? = null): Boolean {
        when {
            !isReady() -> billingEventListener?.onError(this, BillingResponse(ErrorType.CLIENT_NOT_READY, "Client is not ready yet"))
            skuId != null && !fetchedSkuInfosList.contains(skuId) -> {
                billingEventListener?.onError(
                    this,
                    BillingResponse(ErrorType.ITEM_NOT_EXIST, "The skuId: $skuId seems to be not existing on Google Playstore")
                )
            }
            else -> return true
        }

        return false
    }

    /**
     * To attach an event listener to establish a bridge with the caller.
     */
    fun setBillingEventListener(billingEventListener: BillingEventListener) {
        this.billingEventListener = billingEventListener
    }

    /**
     * To initialise IapConnector.
     */
    private fun init(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener { billingResult, purchases ->

                /**
                 * Only recent purchases are received here
                 */

                when (billingResult.responseCode) {
                    OK -> purchases?.let { processPurchases(purchases, false) }
                    ITEM_ALREADY_OWNED -> billingEventListener?.onError(
                        this,
                        BillingResponse(ErrorType.ITEM_ALREADY_OWNED, billingResult)
                    )
                    SERVICE_DISCONNECTED, SERVICE_TIMEOUT -> connect()
                    else -> Log.i(tag, "Init : ${billingResult.debugMessage}")
                }
            }.build()
    }

    /**
     * Connects billing client with Play console to start working with IAP.
     */
    fun connect(): IapConnector {

        val allIds = mutableListOf<String>()

        // Before we start, check input params we set empty list to null so we only have to deal with lists who are null (not provided) or not empty.
        if (nonConsumableIds.isNullOrEmpty())
            nonConsumableIds = null
        else
            allIds.addAll(nonConsumableIds!!)

        if (consumableIds.isNullOrEmpty())
            consumableIds = null
        else
            allIds.addAll(consumableIds!!)

        if (subIds.isNullOrEmpty())
            subIds = null
        else
            allIds.addAll(subIds!!)

        // Check if any list is provided.
        if (nonConsumableIds == null && consumableIds == null && subIds == null) {
            throw IllegalArgumentException("At least one list of subscriptions, non-consumables or consumables is needed")
        }

        val allIdSize = allIds.size
        val allIdSizeDistinct = allIds.distinct().size
        if (allIdSize != allIdSizeDistinct) {
            throw IllegalArgumentException("An Id must appear only once in a list. An Id must also not be in different lists")
        }


        Log.d(tag, "Billing service : Connecting...")
        if (!billingClient.isReady) {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    connected = false
                    billingEventListener?.onError(
                        this@IapConnector,
                        BillingResponse(ErrorType.CLIENT_DISCONNECTED, "Billing service : Disconnected")
                    )
                    Log.d(tag, "Billing service : Trying to establish to reconnect...")
                    billingClient.startConnection(this)
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    connected = false
                    when (billingResult.responseCode) {
                        OK -> {
                            connected = true
                            Log.d(tag, "Billing service : Connected")

                            // Check for Consumable and Non-Consumables (InAPPs)
                            val inAppIds = mutableListOf<String>()
                            consumableIds?.let { inAppIds.addAll(it) }
                            nonConsumableIds?.let { inAppIds.addAll(it) }

                            if (inAppIds.isNotEmpty())
                                querySkuDetails(INAPP, inAppIds)

                            subIds?.let {
                                querySkuDetails(SUBS, it)
                            }
                            if (!checkedForPurchasesAtStart) {
                                fetchPurchasedProducts()
                                checkedForPurchasesAtStart = true
                            }
                        }
                        BILLING_UNAVAILABLE -> Log.d(tag, "Billing service : Unavailable")
                        else -> Log.d(tag, "Billing service : Setup error")
                    }
                }
            })
        }
        return this
    }

    /**
     * Fires a query in Play console to get [SkuDetails] for provided type and IDs.
     */
    private fun querySkuDetails(skuType: String, ids: List<String>) {
        billingClient.querySkuDetailsAsync(
            SkuDetailsParams.newBuilder()
                .setSkusList(ids).setType(skuType).build()
        ) { billingResult, skuDetailsList ->
            when (billingResult.responseCode) {
                OK -> {
                    if (skuDetailsList!!.isEmpty()) {
                        Log.d(tag, "Query SKU : Data not found (List empty) seems like no SkuIDs are configured on Google Playstore!")

                        billingEventListener?.onError(
                            this,
                            BillingResponse(ErrorType.BILLING_ERROR, billingResult)
                        )
                    } else {
                        Log.d(tag, "Query SKU : Data found")

                        val fetchedSkuInfos = skuDetailsList.map {
                            generateSkuInfo(it)
                        }

                        fetchedSkuInfosList.addAll(fetchedSkuInfos)

                        when (skuType) {
                            SUBS -> {
                                billingEventListener?.onProductsFetched(fetchedSkuInfos)
                            }
                            INAPP -> {
                                billingEventListener?.onProductsFetched(fetchedSkuInfos)
                            }
                            else -> {
                                throw IllegalStateException("Not implemented SkuType")
                            }
                        }
                    }
                }
                else -> {
                    Log.d(tag, "Query SKU : Failed")
                    billingEventListener?.onError(
                        this, BillingResponse(ErrorType.BILLING_ERROR, billingResult)
                    )
                }
            }
        }
    }

    private fun generateSkuInfo(skuDetails: SkuDetails): SkuInfo {

        fun isSkuIdConsumable(skuId: String): Boolean {
            if (consumableIds.isNullOrEmpty()) return false
            return consumableIds!!.contains(skuId)
        }

        val skuProductType: SkuProductType = when (skuDetails.type) {
            SUBS -> SUBSCRIPTION
            INAPP -> {
                val consumable = isSkuIdConsumable(skuDetails.sku)
                if (consumable)
                    CONSUMABLE
                else
                    NON_CONSUMABLE
            }
            else -> throw IllegalStateException("Not implemented SkuType")
        }

        return SkuInfo(skuProductType, skuDetails)
    }

    /**
     * Load all purchases of the user and trigger the listener.
     */
    fun fetchPurchasedProducts() {
        if (billingClient.isReady) {
            val allPurchases = mutableListOf<Purchase>()

            val inAppPurchases = billingClient.queryPurchases(INAPP).purchasesList!!
            allPurchases.addAll(inAppPurchases)

            if (isSubscriptionSupported() == SupportState.SUPPORTED) {
                val subPurchases = billingClient.queryPurchases(SUBS).purchasesList!!
                allPurchases.addAll(subPurchases)
            }

            processPurchases(allPurchases, true)
        } else {
            billingEventListener?.onError(
                this,
                BillingResponse(ErrorType.FETCH_PURCHASED_PRODUCTS_ERROR, "Client not ready yet.")
            )
        }
    }

    /**
     * Checks purchase signature for more security.
     */
    private fun processPurchases(allPurchases: List<Purchase>, purchasedProductsFetched: Boolean) {
        if (allPurchases.isNotEmpty()) {
            val validPurchases = allPurchases.filter {
                isPurchaseSignatureValid(it)
            }.map { purchase ->
                val skuDetails = fetchedSkuInfosList.find { it.skuId == purchase.sku }!!.skuDetails
                PurchaseInfo(
                    generateSkuInfo(skuDetails),
                    purchase
                )
            }

            if (purchasedProductsFetched) {
                fetchedPurchasedProducts = true
                billingEventListener?.onPurchasedProductsFetched(validPurchases)
            } else
                billingEventListener?.onProductsPurchased(validPurchases)

            purchasedProductsList.addAll(validPurchases)

            validPurchases.forEach {

                // Auto Consume
                if (shouldAutoConsume) {
                    consume(it)
                }

                // Auto Acknowledge
                if (shouldAutoAcknowledge) {
                    val wasConsumedBefore = it.skuProductType == CONSUMABLE && shouldAutoConsume
                    if (!wasConsumedBefore)
                        acknowledgePurchase(it)
                }
            }
        }
    }


    /**
     * Consume consumable purchases
     * */
    fun consume(purchaseInfo: PurchaseInfo) {

        if (checkBeforeUserInteraction(purchaseInfo.skuId)) {

            when (purchaseInfo.skuProductType) {
                NON_CONSUMABLE, SUBSCRIPTION -> throw IllegalArgumentException("Only consumable products can be consumed")
                CONSUMABLE -> {
                    purchaseInfo.run {
                        billingClient.consumeAsync(
                            ConsumeParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken).build()
                        ) { billingResult, purchaseToken ->
                            when (billingResult.responseCode) {
                                OK -> {
                                    purchasedProductsList.remove(purchaseInfo)
                                    billingEventListener?.onPurchaseConsumed(this)
                                }
                                else -> {
                                    Log.d(tag, "Handling consumables : Error during consumption attempt -> ${billingResult.debugMessage}")

                                    billingEventListener?.onError(
                                        this@IapConnector,
                                        BillingResponse(ErrorType.CONSUME_ERROR, billingResult)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Purchase of subscriptions & non-consumable products must be acknowledged to Play console.
     * This will avoid refunding for these products to users by Google.
     *
     * Consumable products might be brought/consumed by users multiple times (for eg. diamonds, coins).
     * They have to be consumed or acknowledged within 3 days otherwise Google will refund the products.
     */
    fun acknowledgePurchase(purchaseInfo: PurchaseInfo) {

        if (checkBeforeUserInteraction(purchaseInfo.skuId)) {

            when (purchaseInfo.skuProductType) {
                CONSUMABLE, NON_CONSUMABLE, SUBSCRIPTION -> {
                    purchaseInfo.run {
                        billingClient.acknowledgePurchase(
                            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(
                                purchase.purchaseToken
                            ).build()
                        ) { billingResult ->
                            when (billingResult.responseCode) {
                                OK -> billingEventListener?.onPurchaseAcknowledged(this)
                                else -> {
                                    Log.d(tag, "Handling acknowledgePurchase : Error -> ${billingResult.debugMessage}")

                                    billingEventListener?.onError(
                                        this@IapConnector,
                                        BillingResponse(ErrorType.ACKNOWLEDGE_ERROR, billingResult)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun isPurchased(skuInfo: SkuInfo) = isPurchased(skuInfo.skuId)

    enum class IsPurchasedResult { CLIENT_NOT_READY, PURCHASED_PRODUCTS_NOT_FETCHED_YET, YES, NO }

    fun isPurchased(skuId: String): IsPurchasedResult {
        return when {
            !isReady() -> IsPurchasedResult.CLIENT_NOT_READY
            !fetchedPurchasedProducts -> IsPurchasedResult.PURCHASED_PRODUCTS_NOT_FETCHED_YET
            else -> { // The checks were successful
                for (p in purchasedProductsList) {
                    if (p.skuId == skuId) {
                        return IsPurchasedResult.YES
                    }
                }

                return IsPurchasedResult.NO
            }
        }
    }

    enum class SupportState {
        SUPPORTED, NOT_SUPPORTED, DISCONNECTED
    }

    /**
     * Before using subscriptions, device-support must be checked.
     */
    fun isSubscriptionSupported(): SupportState {
        val response = billingClient.isFeatureSupported(SUBSCRIPTIONS)
        return when (response.responseCode) {
            OK -> {
                Log.d(tag, "Subs support check : Success")
                SupportState.SUPPORTED
            }
            SERVICE_DISCONNECTED -> {
                connect()
                SupportState.DISCONNECTED
            }
            else -> {
                Log.d(tag, "Subs support check : Error -> ${response.responseCode} ${response.debugMessage}")
                SupportState.NOT_SUPPORTED
            }
        }
    }

    /**
     * Checks purchase signature validity
     */
    private fun isPurchaseSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(base64Key, purchase.originalJson, purchase.signature)
    }

}
