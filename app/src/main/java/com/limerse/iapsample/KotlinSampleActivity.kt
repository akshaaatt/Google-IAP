package com.limerse.iapsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.limerse.iap.DataWrappers
import com.limerse.iap.IapConnector
import com.limerse.iap.PurchaseServiceListener
import com.limerse.iap.SubscriptionServiceListener
import com.limerse.iapsample.databinding.ActivityMainBinding

class KotlinSampleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomnavview.itemIconTintList = null

        val nonConsumablesList = listOf("lifetime")
        val consumablesList = listOf("base", "moderate", "quite", "plenty", "yearly")
        val subsList = listOf("subscription")

        val iapConnector = IapConnector(
            context = this,
            nonConsumableKeys = nonConsumablesList,
            consumableKeys = consumablesList,
            subscriptionKeys = subsList,
            key = "LICENSE KEY",
            enableLogging = true
        )

        iapConnector.addPurchaseListener(object : PurchaseServiceListener {
            override fun onPricesUpdated(iapKeyPrices: Map<String, String>) {
                // list of available products will be received here, so you can update UI with prices if needed
            }

            override fun onProductPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                when (purchaseInfo.sku) {
                    "base" -> {
                        purchaseInfo.orderId
                    }
                    "moderate" -> {

                    }
                    "quite" -> {

                    }
                    "plenty" -> {

                    }
                    "yearly" -> {

                    }
                }
            }

            override fun onProductRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered fetching owned products using IapConnector;
            }
        })

        iapConnector.addSubscriptionListener(object : SubscriptionServiceListener {
            override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered upon fetching owned subscription upon initialization
            }

            override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered whenever subscription succeeded
                when(purchaseInfo.sku){
                    "subscription"->{

                    }
                }
            }

            override fun onPricesUpdated(iapKeyPrices: Map<String, String>) {
                // list of available products will be received here, so you can update UI with prices if needed
            }
        })

        binding.btPurchaseCons.setOnClickListener {
            iapConnector.purchase(this, "base")
        }
        binding.btnMonthly.setOnClickListener {
            iapConnector.subscribe(this, "subscription")
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