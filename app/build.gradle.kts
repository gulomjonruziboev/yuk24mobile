import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun localOrEnv(key: String, env: String, default: String = ""): String =
    localProps.getProperty(key)
        ?: System.getenv(env)
        ?: default

val deployedBaseUrl = localOrEnv(
    "yuk24.base.url",
    "YUK24_BASE_URL",
    "https://yuk24-backend.onrender.com/"
)
val emulatorBaseUrl = localOrEnv("yuk24.base.url.debug", "YUK24_BASE_URL_DEBUG", deployedBaseUrl)
// OpenRouteService key for map polyline (debug/local). Add ors.api.key=... to local.properties — never commit keys.
val orsApiKey = localOrEnv("ors.api.key", "ORS_API_KEY", "")

android {
    namespace = "uz.yuk24.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "uz.yuk24.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        resourceConfigurations += listOf("uz", "ru", "en")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "BASE_URL", "\"$emulatorBaseUrl\"")
            buildConfigField("String", "ORS_API_KEY", "\"$orsApiKey\"")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"$deployedBaseUrl\"")
            buildConfigField("String", "ORS_API_KEY", "\"$orsApiKey\"")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    implementation(libs.coil.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.play.services.location)

    implementation(libs.osmdroid.android)

    testImplementation(libs.junit)
}
