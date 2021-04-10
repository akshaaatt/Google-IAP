package com.alphelios.richierich

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alphelios.iap.BillingEventListener
import com.alphelios.iap.IapConnector
import com.alphelios.iap.model.BillingResponse
import com.alphelios.iap.model.PurchaseInfo
import com.alphelios.iap.model.SkuInfo
import com.alphelios.richierich.databinding.ActivityMainBinding

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
            .setNonConsumableInAppIds("no_ads", "super_sword")
            .setConsumableInAppIds("base","yearly","quite","moderate", "plenty")
            .setSubscriptionIds("subscribe")
            .autoAcknowledge()
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

            override fun onConsumed(purchase: PurchaseInfo) {
                Log.d(tag, "onConsumed : " + purchase.skuId)
            }

            override fun onError(inAppConnector: IapConnector, result: BillingResponse) {
                Log.d(tag, "Error : $result")
            }
        })

        binding.btPurchaseCons.setOnClickListener {
            iapConnector.purchase(this, "base")
        }
        binding.btnMonthly.setOnClickListener {
            iapConnector.purchase(this, "subscribe")
        }

        binding.btnYearly.setOnClickListener {
            iapConnector.purchase(this, "yearly")
        }
        binding.btnQuite.setOnClickListener {
            iapConnector.purchase(this, "quite")
        }
        binding.btnModerate.setOnClickListener {
            iapConnector.purchase(this, "moderate")
        }

        binding.btnUltimate.setOnClickListener {
            iapConnector.purchase(this, "plenty")
        }
    }
}