plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

apply {
    from(rootProject.file("build-config/version.gradle.kts"))
}

apply(from = "${rootDir}/build-config/klint.gradle.kts")

android {
    compileSdk = 34
    namespace = project.properties["FALU_SDK_NAMESPACE"].toString()

    val publishVersionCode: String by project.extra
    val publishVersion: String by project.extra

    defaultConfig {
        minSdk = 23

        buildConfigField("String", "FALU_VERSION_NAME", "\"${publishVersion}\"")
        buildConfigField("String", "FALU_VERSION_CODE", "\"${publishVersionCode}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    sourceSets {
        getByName("main") {
            res.srcDirs(listOf("res"))
        }
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
    api(libs.abstractions)
    implementation(libs.kotlin)

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
    set("artifactId", "falu-core")
    set("artifactName", "Falu Core")
    set("artifactDescription", "Common packages for Falu Android")
}

apply(from = "${rootDir}/deploy/deploy.gradle")