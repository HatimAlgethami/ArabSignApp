// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
    }
}

plugins {
    alias(libs.plugins.android.application) apply false

}