plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "za.co.safety4women"
    compileSdk = 36
    defaultConfig {
        applicationId = "za.co.safety4women"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val apiUrl = providers.gradleProperty("safetyApiBaseUrl").orElse("").get()
        buildConfigField("String", "SAFETY_API_BASE_URL", "\"${apiUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
    }
    signingConfigs {
        create("release") {
            val storePath = providers.environmentVariable("SAFETY4WOMEN_KEYSTORE").orNull
            val storePass = providers.environmentVariable("SAFETY4WOMEN_KEYSTORE_PASSWORD").orNull
            val keyAliasValue = providers.environmentVariable("SAFETY4WOMEN_KEY_ALIAS").orNull
            val keyPass = providers.environmentVariable("SAFETY4WOMEN_KEY_PASSWORD").orNull
            if (!storePath.isNullOrBlank() && !storePass.isNullOrBlank() && !keyAliasValue.isNullOrBlank() && !keyPass.isNullOrBlank()) {
                storeFile = file(storePath)
                storePassword = storePass
                keyAlias = keyAliasValue
                keyPassword = keyPass
            }
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            val hasReleaseKey = listOf("SAFETY4WOMEN_KEYSTORE", "SAFETY4WOMEN_KEYSTORE_PASSWORD", "SAFETY4WOMEN_KEY_ALIAS", "SAFETY4WOMEN_KEY_PASSWORD").all { !providers.environmentVariable(it).orNull.isNullOrBlank() }
            if (hasReleaseKey) signingConfig = signingConfigs.getByName("release")
        }
    }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    // Firebase BoM keeps the Firebase SDKs on mutually compatible versions.
    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    implementation("androidx.credentials:credentials:1.6.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    val composeBom = platform("androidx.compose:compose-bom:2026.04.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
