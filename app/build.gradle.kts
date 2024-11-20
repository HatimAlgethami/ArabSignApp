import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

val myVar1 = gradleLocalProperties(rootDir,providers).getProperty("api_key1", "")

plugins {

    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") version "4.4.2"

}



android {
    namespace = "com.example.arabsignapp"
    compileSdk = 34

    defaultConfig {

        resValue("string", "api_key1", "\"$myVar1\"")

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-firestore-ktx:24.0.0")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")


    // MediaPipe Vision Tasks (only include what's necessary)
    implementation("com.google.mediapipe:tasks-vision:0.10.2")

    // Android libraries and UI components
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Android CameraX
    implementation("androidx.camera:camera-core:1.4.0")
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")

    // Navigation and Guava for concurrency
    implementation(libs.navigation.ui)
    implementation(libs.navigation.fragment)
    implementation("com.google.guava:guava:31.0.1-android")
    implementation("androidx.concurrent:concurrent-futures:1.2.0")

    // Kotlin coroutines for Guava
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.6.0")

    // JSON converter
    implementation("com.google.code.gson:gson:2.11.0")

    // OkHttp for making HTTP requests   Google API
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

//    // MediaPipe Hands for hand tracking
//    implementation("com.google.mediapipe:tasks-vision-handlandmarker:0.10.2")
//
//    // MediaPipe Pose for body/pose tracking
//    implementation("com.google.mediapipe:tasks-vision-poselandmarker:0.10.2")
//
//    // MediaPipe Face for face tracking
//    implementation("com.google.mediapipe:tasks-vision-facelandmarker:0.10.2")
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}


