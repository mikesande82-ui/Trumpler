plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// A build made with these in its environment (GitHub Actions passes them from
// the repo's secrets) starts out pointed at the server, so there is nothing
// to type on the phone. Without them the app starts in offline mode.
fun baked(name: String): String =
    "\"" + System.getenv(name).orEmpty().replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "app.rambler"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.rambler"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "DEFAULT_SERVER_URL", baked("RAMBLE_SERVER_URL"))
        buildConfigField("String", "DEFAULT_APP_KEY", baked("RAMBLE_APP_KEY"))
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
