plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") version "4.3.15"
}

android {
    namespace = "com.example.arabsignapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.arabsignapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("com.google.firebase:firebase-firestore-ktx:24.0.0")
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
dependencies {
    implementation("androidx.camera:camera-core:1.2.0")
    implementation("androidx.camera:camera-camera2:1.2.0")
    implementation("androidx.camera:camera-lifecycle:1.2.0")
    implementation("androidx.camera:camera-view:1.2.0") // Optional for a CameraView
    implementation(libs.navigation.ui)
    implementation(libs.navigation.fragment)
    implementation("com.google.guava:guava:31.0.1-android")

    // To use CallbackToFutureAdapter
    implementation("androidx.concurrent:concurrent-futures:1.2.0")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.6.0")
}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.15") // Ensure this line is present
    }
}
