package com.alphelios.superrich

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alphelios.iap.BillingEventListener
import com.alphelios.iap.IapConnector
import com.alphelios.iap.model.BillingResponse
import com.alphelios.iap.model.PurchaseInfo
import com.alphelios.iap.model.SkuInfo
import com.alphelios.iap.type.ErrorType.*
import com.alphelios.superrich.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val tag: String = "IAP"
    private var fetchedSkuDetailsList = mutableListOf<SkuInfo>()
    private lateinit var iapConnector: IapConnector
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        iapConnector = IapConnector(this, "License Key")
            .setNonConsumableIds(listOf("no_ads", "super_sword"))
            .setConsumableIds(listOf("100_coins", "200_coins"))
            .autoAcknowledge()
            .autoConsume()
            .connect()

        iapConnector.setBillingEventListener(object : BillingEventListener {
            override fun onProductsFetched(skuDetailsList: List<SkuInfo>) {
                fetchedSkuDetailsList.addAll(skuDetailsList)
                Log.d(tag, "Retrieved SKU details list : $skuDetailsList")
            }

            override fun onPurchasedProductsFetched(purchases: List<PurchaseInfo>) {
                Log.d(tag, "Retrieved all purchased products : $purchases")
            }

            override fun onProductsPurchased(purchases: List<PurchaseInfo>) {
                purchases.forEach {
                    when (it.skuId) {
                        "no_ads" -> {

                        }
                        "super_sword" -> {

                        }
                        "100_coins" -> {

                        }
                        "200_coins" -> {

                        }

                    }
                }
            }

            override fun onPurchaseAcknowledged(purchase: PurchaseInfo) {
                Log.d(tag, "onPurchaseAcknowledged : " + purchase.skuId)
            }

            override fun onPurchaseConsumed(purchase: PurchaseInfo) {
                Log.d(tag, "onPurchaseConsumed : " + purchase.skuId)
            }

            override fun onError(inAppConnector: IapConnector, result: BillingResponse) {
                Log.d(tag, "Error : $result")

                when (result.errorType) {
                    CLIENT_NOT_READY_ERROR -> TODO()
                    CLIENT_DISCONNECTED -> TODO()
                    ITEM_ALREADY_OWNED_ERROR -> TODO()
                    CONSUME_ERROR -> TODO()
                    ACKNOWLEDGE_ERROR -> TODO()
                    FETCH_PURCHASED_PRODUCTS_ERROR -> TODO()
                    BILLING_ERROR -> TODO()
                }
            }
        })

        binding.btPurchaseCons.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.skuId == "base" } != null) {
                iapConnector.purchase(this, "base")
            }
        }
        binding.btnMonthly.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.skuId == "subscribe" } != null) {
                iapConnector.purchase(this, "subscribe")
            }
        }

        binding.btnYearly.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.skuId == "yearly" } != null) {
                iapConnector.purchase(this, "yearly")
            }
        }
        binding.btnQuite.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.skuId == "quite" } != null) {
                iapConnector.purchase(this, "quite")
            }
        }
        binding.btnModerate.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.skuId == "moderate" } != null) {
                iapConnector.purchase(this, "moderate")
            }
        }

        binding.btnUltimate.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.skuId == "plenty" } != null) {
                iapConnector.purchase(this, "plenty")
            }
        }
    }
}