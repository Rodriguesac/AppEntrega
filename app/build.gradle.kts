plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.rodriguesacai.entregador"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rodriguesacai.entregador"
        minSdk = 23
        targetSdk = 35
        versionCode = 800
        versionName = "8.0.0"
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/clean/AndroidManifest.xml")
            java.setSrcDirs(listOf("src/clean/java"))
            res.setSrcDirs(listOf("src/clean/res"))
            assets.setSrcDirs(listOf("src/clean/assets"))
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("androidx.core:core-ktx:1.13.1")
}
