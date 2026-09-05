plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.apexinnovators.app"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "role"
    productFlavors {
        create("client") {
            dimension = "role"
            applicationId = "com.apexinnovators.app"
            manifestPlaceholders["appName"] = "Apex Innovators"
            buildConfigField("String", "START_URL", "\"https://sky-ydv2008.github.io/Team.Apex/index.html\"")
            buildConfigField("Boolean", "IS_ADMIN_APP", "false")
        }
        create("admin") {
            dimension = "role"
            applicationId = "com.apexinnovators.admin"
            manifestPlaceholders["appName"] = "Apex Admin"
            buildConfigField("String", "START_URL", "\"https://sky-ydv2008.github.io/Team.Apex/admin/dashboard.html\"")
            buildConfigField("Boolean", "IS_ADMIN_APP", "true")
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
        debug {
            isDebuggable = true
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
        viewBinding = true
        buildConfig = true
    }
    androidResources {
        noCompress += listOf("dat", "bundle")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("androidx.activity:activity-ktx:1.9.3")
}
