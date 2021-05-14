import android.content.Context
import com.limerse.iap.IapConnector
import com.limerse.iap.PurchaseServiceListener
import com.limerse.iap.SubscriptionServiceListener
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class BillingTest {
    @Mock
    var context: Context? = null
    @Test
    @Throws(Exception::class)
    fun onProductOwnedEvent_eachRegisteredListenerShouldBeTriggered() {
        val iapConnector = IapConnector(context!!, ArrayList(), ArrayList())
        val firstListener = Mockito.spy(
            PurchaseServiceListener::class.java
        )
        iapConnector.addPurchaseListener(firstListener)
        val secondListener = Mockito.spy(
            PurchaseServiceListener::class.java
        )
        iapConnector.addPurchaseListener(secondListener)
        iapConnector.getBillingService()
            .productOwnedInternal(TestConstants.TEST_SKU, false)
        Mockito.verify(firstListener, Mockito.times(1))
            .onProductPurchased(TestConstants.TEST_SKU)
        Mockito.verify(secondListener, Mockito.times(1))
            .onProductPurchased(TestConstants.TEST_SKU)
    }

    @Test
    fun onProductDetailsFetched_eachRegisteredListenerShouldBeTriggered() {
        val iapConnector = IapConnector(context!!, ArrayList<String>(), ArrayList<String>())
        val firstListener = Mockito.spy(
            PurchaseServiceListener::class.java
        )
        iapConnector.addPurchaseListener(firstListener)
        val secondListener = Mockito.spy(
            PurchaseServiceListener::class.java
        )
        iapConnector.addPurchaseListener(secondListener)
        val products: MutableMap<String, String> = HashMap()
        products[TestConstants.TEST_SKU] = TestConstants.Companion.TEST_PRODUCT
        products[TestConstants.TEST_SKU_1] = TestConstants.Companion.TEST_PRODUCT
        iapConnector.getBillingService().updatePricesInternal(products)
        Mockito.verify(firstListener, Mockito.times(1)).onPricesUpdated(products)
        Mockito.verify(secondListener, Mockito.times(1)).onPricesUpdated(products)
    }

    @Test
    fun onIapManagerInteraction_onlyRegisteredListenersShouldBeTriggered() {
        val iapConnector = IapConnector(context!!, ArrayList<String>(), ArrayList<String>())
        val firstListener = Mockito.spy(
            PurchaseServiceListener::class.java
        )
        iapConnector.addPurchaseListener(firstListener)
        val secondListener = Mockito.spy(
            PurchaseServiceListener::class.java
        )
        iapConnector.addPurchaseListener(secondListener)
        iapConnector.removePurchaseListener(firstListener)
        iapConnector.getBillingService().productOwnedInternal(TestConstants.Companion.TEST_SKU, false)
        Mockito.verify(firstListener, Mockito.never()).onProductPurchased(Mockito.anyString())
        Mockito.verify(secondListener, Mockito.times(1))
            .onProductPurchased(TestConstants.Companion.TEST_SKU)
    }

    @Test
    fun onProductPurchase_purchaseCallbackShouldBeTriggered() {
        val iapConnector = IapConnector(context!!, ArrayList<String>(), ArrayList<String>())
        val listener = Mockito.spy(
            PurchaseServiceListener::class.java
        )
        iapConnector.addPurchaseListener(listener)
        iapConnector.getBillingService().productOwnedInternal(TestConstants.Companion.TEST_SKU, false)
        Mockito.verify(listener, Mockito.times(1))
            .onProductPurchased(TestConstants.Companion.TEST_SKU)
    }

    @Test
    fun onProductRestore_restoreCallbackShouldBeTriggered() {
        val iapConnector = IapConnector(context!!, ArrayList<String>(), ArrayList<String>())
        val listener = Mockito.spy(
            PurchaseServiceListener::class.java
        )
        iapConnector.addPurchaseListener(listener)
        iapConnector.getBillingService().productOwnedInternal(TestConstants.TEST_SKU, true)
        Mockito.verify(listener, Mockito.times(1))
            .onProductRestored(TestConstants.TEST_SKU)
    }

    @Test
    fun onSubscriptionPurchase_purchaseCallbackShouldBeTriggered() {
        val iapConnector = IapConnector(context!!, ArrayList<String>(), ArrayList<String>())
        val listener: SubscriptionServiceListener = Mockito.spy(
            SubscriptionServiceListener::class.java
        )
        iapConnector.addSubscriptionListener(listener)
        iapConnector.getBillingService()
            .subscriptionOwnedInternal(TestConstants.TEST_SKU, false)
        Mockito.verify(listener, Mockito.times(1))
            .onSubscriptionPurchased(TestConstants.TEST_SKU)
    }

    @Test
    fun onSubscriptionRestore_restoreCallbackShouldBeTriggered() {
        val iapConnector = IapConnector(context!!, ArrayList<String>(), ArrayList<String>())
        val listener: SubscriptionServiceListener = Mockito.spy(
            SubscriptionServiceListener::class.java
        )
        iapConnector.addSubscriptionListener(listener)
        iapConnector.getBillingService()
            .subscriptionOwnedInternal(TestConstants.TEST_SKU, true)
        Mockito.verify(listener, Mockito.times(1))
            .onSubscriptionRestored(TestConstants.TEST_SKU)
    }
}