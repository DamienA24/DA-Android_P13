plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.kotlin)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
  alias(libs.plugins.googleServices)
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "com.openclassrooms.hexagonal.games"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.openclassrooms.hexagonal.games"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.11"
  }
  buildFeatures {
    compose = true
  }
}

dependencies {
  //kotlin
  implementation(platform(libs.kotlin.bom))

  //DI
  implementation(libs.hilt)
  ksp(libs.hilt.compiler)
  implementation(libs.hilt.navigation.compose)

  //compose
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.graphics)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.material)
  implementation(libs.compose.material3)
  implementation(libs.lifecycle.runtime.compose)
  debugImplementation(libs.compose.ui.tooling)
  debugImplementation(libs.compose.ui.test.manifest)

  implementation(libs.activity.compose)
  implementation(libs.navigation.compose)
  
  implementation(libs.kotlinx.coroutines.android)
  
  implementation(libs.coil.compose)
  implementation(libs.accompanist.permissions)

  testImplementation(libs.junit)
  androidTestImplementation(libs.ext.junit)
  androidTestImplementation(libs.espresso.core)

  implementation(libs.firebase.bom)
  implementation(libs.firebase.analytics)
  implementation(libs.firebase.auth)
  implementation(libs.firebase.ui.auth)
  implementation(libs.androidx.compose.material.icons.extended)

  implementation(libs.kotlin.metadata.jvm)
}