import java.util.Base64

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  // alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.gms.google.services)
  alias(libs.plugins.kotlinx.serialization)
}

android {
  namespace = "com.example"
  compileSdk = 34

  // Decode the debug keystore from base64 if it does not exist
  val keystoreFile = file("${rootDir}/debug.keystore")
  val base64File = file("${rootDir}/debug.keystore.base64")
  if (!keystoreFile.exists() && base64File.exists()) {
      try {
          val base64Text = base64File.readText().trim()
          val decodedBytes = Base64.getDecoder().decode(base64Text)
          keystoreFile.writeBytes(decodedBytes)
          println("Successfully decoded debug.keystore from base64!")
      } catch (e: Exception) {
          println("Error decoding keystore: ${e.message}")
      }
  }

  defaultConfig {
    applicationId = "com.aistudio.halalcircle.vqyptl"
    minSdk = 23
    targetSdk = 34
    // Increment versionCode for each release. versionName follows semver.
    versionCode = 12
    versionName = "1.9.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    buildConfigField("String", "FB_K1", "\"${System.getenv("FB_K1") ?: ""}\"")
    buildConfigField("String", "FB_K2", "\"${System.getenv("FB_K2") ?: ""}\"")
    buildConfigField("String", "FB_K3", "\"${System.getenv("FB_K3") ?: ""}\"")
    buildConfigField("String", "FB_K4", "\"${System.getenv("FB_K4") ?: ""}\"")
  }

  signingConfigs {
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
      enableV1Signing = true
      enableV2Signing = true
    }
    // Placeholder release configuration. Update with actual keystore details for production.
    create("releaseConfig") {
            storeFile = file("tmkey.jks")
            storePassword = "CallOfTamim2345617859Tanjil@#yt@#67Tuli@#"
            keyAlias = "releasekey"
            keyPassword = "CallOfTamim2345617859Tanjil@#yt@#67Tuli@#"
            enableV1Signing = true
            enableV2Signing = true
        }

  buildTypes {
    release {
      // Enables code shrinking, obfuscation, and optimization.
      isMinifyEnabled = false
      // Enables resource shrinking, which is performed by the Android Gradle plugin.
      isShrinkResources = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("releaseConfig")
      
      // Optimization for AAB generation
      ndk {
        debugSymbolLevel = "FULL"
      }
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
      versionNameSuffix = "-debug"
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  kotlin {
    jvmToolchain(11)
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.storage)
  implementation(libs.firebase.auth)
  implementation("com.google.firebase:firebase-messaging")
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.camera.camera2)
  implementation(libs.androidx.camera.core)
  implementation(libs.androidx.camera.lifecycle)
  implementation(libs.androidx.camera.view)
  implementation(libs.androidx.camera.video)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.media3.ui)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(platform(libs.supabase.bom))
  implementation(libs.supabase.auth)
  implementation(libs.supabase.postgrest)
  implementation(libs.supabase.storage)
  // Removed minio dependency
  implementation(libs.ktor.client.android)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.location)
  implementation(libs.maps.compose)
  implementation(libs.play.services.maps)
  implementation(libs.retrofit)
  // testImplementation(libs.androidx.compose.ui.test.junit4)
  // testImplementation(libs.androidx.core)
  // testImplementation(libs.androidx.junit)
  // testImplementation(libs.junit)
  // testImplementation(libs.kotlinx.coroutines.test)
  // testImplementation(libs.robolectric)
  // testImplementation(libs.roborazzi)
  // testImplementation(libs.roborazzi.compose)
  // testImplementation(libs.roborazzi.junit.rule)
  // androidTestImplementation(platform(libs.androidx.compose.bom))
  // androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  // androidTestImplementation(libs.androidx.espresso.core)
  // androidTestImplementation(libs.androidx.junit)
  // androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

tasks.register("generateReleaseKeystore") {
    val keystoreFile = file("${rootDir}/release.keystore")
    doLast {
        if (!keystoreFile.exists()) {
            ant.withGroovyBuilder {
                "genkey"(
                    "keystore" to keystoreFile.absolutePath,
                    "alias" to "release",
                    "storepass" to "halalcircle",
                    "keypass" to "halalcircle",
                    "dname" to "CN=Halal Circle, OU=Halal Circle, O=Halal Circle, C=BD",
                    "validity" to "10000",
                    "keyalg" to "RSA"
                )
            }
            println("Generated release.keystore at ${keystoreFile.absolutePath}")
        }
    }
}

tasks.named("preBuild") {
    dependsOn("generateReleaseKeystore")
}

tasks.register<Copy>("copyApkToWorkspace") {
    outputs.upToDateWhen { false }
    from("${layout.buildDirectory.get()}/outputs/apk/debug")
    include("app-debug.apk")
    into("${rootProject.projectDir}/apks")
    doLast {
        println("APK copied to apks/ folder")
    }
}

tasks.register<Copy>("copyAabToWorkspace") {
    outputs.upToDateWhen { false }
    from("${layout.buildDirectory.get()}/outputs/bundle/release")
    include("app-release.aab")
    into("${rootProject.projectDir}/apks")
    doLast {
        println("AAB copied to apks/ folder")
    }
}

project.afterEvaluate {
    tasks.findByName("assembleDebug")?.finalizedBy("copyApkToWorkspace")
    tasks.findByName("bundleRelease")?.finalizedBy("copyAabToWorkspace")
}

