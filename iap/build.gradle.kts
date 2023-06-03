plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

android {
    compileSdk = 33

    namespace = "com.limurse.iap"
    defaultConfig {
        minSdk = 16
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                "proguard-rules.pro"
            )
        }
    }

    kotlin {
        jvmToolchain(17)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}


dependencies {
    implementation("com.android.billingclient:billing-ktx:6.0.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    testImplementation("junit:junit:4.13.2")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.limurse"
            artifactId = "Google-IAP"
            version = "1.3.1"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
