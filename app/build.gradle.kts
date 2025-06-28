plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    //id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.gestordetareas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gestordetareas"
        minSdk = 27
        targetSdk = 35
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
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("org.osmdroid:osmdroid-android:6.1.16")
    implementation ("org.osmdroid:osmdroid-wms:6.1.16")
    implementation ("androidx.sqlite:sqlite:2.1.0")
    implementation ("androidx.security:security-crypto:1.1.0-alpha03")
}

sonarqube {
    properties {
        property("sonar.projectKey", "gestorTareas")
        property("sonar.organization", "oelorduy") // Si usas SonarCloud
        property("sonar.host.url", "http://localhost:9000") // o tu URL de Sonar
        property("sonar.language", "kotlin")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.sources", "src/main")
        property("sonar.tests", "src/test")
        property("sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.xml")
    }
}