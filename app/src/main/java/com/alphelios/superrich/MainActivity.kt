package com.alphelios.superrich

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alphelios.iap.DataWrappers
import com.alphelios.iap.IapConnector
import com.alphelios.iap.InAppEventsListener
import com.alphelios.superrich.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.Games
import com.google.android.gms.games.LeaderboardsClient
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val tag: String = "IAP"
    private var firstLoginAttempt = false
    private var leaderboardsClient: LeaderboardsClient? = null
    private var achievementsClient: AchievementsClient? = null
    private val signInCode = 100
    private var fetchedSkuDetailsList = mutableListOf<DataWrappers.SkuInfo>()
    private lateinit var iapConnector: IapConnector
    private lateinit var binding: ActivityMainBinding
    var score: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        signInToGoogle()
        iapConnector = IapConnector(
                this, "key" // License Key
        )
            .setInAppProductIds(listOf("base", "moderate", "plenty", "quite"))
            .setConsumableProductIds(listOf("base", "moderate", "plenty", "quite"))
            .setSubscriptionIds(listOf("subscribe", "yearly"))
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
                            achievementsClient?.unlock(getString(R.string.achievement_basic))
                            leaderboardsClient?.submitScore(getString(R.string.leaderboard_contributions),score+10)
                        }
                        "moderate" -> {
                            achievementsClient?.unlock(getString(R.string.achievement_moderate))
                            leaderboardsClient?.submitScore(getString(R.string.leaderboard_contributions),score+50)
                        }
                        "quite" -> {
                            achievementsClient?.unlock(getString(R.string.achievement_quite))
                            leaderboardsClient?.submitScore(getString(R.string.leaderboard_contributions),score+100)
                        }
                        "plenty" -> {
                            achievementsClient?.unlock(getString(R.string.achievement_ultimate))
                            leaderboardsClient?.submitScore(getString(R.string.leaderboard_contributions),score+1000)
                        }
                        "subscribe" -> {
                            achievementsClient?.unlock(getString(R.string.achievement_monthly_subscriber))
                        }
                        "yearly" -> {
                            achievementsClient?.unlock(getString(R.string.achievement_yearly_subscriber))
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

        binding.bottomnavview.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        binding.bottomnavview.itemIconTintList = null

        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.github -> {

                }
            }
            false
        }
    }

    private fun signInToGoogle() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val signInClient = GoogleSignIn.getClient(this, signInOptions)
        signInClient.silentSignIn().addOnCompleteListener(this) { task ->
            if (!task.isSuccessful) {
                if (!firstLoginAttempt) {
                    firstLoginAttempt = true
                    startActivityForResult(signInClient.signInIntent, signInCode)
                }
            } else {
                leaderboardsClient = Games.getLeaderboardsClient(this, task.result!!)
                achievementsClient = Games.getAchievementsClient(this, task.result!!)

                if (task.result != null) {
                    val client = Games.getGamesClient(this@MainActivity, task.result!!)
                    client.setViewForPopups(binding.gridView)
                }
                playerScore()
            }
        }
    }

    private fun playerScore() {
        leaderboardsClient?.loadCurrentPlayerLeaderboardScore(
                getString(R.string.leaderboard_contributions),
                LeaderboardVariant.TIME_SPAN_DAILY,
                LeaderboardVariant.COLLECTION_PUBLIC
        )?.addOnSuccessListener(this@MainActivity) { leaderboardScoreAnnotatedData ->
            if (leaderboardScoreAnnotatedData != null) {
                if (leaderboardScoreAnnotatedData.get() != null) {
                    score = leaderboardScoreAnnotatedData.get()!!.rawScore
                    Log.d("cool", score.toString())
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != signInCode) {
            return
        }
        val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data) ?: return
        if (result.isSuccess) {
            leaderboardsClient = Games.getLeaderboardsClient(this, result.signInAccount!!)
            achievementsClient = Games.getAchievementsClient(this, result.signInAccount!!)


            if (result.signInAccount != null) {
                val client = Games.getGamesClient(this@MainActivity, result.signInAccount!!)
                client.setViewForPopups(binding.gridView)
            }
            playerScore()
        }
    }

    private var mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.home -> {
            }
            R.id.achievements -> {
                achievementsClient?.achievementsIntent?.addOnSuccessListener { intent ->
                    startActivityForResult(intent, 14)
                }
            }
            R.id.leaderboard -> {
                leaderboardsClient?.getLeaderboardIntent(getString(R.string.leaderboard_contributions))?.addOnSuccessListener { intent ->
                    startActivityForResult(intent, 11)
                }
            }
        }
        false
    }
}