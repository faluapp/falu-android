plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 33
    namespace = project.properties["FALU_SDK_NAMESPACE"].toString()

    defaultConfig {
        applicationId = "io.falu.identity.sample"
        minSdk = 23
        targetSdk = 33

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of("17"))
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(project(":identity"))
    implementation("androidx.browser:browser:1.5.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.core.ktx)
    implementation(libs.material)
    implementation(libs.constraint)
    implementation(libs.abstractions)
    implementation(libs.androidx.livedata)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.fragment)
    testImplementation(libs.junit)
}