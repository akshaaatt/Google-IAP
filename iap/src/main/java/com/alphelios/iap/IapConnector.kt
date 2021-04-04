package com.alphelios.iap

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.*
import com.android.billingclient.api.BillingClient.FeatureType.SUBSCRIPTIONS
import com.android.billingclient.api.BillingClient.SkuType.INAPP
import com.android.billingclient.api.BillingClient.SkuType.SUBS
import java.lang.IllegalArgumentException

/**
 * Wrapper class for Google In-App purchases.
 * Handles vital processes while dealing with IAP.
 * Works around a listener [InAppEventsListener] for delivering events back to the caller.
 */
class IapConnector(context: Context, private val base64Key: String) {
    private var shouldAutoAcknowledge: Boolean = false
    private var fetchedSkuDetailsList = mutableListOf<SkuDetails>()
    private val tag = "InAppLog"
    private var inAppEventsListener: InAppEventsListener? = null

    private var inAppIds: List<String>? = null
    private var subIds: List<String>? = null
    private var consumableIds: List<String>? = null

    private lateinit var iapClient: BillingClient

    private var connected = false

    init {
        init(context)
    }


    fun isReady(debugLog: Boolean = false): Boolean {
        if (!connected) {
            Log.d(tag, "Billing client : is not ready because no connection is established yet")
        }

        if (!iapClient.isReady) {
            Log.d(tag, "Billing client : is not ready because iapClient is not ready yet")
        }

        if (fetchedSkuDetailsList.isEmpty()) {
            Log.d(
                tag,
                "Billing client : is not ready because fetchedSkuDetailsList is empty or not fetched yet"
            )
        }

        if (!connected) {
            Log.d(tag, "Billing client : is not ready because no connection is established yet")
        }

        return connected && iapClient.isReady && fetchedSkuDetailsList.isNotEmpty()
    }

    /**
     * To set INAPP product IDs.
     */
    fun setInAppProductIds(inAppIds: List<String>): IapConnector {
        this.inAppIds = inAppIds
        return this
    }

