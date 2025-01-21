import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    namespace = "com.limurse.iapsample"
    defaultConfig {
        applicationId = "com.limurse.iapsample"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 9
        versionName = "1.1.0"
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
            proguardFiles("proguard-rules.pro")
            resValue("string", "licenseKey", licenseKey)
        }
        debug {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
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

    implementation(libs.appcompat)
    implementation(libs.gridlayout)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.sdp.android)
}