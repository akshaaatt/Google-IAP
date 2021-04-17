package com.alphelios.richierich

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alphelios.iap.IapConnector
import com.alphelios.iap.PurchaseServiceListener
import com.alphelios.iap.SubscriptionServiceListener
import com.alphelios.richierich.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomnavview.itemIconTintList = null

        val nonConsumablesList = listOf("base")
        val consumablesList = listOf("base", "moderate", "quite", "plenty", "yearly")
        val subsList = listOf("subscription")

        val iapConnector = IapConnector(
            context = this,
            nonConsumableKeys = nonConsumablesList,
            consumableKeys = consumablesList,
            subscriptionKeys = subsList,
            key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh09sfdDzMhCh3AG9mq2EsyFUN72FBKabPpMsJyUUwXVsVJRLDWQYKmWnr0bVGsdVHwQtEDi//EY1NubXjCmViAxFnnfbdUrAk9PbRRTaMQ4taifn9fCaQ6XAW70ju3/mXuL+1xX+r8B0O9B373sP0YqiUs0b8HTtTjQoOGtcGb2KlYpQlJjjtISfVpsSk2RdKatkDyeesv+6568O7xgb5zp/KNJk8d1fMqKGWJiveFkZvedDh1ECdi3rSz1aQB+z/aEf6AIiuLzu0V8NNKjvZnxUjVJgL+lcLDrL1YZuKx9h5BX7k8lPZI7fLIVE6b6iNx3msfVdiqPkZ3s49JxA1QIDAQAB",
            enableLogging = true
        )

        iapConnector.addPurchaseListener(object : PurchaseServiceListener {
            override fun onPricesUpdated(iapKeyPrices: Map<String, String>) {
                // list of available products will be received here, so you can update UI with prices if needed
            }

            override fun onProductPurchased(sku: String?) {
                when (sku) {
                    "base" -> {

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

            override fun onProductRestored(sku: String?) {
                // will be triggered fetching owned products using IapConnector;
            }
        })

        iapConnector.addSubscriptionListener(object : SubscriptionServiceListener {
            override fun onSubscriptionRestored(sku: String?) {
                // will be triggered upon fetching owned subscription upon initialization
            }

            override fun onSubscriptionPurchased(sku: String?) {
                // will be triggered whenever subscription succeeded
                when(sku){
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