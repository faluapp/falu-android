plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

android {
    compileSdk = 33
    namespace = project.properties["FALU_SDK_NAMESPACE"].toString()

    defaultConfig {
        minSdk = 23


//                buildConfigField("String", "FALU_VERSION_NAME", "\"${getPublishVersion()}\"")
//                buildConfigField("String", "FALU_VERSION_CODE", "\"${getPublishVersionCode()}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":falu-core"))
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.constraint)

    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.support)

    implementation(libs.bundles.navigation)
    implementation(libs.bundles.camera)
    implementation(libs.bundles.androidx.lifecyle)
    implementation(libs.androidx.exifinterface)

    implementation(libs.core.ktx)
    implementation(libs.material)
    implementation(libs.joda)

    testImplementation(libs.junit)
    testImplementation(libs.bundles.mokito)
    testImplementation(libs.nhaarman)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.robo)
    testImplementation(libs.mokito.kotlin)
    testImplementation(libs.bundles.kotlin.test)
    testImplementation(libs.androidx.navigation.testing)
    testImplementation(libs.androidx.espresso.core)
    testImplementation(libs.androidx.fragment.testing)
}

ext {
    set("artifactId", "falu-identity")
    set("artifactName", "Falu Identity")
    set("artifactDescription", "SDK for Falu Identity")
}

apply(from = "${rootDir}/deploy/deploy.gradle")