plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "co.edu.unal.tictactoe"

    compileSdk = 37

    defaultConfig {
        applicationId = "co.edu.unal.tictactoe"
        minSdk = 24
        targetSdk = 35 // Actualizado a 37 para eliminar la advertencia de compatibilidad
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
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

    buildFeatures {
        compose = false
    }
}

dependencies {
    // UI basada en Views (XML)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}