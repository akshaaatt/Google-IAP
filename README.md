<h1 align="center">Google-IAP</h1>

<p align="center">
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

---

## Play Billing Library Version 4.0.0


[![16113251508601.jpg](https://i.postimg.cc/2yjZh36s/16113251508601.jpg)](https://postimg.cc/hzwvqDVs)
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

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

* Add the dependency in your app's build.gradle file

```
dependencies {
    implementation 'com.github.akshaaatt:Google-IAP:1.1.8'
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
            override fun onPricesUpdated(iapKeyPrices: Map<String, String>) {
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

            override fun onPricesUpdated(iapKeyPrices: Map<String, String>) {
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
