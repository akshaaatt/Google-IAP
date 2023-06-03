import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 33

    namespace = "com.limurse.iapsample"
    defaultConfig {
        applicationId = "com.limurse.iapsample"
        minSdk = 21
        targetSdk = 33
        versionCode = 4
        versionName = "1.0.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val localProperties = Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            load(localPropertiesFile.inputStream())
        }
    }

    val keystoreProperties = Properties().apply {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        if (keystorePropertiesFile.exists()) {
            load(keystorePropertiesFile.inputStream())
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                "proguard-rules.pro"
            )
            when {
                !keystoreProperties.isEmpty -> {
                    val licenseKey = keystoreProperties.getProperty("licenseKey")
                    resValue("string", "licenseKey", licenseKey)
                }
                else -> {
                    resValue("string", "licenseKey", "test")
                }
            }
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                "proguard-rules.pro"
            )
            when {
                !localProperties.isEmpty -> {
                    val licenseKey = localProperties.getProperty("licenseKey")
                    resValue("string", "licenseKey", licenseKey)
                }
                else -> {
                    resValue("string", "licenseKey", "test")
                }
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    kotlin {
        jvmToolchain(8)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(project(":iap"))

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.intuit.sdp:sdp-android:1.1.0")
}
