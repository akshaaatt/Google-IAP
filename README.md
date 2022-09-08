<h1 align="center">Google-IAP (Play Billing Library Version 5.0.0)</h1>

<p align="center">
    <img src="https://jitpack.io/v/akshaaatt/Google-IAP.svg?style=flat-square&logo=github&logoColor=white"
         alt="GitHub issues">
    <a href="https://jitpack.io/#akshaaatt/Google-IAP">
    <a href="https://play.google.com/store/apps/details?id=com.redalck.dtu_rm">
       <img src="https://PlayBadges.pavi2410.me/badge/downloads?id=com.redalck.dtu_rm">
    </a>
    <a href="https://github.com/akshaaatt/Google-IAP/commits/master">
    <img src="https://img.shields.io/github/last-commit/akshaaatt/Google-IAP.svg?style=flat-square&logo=github&logoColor=white"
         alt="GitHub last commit">
    <a href="https://github.com/akshaaatt/Google-IAP/issues">
    <img src="https://img.shields.io/github/issues-raw/akshaaatt/Google-IAP.svg?style=flat-square&logo=github&logoColor=white"
         alt="GitHub issues">
    <a href="https://github.com/akshaaatt/Google-IAP/pulls">
    <img src="https://img.shields.io/github/issues-pr-raw/akshaaatt/Google-IAP.svg?style=flat-square&logo=github&logoColor=white"
         alt="GitHub pull requests">
</p>
      
<p align="center">
  <a href="#features">Features</a> •
  <a href="#development">Development</a> •
  <a href="#usage">Usage</a> •
  <a href="#license">License</a> •
  <a href="#contribution">Contribution</a>
</p>

<h1 align="center">
  <br>
  <a href="https://github.com/akshaaatt/Google-IAP/archive/master.zip"><img src="https://i.postimg.cc/wMCccWJH/4910241.jpg" alt="Google-IAP"></a>
</h1>
	    
---

[![ezgif-com-gif-maker-3.gif](https://i.postimg.cc/cH8xyLHG/ezgif-com-gif-maker-3.gif)](https://postimg.cc/Q9hGcs1f)

IAP is an Android library to handle In-App purchases with minimal code.

## Features

* Written in Kotlin
* No boilerplate code
* Easy initialization
* Supports InApp & Subscription products
* Simple configuration for consumable products

## Gradle Dependency

* Add the JitPack repository to your project's build.gradle file

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
* Add the dependency in your app's build.gradle file

```groovy
dependencies {
    implementation 'com.github.akshaaatt:Google-IAP:1.2.2'
}
```
        
## Development
	    
* Prerequisite: Latest version of the Android Studio and SDKs on your pc.
* Clone this repository.
* Use the `gradlew build` command to build the project directly or use the IDE to run the project to your phone or the emulator.

## Usage

#### Establishing connection with Play console

```kotlin
 val iapConnector = IapConnector(
            context = this, // activity / context
            nonConsumableKeys = nonConsumablesList, // pass the list of non-consumables
            consumableKeys = consumablesList, // pass the list of consumables
            subscriptionKeys = subsList, // pass the list of subscriptions
            key = "LICENSE KEY" // pass your app's license key
            enableLogging = true // to enable / disable logging
        )
```

#### Receiving events

```kotlin
 iapConnector.addPurchaseListener(object : PurchaseServiceListener {
            override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.SkuDetails>) {
                // list of available products will be received here, so you can update UI with prices if needed
            }

            override fun onProductPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
               // will be triggered whenever purchase succeeded
            }

            override fun onProductRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered fetching owned products using IapConnector
            }
        })

 iapConnector.addSubscriptionListener(object : SubscriptionServiceListener {
            override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered upon fetching owned subscription upon initialization
            }

            override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered whenever subscription succeeded
            }

            override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.SkuDetails>) {
                // list of available products will be received here, so you can update UI with prices if needed
            }
        })

```

#### Making a purchase

```kotlin
iapConnector.purchase(this, "<sku>")
```

#### Making a subscription

```kotlin
iapConnector.susbcribe(this, "<sku>")
```

#### Removing a subscription

```kotlin
iapConnector.unsubscribe(this, "<sku>")
```

## Sample App

* Add your products to the developer console

* Replace the key with your App's License Key


## Apps Using this Library

* https://play.google.com/store/apps/details?id=com.redalck.gameone

* https://play.google.com/store/apps/details?id=com.redalck.gametwo

* https://play.google.com/store/apps/details?id=com.redalck.gamethree

* https://play.google.com/store/apps/details?id=com.redalck.gamefour

* https://play.google.com/store/apps/details?id=com.redalck.gamefive
	    
* https://play.google.com/store/apps/details?id=com.redalck.gamesix
	    
* https://play.google.com/store/apps/details?id=com.redalck.gameseven
	    
* https://play.google.com/store/apps/details?id=com.redalck.gameeight

* https://play.google.com/store/apps/details?id=daily.status.earn.money
	    
## License

This Project is licensed under the [GPL version 3 or later](https://www.gnu.org/licenses/gpl-3.0.html).

## Contribution

You are most welcome to contribute to this project!
