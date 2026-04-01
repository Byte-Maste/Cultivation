plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.cultivation"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.cultivation"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true

    }
    buildFeatures {
        // mlModelBinding = true // Disabled to use manual LiteRT implementation
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.location)


// Support libs (current latest on Maven Central)
// Support libs (LiteRT)
    implementation(libs.litert.support)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    implementation(libs.litert)
    implementation(libs.litert.gpu)
    implementation(libs.litert.select.tf.ops)


    implementation("com.google.guava:guava:31.1-android")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.mpandroidchart)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
}