plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

}

android {
    namespace = "com.playdevsgame"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.playdevsgame"
        minSdk = 24
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
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:21.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("io.reactivex.rxjava3:rxjava:3.0.13")
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation ("com.google.android.exoplayer:exoplayer-core:2.19.1")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-common-java8:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-service:2.2.0")
    implementation ("com.google.android.gms:play-services-location:20.0.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation ("com.firebaseui:firebase-ui-auth:7.2.0")
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    // TODO: Add the dependencies for Firebase products you want to use
    implementation("com.google.firebase:firebase-auth-ktx")
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.1.1")
    //Retrofit y Moshi
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.moshi:moshi:1.9.3")
    implementation ("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation ("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    


}