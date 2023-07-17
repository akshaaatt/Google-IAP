package com.limurse.iapsample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.limurse.iap.BillingClientConnectionListener
import com.limurse.iap.DataWrappers
import com.limurse.iap.IapConnector
import com.limurse.iap.PurchaseServiceListener
import com.limurse.iap.SubscriptionServiceListener
import com.limurse.iapsample.databinding.ActivityMainBinding

class KotlinSampleActivity : AppCompatActivity() {

    private lateinit var iapConnector: IapConnector
    private lateinit var binding: ActivityMainBinding

    val isBillingClientConnected: MutableLiveData<Boolean> = MutableLiveData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomnavview.itemIconTintList = null
        isBillingClientConnected.value = false

        val nonConsumablesList = listOf("lifetime")
        val consumablesList = listOf("base", "moderate", "quite")
        val subsList = listOf("subscription", "yearly")

        iapConnector = IapConnector(
            context = this,
            nonConsumableKeys = nonConsumablesList,
            consumableKeys = consumablesList,
            subscriptionKeys = subsList,
            key = getString(R.string.licenseKey),
            enableLogging = true
        )

        iapConnector.addBillingClientConnectionListener(object : BillingClientConnectionListener {
            override fun onConnected(status: Boolean, billingResponseCode: Int) {
                Log.d(
                    "KSA",
                    "This is the status: $status and response code is: $billingResponseCode"
                )
                isBillingClientConnected.value = status
            }
        })

        iapConnector.addPurchaseListener(object : PurchaseServiceListener {
            override fun onPricesUpdated(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>) {
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
                when (purchaseInfo.sku) {
                    "subscription" -> {

                    }
                    "yearly" -> {

                    }
                }
            }

            override fun onPricesUpdated(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>) {
                // list of available products will be received here, so you can update UI with prices if needed
            }
        })

        isBillingClientConnected.observe(this) {connected->
            Log.d("KSA", "limurse $connected")
            when(connected) {
                true -> {
                    binding.btPurchaseCons.isEnabled = true
                    binding.btnMonthly.isEnabled = true
                    binding.btnYearly.isEnabled = true
                    binding.btnQuite.isEnabled = true
                    binding.btnModerate.isEnabled = true
                    binding.btnUltimate.isEnabled = true

                    binding.btPurchaseCons.setOnClickListener {
                        iapConnector.purchase(this, "base")
                    }
                    binding.btnMonthly.setOnClickListener {
                        iapConnector.subscribe(this, "subscription")
                    }

                    binding.btnYearly.setOnClickListener {
                        iapConnector.subscribe(this, "yearly")
                    }
                    binding.btnQuite.setOnClickListener {
                        iapConnector.purchase(this, "quite")
                    }
                    binding.btnModerate.setOnClickListener {
                        iapConnector.purchase(this, "moderate")
                    }

                    binding.btnUltimate.setOnClickListener {
                        iapConnector.purchase(this, "lifetime")
                    }
                }
                else -> {
                    binding.btPurchaseCons.isEnabled = false
                    binding.btnMonthly.isEnabled = false
                    binding.btnYearly.isEnabled = false
                    binding.btnQuite.isEnabled = false
                    binding.btnModerate.isEnabled = false
                    binding.btnUltimate.isEnabled = false
                }
            }
        }
    }
}