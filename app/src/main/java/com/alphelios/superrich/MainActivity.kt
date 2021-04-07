package com.alphelios.superrich

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alphelios.iap.BillingEventListener
import com.alphelios.iap.DataWrappers
import com.alphelios.iap.IapConnector
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
            .setNonConsumableIds(listOf("base", "moderate", "plenty", "quite"))
            .autoAcknowledge()
            .connect()

        iapConnector.setBillingEventListener(object : BillingEventListener {
            override fun onProductsFetched(skuDetailsList: List<DataWrappers.SkuInfo>) {
                fetchedSkuDetailsList.addAll(skuDetailsList)
                Log.d(tag, "Retrieved SKU details list : $skuDetailsList")
            }

            override fun onPurchasedProductsFetched(purchases: List<DataWrappers.PurchaseInfo>) {
                Log.d(tag, "Retrieved all purchased products : $purchases")
            }

            override fun onProductsPurchased(purchases: List<DataWrappers.PurchaseInfo>) {
                purchases.forEach {
                    when (it.skuId) {
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

            override fun onPurchaseAcknowledged(purchase: DataWrappers.PurchaseInfo) {
                Log.d(tag, "onPurchaseAcknowledged : " + purchase.skuId)
            }

            override fun onPurchaseConsumed(purchase: DataWrappers.PurchaseInfo) {
                Log.d(tag, "onPurchaseConsumed : " + purchase.skuId)
            }

            override fun onError(
                inAppConnector: IapConnector,
                result: DataWrappers.BillingResponse?
            ) {
                Log.d(tag, "Error : ${result?.message}")
            }
        })

        binding.btPurchaseCons.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.skuId == "base" } != null) {
                iapConnector.makePurchase(this, "base")
            }
        }
        binding.btnMonthly.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.skuId == "subscribe" } != null) {
                iapConnector.makePurchase(this, "subscribe")
            }
        }

        binding.btnYearly.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.skuId == "yearly" } != null) {
                iapConnector.makePurchase(this, "yearly")
            }
        }
        binding.btnQuite.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.skuId == "quite" } != null) {
                iapConnector.makePurchase(this, "quite")
            }
        }
        binding.btnModerate.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.skuId == "moderate" } != null) {
                iapConnector.makePurchase(this, "moderate")
            }
        }

        binding.btnUltimate.setOnClickListener {
            if (fetchedSkuDetailsList.find { it.skuId == "plenty" } != null) {
                iapConnector.makePurchase(this, "plenty")
            }
        }
    }
}