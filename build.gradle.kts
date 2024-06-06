// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.22" apply false // Update the KSP version
}

buildscript {

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle)
        classpath (libs.kotlin.gradle.plugin.v200)
        classpath(libs.com.google.devtools.ksp.gradle.plugin.v2001022)
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)
    }
}
