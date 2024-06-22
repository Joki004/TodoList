plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.todolist"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.todolist"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation (libs.github.glide)
    annotationProcessor (libs.glide.compiler)
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.recyclerview.v121)
    implementation(libs.androidx.room.runtime.v230)
    implementation(libs.androidx.room.ktx.v250)
    ksp(libs.androidx.room.compiler.v230)
    implementation(libs.material.v150alpha05)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.material)
    implementation(libs.androidx.core.ktx.v160)
    implementation(libs.androidx.appcompat.v130)
    implementation(libs.androidx.activity.ktx.v130)
    implementation(libs.androidx.constraintlayout.v204)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v113)
    androidTestImplementation(libs.androidx.espresso.core.v340)
}
