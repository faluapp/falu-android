plugins {
    id("com.android.application")
    kotlin("android")
}

apply(from = "${rootDir}/build-config/klint.gradle.kts")

android {
    compileSdk = 34
    namespace = project.properties["FALU_SDK_NAMESPACE"].toString()

    defaultConfig {
        applicationId = "io.falu.identity.sample"
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
        }
    }

    compileOptions {
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

    lint.enable += "Interoperability"
    lint.disable += "CoroutineCreationDuringComposition"
    lint.lintConfig = file("../settings/lint.xml")

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(project(":identity"))
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(libs.androidx.browser)
    implementation(libs.core.ktx)
    implementation(libs.material)
    implementation(libs.constraint)
    implementation(libs.abstractions)
    implementation(libs.androidx.livedata)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.fragment)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.core.ktx)
}