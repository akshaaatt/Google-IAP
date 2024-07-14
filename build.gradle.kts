buildscript {
    repositories {
        google()
        mavenCentral()
    }

    val kotlinVersion = "2.0.0"
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
