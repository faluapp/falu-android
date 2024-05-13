// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.4.0")
        classpath("io.github.gradle-nexus:publish-plugin:1.3.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
    }
}

allprojects {
    group = project.properties["GROUP"].toString()
}

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    kotlin("android") version "1.9.23" apply false
}

val publishUsername: String? by extra {
    System.getenv("PUBLISHING_USERNAME")
}

val publishPassword: String? by extra {
    System.getenv("PUBLISHING_PASSWORD")
}

val publishStagingProfileId: String? by extra {
    System.getenv("PUBLISHING_PROFILE_ID")
}

nexusPublishing {
    val publishStagingProfileId: String? by extra
    val publishPassword: String? by extra
    val publishUsername: String? by extra

    packageGroup.set(project.properties["GROUP"].toString())

    repositories {
        sonatype {
            stagingProfileId.set(publishStagingProfileId)
            username.set(publishUsername)
            password.set(publishPassword)
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}