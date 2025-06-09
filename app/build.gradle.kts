plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger)
    kotlin("kapt")
//    id ("kotlin-kapt")
//    id ("com.google.dagger.hilt.android")

}

android {
    namespace = "com.example.vision2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.vision2"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

//    implementation(libs.googleid)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    // CameraX library
    implementation("androidx.camera:camera-camera2:1.5.0-alpha05")
    implementation("androidx.camera:camera-lifecycle:1.5.0-alpha05")
    implementation("androidx.camera:camera-view:1.5.0-alpha05")
    implementation("androidx.camera:camera-extensions:1.5.0-alpha05")

    //// ML-KIT ////
    implementation("com.google.mlkit:text-recognition:16.0.1")
    //// ML-KIT for Image Labeling ////
    implementation("com.google.mlkit:image-labeling:17.0.9")
    ///ML-KIT for Object Detection
    implementation("com.google.mlkit:object-detection:17.0.2")
    ///ML- Kit custom Model for Object detection
    implementation("com.google.mlkit:object-detection-custom:17.0.2")

    // ML-kit for Barcode detection
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    // ML-kit for Translate
    implementation("com.google.mlkit:translate:17.0.3")
    implementation("com.google.mlkit:common:18.11.0")
    

    implementation(libs.dagger.hilt)
    implementation(libs.hilt.compose.navigation)
    kapt(libs.dagger.kapt)



    implementation("com.google.accompanist:accompanist-permissions:0.37.0")

    // retrofit dependencies
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup:javapoet:1.13.0")

    // ViewModel + LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation("com.google.apis:google-api-services-gmail:v1-rev110-1.25.0")

    dependencies {
        implementation ("com.google.guava:guava:31.0.1-jre") // Use latest stable version
    }
    dependencies {
        implementation ("androidx.credentials:credentials-play-services-auth:1.2.2")
        implementation ("androidx.credentials:credentials:1.2.2")
        implementation ("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    }


    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    implementation("com.google.api-client:google-api-client-android:2.2.0") // Check for latest
    implementation("com.google.http-client:google-http-client-gson:1.43.3")









}

//// Allow references to generated code
//kapt {
//    correctErrorTypes = true
//}