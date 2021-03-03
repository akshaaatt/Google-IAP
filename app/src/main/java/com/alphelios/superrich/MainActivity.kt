package com.alphelios.superrich

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alphelios.iap.DataWrappers
import com.alphelios.iap.IapConnector
import com.alphelios.iap.InAppEventsListener
import com.alphelios.superrich.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val tag: String = "IAP"
    private var fetchedSkuDetailsList = mutableListOf<DataWrappers.SkuInfo>()
    private lateinit var iapConnector: IapConnector
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        iapConnector = IapConnector(
                this, "key" // License Key
        )
                .setInAppProductIds(listOf("base", "moderate", "plenty", "quite"))
                .setConsumableProductIds(listOf("base", "moderate", "plenty", "quite"))
                .setSubscriptionIds(listOf())
                .autoAcknowledge()
                .connect()

        iapConnector.setOnInAppEventsListener(object : InAppEventsListener {

            override fun onSubscriptionsFetched(skuDetailsList: List<DataWrappers.SkuInfo>) {
                fetchedSkuDetailsList.addAll(skuDetailsList)
                Log.d(tag, "Retrieved SKU details list : $skuDetailsList")
            }

            override fun onInAppProductsFetched(skuDetailsList: List<DataWrappers.SkuInfo>) {
                fetchedSkuDetailsList.addAll(skuDetailsList)
                Log.d(tag, "Retrieved SKU details list : $skuDetailsList")
            }

            override fun onPurchaseAcknowledged(purchase: DataWrappers.PurchaseInfo) {
                Log.d(tag, "onPurchaseAcknowledged")
            }

            override fun onProductsPurchased(purchases: List<DataWrappers.PurchaseInfo>) {
                purchases.forEach {
                    when (it.sku) {
                        "base" -> {

                        }
                        "moderate" -> {

                        }
                        "quite" -> {

                        }
                        "plenty" -> {

                        }
                        "subscribe" -> {

                        }
                        "yearly" -> {

                        }
                    }
                }
            }

            override fun onError(
                    inAppConnector: IapConnector,
                    result: DataWrappers.BillingResponse?
            ) {
                Log.d(tag, "Error : ${result?.message}")
            }
        })

        binding.btPurchaseCons.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.sku == "base" } != null) {
                iapConnector.makePurchase("base")
            }
        }
        binding.btnMonthly.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.sku == "subscribe" } != null) {
                iapConnector.makePurchase("subscribe")
            }
        }

        binding.btnYearly.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.sku == "yearly" } != null) {
                iapConnector.makePurchase("yearly")
            }
        }
        binding.btnQuite.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.sku == "quite" } != null) {
                iapConnector.makePurchase("quite")
            }
        }
        binding.btnModerate.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.sku == "moderate" } != null) {
                iapConnector.makePurchase("moderate")
            }
        }

        binding.btnUltimate.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.sku == "plenty" } != null) {
                iapConnector.makePurchase("plenty")
            }
        }
    }
}