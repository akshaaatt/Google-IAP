[![16113251508601.jpg](https://i.postimg.cc/2yjZh36s/16113251508601.jpg)](https://postimg.cc/hzwvqDVs)
[![ezgif-com-gif-maker-3.gif](https://i.postimg.cc/cH8xyLHG/ezgif-com-gif-maker-3.gif)](https://postimg.cc/Q9hGcs1f)

IAP is an Android library to handle In-App purchases with minimal code.

## Features

* Written in Kotlin
* No boilerplate code
* Easy initialisation
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
    implementation 'com.github.akshaaatt:Google-IAP:1.1.0'
}
```

## Usage

#### Establishing connection with Play console

```kotlin
 val iapConnector = IapConnector(
            this,
            skuList, // pass the list of in-app products
            subsList, // pass the list of subscriptions
            "LICENSE KEY", // pass your app's license key
            true // to enable/disable logging
        )
```

#### Receiving events

```kotlin
 iapConnector.addPurchaseListener(object : PurchaseServiceListener {
            override fun onPricesUpdated(iapKeyPrices: Map<String, String>) {
                // list of available products will be received here, so you can update UI with prices if needed
            }

            override fun onProductPurchased(sku: String?) {
               // will be triggered whenever purchase succeeded
            }

            override fun onProductRestored(sku: String?) {
                // will be triggered fetching owned products using IAPManager.init();
            }
        })

 iapConnector.addSubscriptionListener(object : SubscriptionServiceListener {
            override fun onSubscriptionRestored(sku: String?) {
                // will be triggered upon fetching owned subscription upon initialization
            }

            override fun onSubscriptionPurchased(sku: String?) {
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

## Sample App

* Add your products to the developer console

* Replace the key with your App's License Key

## Contribution

You are most welcome to contribute to this project!
