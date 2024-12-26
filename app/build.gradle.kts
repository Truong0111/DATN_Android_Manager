plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.truongtq_datn"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.truongtq_datn"
        minSdk = 26
        targetSdk = 34
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
            signingConfig = signingConfigs.getByName("debug")
        }
    }
//    flavorDimensions += "version"
//    productFlavors {
//        create("manager") {
//            dimension = "version"
//            applicationIdSuffix = ".manager"
//            versionNameSuffix = "-manager"
//            applicationId = "com.qrdoor.appmanager"
//            resValue("string", "app_name", "QR Door Manager")
//        }
//        create("user") {
//            dimension = "version"
//            applicationIdSuffix = ".user"
//            versionNameSuffix = "-user"
//            applicationId = "com.qrdoor.appuser"
//            resValue("string", "app_name", "QR Door")
//        }
//    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    viewBinding {
        enable = true
    }
}

dependencies {
    //http
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    //Biometric
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    //QR
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    //Hash
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.auth0.android:jwtdecode:2.0.2")

    //Core
    implementation(libs.core)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment)
    implementation(libs.material)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.gridlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}