plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)

    alias(libs.plugins.googleServices)

   // id("com.google.gms.google-services")

}

android {
    namespace = "martinez.javier.chat"
    compileSdk = 34

    defaultConfig {
        applicationId = "martinez.javier.chat"
        minSdk = 30
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)



    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.firebase.auth)
    implementation(libs.firebase.realtime)
    implementation(libs.login.google)

    implementation(libs.circle.image)
    implementation(libs.glide)

   //SE NECESITAN SI SE USA LA FORMA ANTIGUA EN PLUGINS A NIVEL MODULE (ARRIBA) Y PLUGINS A NIVEL PROJECT
    //implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    //implementation("com.google.firebase:firebase-analytics")

    //implementation("com.google.firebase:firebase-auth")
    //implementation("com.google.firebase:firebase-database-ktx")






}