    /**
     * To set consumable product IDs.
     * Rest of the IDs will be considered non-consumable.
     */
    fun setConsumableProductIds(consumableIds: List<String>): IapConnector {
        this.consumableIds = consumableIds
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
     * To set SUBS product IDs.
     */
    fun setSubscriptionIds(subIds: List<String>): IapConnector {
        this.subIds = subIds
        return this
    }

    /**
     * Called to purchase an item.
     * Its result is received in [PurchasesUpdatedListener] which further is handled
     * by [handleConsumableProducts] / [handleNonConsumableProducts].
     */
    fun makePurchase(activity: Activity, sku: String) {
        if (fetchedSkuDetailsList.isEmpty())
            inAppEventsListener?.onError(this, DataWrappers.BillingResponse("Products not fetched"))
        else
            iapClient.launchBillingFlow(
                activity,
                BillingFlowParams.newBuilder()
                    .setSkuDetails(fetchedSkuDetailsList.find { it.sku == sku }!!).build()
            )
    }

    /**
     * To attach an event listener to establish a bridge with the caller.
     */
    fun setOnInAppEventsListener(inAppEventsListener: InAppEventsListener) {
        this.inAppEventsListener = inAppEventsListener
    }

    /**
     * To initialise IapConnector.
     */
    private fun init(context: Context) {
        iapClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener { billingResult, purchases ->
                /**
                 * Only recent purchases are received here
                 */

                when (billingResult.responseCode) {
                    OK -> purchases?.let { processPurchases(purchases) }
                    ITEM_ALREADY_OWNED -> inAppEventsListener?.onError(
                        this,
                        billingResult.run {
                            DataWrappers.BillingResponse(
                                debugMessage,
                                responseCode
                            )
                        }
                    )
                    SERVICE_DISCONNECTED -> connect()
                    else -> Log.i(tag, "Purchase update : ${billingResult.debugMessage}")
                }
            }.build()
    }

    /**
     * Connects billing client with Play console to start working with IAP.
     */
    fun connect(): IapConnector {

        // Before we start, check input params
        inAppIds?.let { if (it.isEmpty()) throw IllegalArgumentException("The passed parameter: inAppIds is an empty list, please do not pass a parameter if it is empty") }
        subIds?.let { if (it.isEmpty())  throw IllegalArgumentException("The passed parameter: subIds is an empty list, please do not pass a parameter if it is empty") }
        consumableIds?.let { if (it.isEmpty())  throw IllegalArgumentException("The passed parameter: consumableIds is an empty list, please do not pass a parameter if it is empty") }

        Log.d(tag, "Billing service : Connecting...")
        if (!iapClient.isReady) {
            iapClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    connected = false
                    inAppEventsListener?.onError(
                        this@IapConnector,
                        DataWrappers.BillingResponse("Billing service : Disconnected")
                    )
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    connected = false
                    when (billingResult.responseCode) {
                        OK -> {
                            connected = true
                            Log.d(tag, "Billing service : Connected")
                            inAppIds?.let {
                                querySku(INAPP, it)
                            }
                            subIds?.let {
                                querySku(SUBS, it)
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
    private fun querySku(skuType: String, ids: List<String>) {
        iapClient.querySkuDetailsAsync(
            SkuDetailsParams.newBuilder()
                .setSkusList(ids).setType(skuType).build()
        ) { billingResult, skuDetailsList ->
            when (billingResult.responseCode) {
                OK -> {
                    if (skuDetailsList!!.isEmpty()) {
                        Log.d(tag, "Query SKU : Data not found (List empty)")
                        inAppEventsListener?.onError(
                            this,
                            billingResult.run {
                                DataWrappers.BillingResponse(
                                    debugMessage,
                                    responseCode
                                )
                            })
                    } else {
                        Log.d(tag, "Query SKU : Data found")

                        fetchedSkuDetailsList.addAll(skuDetailsList)

                        val fetchedSkuInfo = skuDetailsList.map {
                            getSkuInfo(it)
                        }

                        if (skuType == SUBS)
                            inAppEventsListener?.onSubscriptionsFetched(fetchedSkuInfo)
                        else
                            inAppEventsListener?.onInAppProductsFetched(fetchedSkuInfo.map {
                                it.isConsumable = consumableIds.contains(it.sku)
                                it
                            })
                    }
                }
                else -> {
                    Log.d(tag, "Query SKU : Failed")
                    inAppEventsListener?.onError(
                        this,
                        billingResult.run {
                            DataWrappers.BillingResponse(
                                debugMessage,
                                responseCode
                            )
                        }
                    )
                }
            }
        }
    }

    private fun getSkuInfo(skuDetails: SkuDetails): DataWrappers.SkuInfo {
        return DataWrappers.SkuInfo(
            skuDetails.sku,
            skuDetails.description,
            skuDetails.freeTrialPeriod,
            skuDetails.iconUrl,
            skuDetails.introductoryPrice,
            skuDetails.introductoryPriceAmountMicros,
            skuDetails.introductoryPriceCycles,
            skuDetails.introductoryPricePeriod,
            skuDetails.originalJson,
            skuDetails.originalPrice,
            skuDetails.originalPriceAmountMicros,
            skuDetails.price,
            skuDetails.priceAmountMicros,
            skuDetails.priceCurrencyCode,
            skuDetails.subscriptionPeriod,
            skuDetails.title,
            skuDetails.type
        )
    }

    /**
     * Returns all the **non-consumable** purchases of the user.
     */
    fun getAllPurchases() {
        if (iapClient.isReady) {
            val allPurchases = mutableListOf<Purchase>()
            allPurchases.addAll(iapClient.queryPurchases(INAPP).purchasesList!!)
            if (isSubSupportedOnDevice())
                allPurchases.addAll(iapClient.queryPurchases(SUBS).purchasesList!!)
            processPurchases(allPurchases)
        } else {
            inAppEventsListener?.onError(
                this,
                DataWrappers.BillingResponse("Client not initialized yet.")
            )
        }
    }

    /**
     * Checks purchase signature for more security.
     */
    private fun processPurchases(allPurchases: List<Purchase>) {
        if (allPurchases.isNotEmpty()) {
            val validPurchases = allPurchases.filter {
                isPurchaseSignatureValid(it)
            }.map { purchase ->
                DataWrappers.PurchaseInfo(
                    getSkuInfo(fetchedSkuDetailsList.find { it.sku == purchase.sku }!!),
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
                    purchase.sku,
                    purchase.accountIdentifiers
                )
            }

            inAppEventsListener?.onProductsPurchased(validPurchases)

            if (shouldAutoAcknowledge)
                validPurchases.forEach {
                    acknowledgePurchase(it)
                }
        }
    }

    /**
     * Purchase of non-consumable products must be acknowledged to Play console.
     * This will avoid refunding for these products to users by Google.
     *
     * Consumable products might be brought/consumed by users multiple times (for eg. diamonds, coins).
     * Hence, it is necessary to notify Play console about such products.
     */
    private fun acknowledgePurchase(purchase: DataWrappers.PurchaseInfo) {
        purchase.run {
            if (consumableIds.contains(sku))
                iapClient.consumeAsync(
                    ConsumeParams.newBuilder()
                        .setPurchaseToken(purchaseToken).build()
                ) { billingResult, purchaseToken ->
                    when (billingResult.responseCode) {
                        OK -> inAppEventsListener?.onPurchaseAcknowledged(this)
                        else -> {
                            Log.d(
                                tag,
                                "Handling consumables : Error -> ${billingResult.debugMessage}"
                            )
                            inAppEventsListener?.onError(
                                this@IapConnector,
                                billingResult.run {
                                    DataWrappers.BillingResponse(
                                        debugMessage,
                                        responseCode
                                    )
                                }
                            )
                        }
                    }
                } else
                iapClient.acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder().setPurchaseToken(
                        purchaseToken
                    ).build()
                ) { billingResult ->
                    when (billingResult.responseCode) {
                        OK -> inAppEventsListener?.onPurchaseAcknowledged(this)
                        else -> {
                            Log.d(
                                tag,
                                "Handling non consumables : Error -> ${billingResult.debugMessage}"
                            )
                            inAppEventsListener?.onError(
                                this@IapConnector,
                                billingResult.run {
                                    DataWrappers.BillingResponse(
                                        debugMessage,
                                        responseCode
                                    )
                                }
                            )
                        }
                    }
                }
        }
    }

    /**
     * Before using subscriptions, device-support must be checked.
     */
    private fun isSubSupportedOnDevice(): Boolean {
        var isSupported = false
        when (iapClient.isFeatureSupported(SUBSCRIPTIONS).responseCode) {
            OK -> {
                isSupported = true
                Log.d(tag, "Subs support check : Success")
            }
            SERVICE_DISCONNECTED -> connect()
            else -> Log.d(tag, "Subs support check : Error")
        }
        return isSupported
    }

    /**
     * Checks purchase signature validity
     */
    private fun isPurchaseSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(
            base64Key, purchase.originalJson, purchase.signature
        )
    }
}