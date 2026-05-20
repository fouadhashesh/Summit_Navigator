plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.android.legacy.kapt)
  alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.summitnavigator"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.summitnavigator"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      viewBinding = true
      compose = false
      aidl = false
      buildConfig = false
      shaders = false
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.google.material)
  implementation(libs.androidx.lifecycle.runtime.ktx)

  // Room
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  kapt(libs.androidx.room.compiler)

  // Navigation (XML Fragments)
  implementation(libs.androidx.navigation.fragment)
  implementation(libs.androidx.navigation.ui)

  // Lifecycle components (MVVM)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.livedata.ktx)

  // Kotlin Coroutines
  implementation(libs.kotlinx.coroutines.android)

  // Retrofit and Gson Converter
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.converter.gson)

  // Firebase
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.auth)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)
}
