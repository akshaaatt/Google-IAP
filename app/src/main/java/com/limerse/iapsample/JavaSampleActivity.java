package com.limerse.iapsample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.limerse.iap.IapConnector;
import com.limerse.iap.PurchaseServiceListener;
import com.limerse.iap.SubscriptionServiceListener;
import com.limerse.iapsample.databinding.ActivityMainBinding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class JavaSampleActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        List<String> nonConsumablesList = Collections.singletonList("lifetime");
        List<String> consumablesList = Arrays.asList("base", "moderate", "quite", "plenty", "yearly");
        List<String> subsList = Collections.singletonList("subscription");

        IapConnector iapConnector = new IapConnector(
                this,
                nonConsumablesList,
                consumablesList,
                subsList,
                "LICENSE KEY",
                true
        );

        iapConnector.addPurchaseListener(new PurchaseServiceListener() {
            public void onPricesUpdated(@NotNull Map iapKeyPrices) {

            }

            public void onProductPurchased(@Nullable String sku) {
                if (sku != null) {
                    if (sku.equals("plenty")) {

                    }
                    else if (sku.equals("yearly")) {

                    }
                    else if (sku.equals("moderate")) {

                    }
                    else if (sku.equals("base")) {

                    }
                    else if (sku.equals("quite")) {

                    }
                }
            }

            public void onProductRestored(@Nullable String sku) {

            }
        });
        iapConnector.addSubscriptionListener(new SubscriptionServiceListener() {
            public void onSubscriptionRestored(@Nullable String sku) {
            }

            public void onSubscriptionPurchased(@Nullable String sku) {
                if (sku.equals("subscription")) {

                }
            }

            public void onPricesUpdated(@NotNull Map iapKeyPrices) {

            }
        });

        binding.btPurchaseCons.setOnClickListener(it ->
                iapConnector.purchase(this, "base")
        );

        binding.btnMonthly.setOnClickListener(it ->
                iapConnector.subscribe(this, "subscription")
        );

        binding.btnYearly.setOnClickListener(it ->
                iapConnector.purchase(this, "yearly")
        );

        binding.btnQuite.setOnClickListener(it ->
                iapConnector.purchase(this, "quite")
        );

        binding.btnModerate.setOnClickListener(it ->
                iapConnector.purchase(this, "moderate")
        );

        binding.btnUltimate.setOnClickListener(it ->
                iapConnector.purchase(this, "plenty")
        );
    }
}
