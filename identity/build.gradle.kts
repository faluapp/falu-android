plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
    alias(libs.plugins.compose.compiler)
}

apply(from = "${rootDir}/build-config/klint.gradle.kts")

android {
    compileSdk = 35
    namespace = project.properties["FALU_SDK_NAMESPACE"].toString()

    defaultConfig {
        minSdk = 23

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
        compose = true
    }

    lint.enable += "Interoperability"
    lint.disable += "CoroutineCreationDuringComposition"
    lint.lintConfig = file("../settings/lint.xml")

    lint {
        abortOnError = false
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.maxHeapSize = "1024m"
            }
        }
    }
}

dependencies {
    api(project(":falu-core"))
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.constraint)
    implementation(libs.androidx.appcompat)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.preview)
    implementation(libs.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.navigation)
    implementation(libs.runtime)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.accompanist)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.ui.test.junit4.android)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.support)

    implementation(libs.bundles.navigation)
    implementation(libs.bundles.camera)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.androidx.exifinterface)

    implementation(libs.core.ktx)
    implementation(libs.material)
    implementation(libs.joda)
    implementation(libs.bundles.coil)
    implementation(libs.androidx.browser)

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
    testImplementation(libs.androidx.core.ktx)
    testImplementation(libs.ui.tests)
    testImplementation(libs.ui.tests.manifest)
    testImplementation(libs.androidx.ui.tooling)

    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlin.test)
}

ext {
    set("artifactId", "falu-identity")
    set("artifactName", "Falu Identity")
    set("artifactDescription", "SDK for Falu Identity")
}

apply(from = "${rootDir}/deploy/deploy.gradle")