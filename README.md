[![16113251508601.jpg](https://i.postimg.cc/2yjZh36s/16113251508601.jpg)](https://postimg.cc/hzwvqDVs)
[![ezgif-com-gif-maker-3.gif](https://i.postimg.cc/cH8xyLHG/ezgif-com-gif-maker-3.gif)](https://postimg.cc/Q9hGcs1f)

Google-IAP is an Android library to handle In-App purchases and Subscriptions with minimal code.

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
    implementation 'com.github.akshaaatt:Google-IAP:1.0.7'
}
```

## Usage

#### Establishing connection with Play console

```kotlin
iapConnector = IapConnector(this, "...")
            .setNonConsumableIds(listOf("id1", "id2", "id3"))   /*pass the list of Non-Consumable Product IDs*/
            .setSubscriptionIds(listOf("subId1", "subId2"))   /*pass the list of Subscription IDs*/
            .setConsumableProductIds(listOf("id4", "id5"))  /*pass the list of Consumable Product IDs*/
            .autoAcknowledge()  /*to enable auto acknowledgement*/
            .connect()
```

#### Receiving events

```kotlin
iapConnector.setOnInAppEventsListener(object : InAppEventsListener {

            override fun onSubscriptionsFetched(skuDetailsList: List<SkuInfo>) {
                /*provides list of product details of subs type*/
            }

            override fun onInAppProductsFetched(skuDetailsList: List<SkuInfo>) {
                /*provides list of product details of inapp type*/
            }

            override fun onPurchaseAcknowledged(purchase: PurchaseInfo) {
                /*callback after purchase being acknowledged*/
            }

            override fun onProductsPurchased(purchases: List<PurchaseInfo>) {
                  purchases.forEach {
        	  Log.d(tag, "A new product was purchaed : ${it.SkuId}")
			  when (it.skuId) {
		               "id1" -> {

		                }
		                "id2" -> {

		                }
		                "id3" -> {

		                }
		                "sub1" -> {

		                }
		                "sub2" -> {

		                }
		                "id4" -> {

		                }
		                "id5" -> {

		                }
		            }
		        }
            }

             override fun onPurchaseConsumed(purchase: PurchaseInfo) {
	     	Log.d(tag, "onPurchaseConsumed : ${purchase.skuId}")
	     }

            override fun onError(inAppConnector: IapConnector, result: BillingResponse) {
                Log.d(tag, "Error : $result")

                when (result.errorType) {
                    CLIENT_NOT_READY -> TODO()
                    CLIENT_DISCONNECTED -> TODO()
                    ITEM_ALREADY_OWNED -> TODO()
                    CONSUME_ERROR -> TODO()
                    ACKNOWLEDGE_ERROR -> TODO()
                    FETCH_PURCHASED_PRODUCTS_ERROR -> TODO()
                    BILLING_ERROR -> TODO()
                }
            }
        })
```

#### Making a purchase

```kotlin
iapConnector.purchase(this, "<skuId>")
```

## Sample App

* Replace the key with your App's License Key

```kotlin
iapConnector = IapConnector(
                this, "key" // License Key
        )
```

## Contribution

You are most welcome to contribute to this project!
