import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 34

    namespace = "com.limurse.iapsample"
    defaultConfig {
        applicationId = "com.limurse.iapsample"
        minSdk = 21
        targetSdk = 34
        versionCode = 7
        versionName = "1.0.6"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        val keystoreProperties = Properties().apply {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                load(keystorePropertiesFile.inputStream())
            }
        }
        val licenseKey = keystoreProperties.getProperty("licenseKey")
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                "proguard-rules.pro"
            )
            resValue("string", "licenseKey", licenseKey)
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                "proguard-rules.pro"
            )
            resValue("string", "licenseKey", licenseKey)
        }
    }

    buildFeatures {
        viewBinding = true
    }

    kotlin {
        jvmToolchain(17)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":iap"))

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.intuit.sdp:sdp-android:1.1.1")
}
