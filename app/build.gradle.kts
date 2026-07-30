import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

android {
    namespace = "com.cara.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cara.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY", "")
    }

    buildTypes {
        debug {
            // Backend is deployed on Cloud Run (2026-07-17) - the app now
            // talks to it over the public internet instead of the laptop's
            // hotspot-WiFi IP, so testing no longer requires the phone and
            // laptop to be on the same network at all. HTTPS, so no
            // cleartext-traffic exception needed for this host.
            //
            // See reference_cara_gcp_deployment.md for the deploy path/
            // gotchas. If the backend is ever redeployed under a different
            // Cloud Run URL, update this value + rebuild.
            //
            // Old local-WiFi dev path (same-hotspot, laptop-hosted backend):
            // http://10.220.76.229:8000/ - kept here in case local-only
            // testing against an unpushed backend change is ever needed
            // again; DHCP-assigned, may have changed since.
            buildConfigField("String", "BASE_URL", "\"https://cara-backend-783370662490.asia-south1.run.app/\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "BASE_URL", "\"https://cara-backend-783370662490.asia-south1.run.app/\"")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
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
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Local cache (offline fallback)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Images
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Maps
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    // Pinned to the version current around when this project's other deps
    // were pinned (compose-bom 2024.06.00, compileSdk 34, AGP 8.5.0) -
    // maps-compose's LATEST release (tried first, 8.4.0) transitively pulls
    // in Compose/AndroidX artifacts from mid-2026 that require compileSdk
    // 35-37 and AGP 8.6-9.1, way past what the rest of this project uses -
    // 36 AAR metadata errors on sync. Don't bump this without also bumping
    // compileSdk/AGP/the compose BOM together, deliberately, project-wide.
    implementation("com.google.maps.android:maps-compose:4.4.2")

    // Device location (Home screen context)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
