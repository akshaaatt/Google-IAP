package com.aemerse.iap

interface BillingClientConnectionListener {
    fun onConnected(status: Boolean, billingResponseCode: Int)
}