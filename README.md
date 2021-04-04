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
    implementation 'com.github.akshaaatt:Google-IAP:1.0.5'
}
```

## Usage

#### Establishing connection with Play console

```kotlin
iapConnector = IapConnector(this, "...")
            .setInAppProductIds(listOf("id1", "id2"))   /*pass the list of INAPP IDs*/
            .setSubscriptionIds(listOf("id1", "id2"))   /*pass the list of SUBS IDs*/
            .setConsumableProductIds(listOf("id1", "id2"))  /*pass the list of consumable product IDs*/
            .autoAcknowledge()  /*to enable auto acknowledgement*/
            .connect()
```

#### Receiving events

```kotlin
iapConnector.setOnInAppEventsListener(object : InAppEventsListener {

            override fun onSubscriptionsFetched(skuDetailsList: List<DataWrappers.SkuInfo>) {
                /*provides list of product details of subs type*/
            }

            override fun onInAppProductsFetched(skuDetailsList: List<DataWrappers.SkuInfo>) {
                /*provides list of product details of inapp type*/
            }

            override fun onPurchaseAcknowledged(purchase: DataWrappers.PurchaseInfo) {
                /*callback after purchase being acknowledged*/
            }

            override fun onProductsPurchased(purchases: List<DataWrappers.PurchaseInfo>) {
                /*provides recent purchases*/
            }

            override fun onError(inAppConnector: IapConnector, result: DataWrappers.BillingResponse?) {
                /*provides error message if anything goes wrong*/
            }
        })
```

#### Making a purchase

```kotlin
iapConnector.makePurchase(this, "<sku>")
```

## Sample App

* Replace the key with your App's License Key

```kotlin
iapConnector = IapConnector(
                this, "key" // License Key
        )
```
* Replace the games_ids.xml with your App's resources from the Play Console

```kotlin
achievementsClient?.unlock(getString(R.string.achievement_ultimate))
```

* Replace the default_web_client_id with your App's client_id from the Play Sign In

```kotlin
requestIdToken(getString(R.string.default_web_client_id))
```

## Contribution

You are most welcome to contribute to this project!
