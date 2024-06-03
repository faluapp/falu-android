plugins {
id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
}

apply(from = "${rootDir}/build-config/klint.gradle.kts")

android {
    compileSdk = 34
    namespace = project.properties["FALU_SDK_NAMESPACE"].toString()

    defaultConfig {
        minSdk = 23

        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
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

    testOptions {
        unitTests {
            // Note: without this, all Robolectric tests using BuildConfig will fail.
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    api(project(":falu-core"))

    testImplementation(libs.junit)
    testImplementation(libs.bundles.mokito)
    testImplementation(libs.nhaarman)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.robo)
    testImplementation(libs.mokito.kotlin)
    testImplementation(libs.bundles.kotlin.test)
    testImplementation(libs.androidx.core.ktx)
}

ext {
    set("artifactId", "falu-android")
    set("artifactName", "falu-android")
    set("artifactDescription", "SDK for Falu Android")
}

apply(from = "${rootDir}/deploy/deploy.gradle")