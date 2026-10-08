plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "app.contactonly"
    compileSdk = 35
    defaultConfig {
        applicationId = "app.contactonly.sample"
        minSdk = 26
        targetSdk = 35
        versionCode = 9
        versionName = "0.5.8-sample"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies { testImplementation("junit:junit:4.13.2") }
