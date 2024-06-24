import org.mtali.Configuration

plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.jetbrainsKotlinAndroid)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.google.services)
  alias(libs.plugins.secrets)
}

android {
  namespace = Configuration.PACKAGE_NAME
  compileSdk = Configuration.COMPILE_SDK

  defaultConfig {
    applicationId = Configuration.PACKAGE_NAME
    minSdk = Configuration.MIN_SDK
    targetSdk = Configuration.TARGET_SDK
    versionCode = Configuration.VERSION_CODE
    versionName = Configuration.VERSION_NAME

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.11"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

secrets {
  propertiesFileName = "secrets.properties"

  defaultPropertiesFileName = "secrets.defaults.properties"

  ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"

  ignoreList.add("sdk.*")
}

dependencies {
  // Core
  implementation(libs.androidx.core.ktx)

  // Lifecycle
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose + UI + Navigation
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(libs.androidx.activity.compose)
  implementation(composeBom)
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.navigation.compose)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)

  // Material Icons
  implementation(libs.androidx.material.icons.extended)

  // Timber
  implementation(libs.timber)

  // Hilt
  implementation(libs.hilt.android)
  implementation(libs.androidx.hilt.navigation.compose)
  ksp(libs.hilt.compiler)

  // Splashscreen
  implementation(libs.core.splashscreen)

  // Datastore Preferences
  implementation(libs.androidx.datastore)

  // Retrofit
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.kotlin.serialization)

  // OkHttp
  implementation(libs.okhttp.logging)

  // Serialization
  implementation(libs.kotlinx.serialization.json)

  // Firebase
  val firebaseBom = platform(libs.firebase.bom)
  implementation(firebaseBom)
  implementation(libs.firebase.analytics)
  implementation(libs.firebase.auth)

  // Maps
  implementation(libs.maps.compose)
  implementation(libs.play.location)
  implementation(libs.places)
  implementation(libs.maps.services)
  implementation(libs.maps.utils)

  // Flexible BottomSheet
  implementation(libs.flexible.bottomsheet)

  // Stream
  implementation(libs.stream)
  implementation(libs.stream.client)
  implementation(libs.stream.offline)
  implementation(libs.stream.state)
  implementation(libs.stream.compose)

  // Accompanist
  implementation(libs.accompanist.permissions)

  // Testing
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
}