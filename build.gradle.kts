// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.2")
        classpath("io.github.gradle-nexus:publish-plugin:1.3.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    }
}

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    kotlin("android") version "1.9.0" apply false
}

allprojects {
    group = GROUP

    repositories {
        google()
        mavenCentral()
    }
}

ext.getPublishVersion = { -> return System.getenv("VERSION_NAME") ?: "1.0-SNAPSHOT" }
ext.getPublishVersionCode = { -> return 'git rev-list HEAD --count'.execute().text.trim() }
ext.getPublishUsername = { -> return System.getenv("PUBLISHING_USERNAME") ?: "" }
ext.getPublishPassword = { -> return System.getenv("PUBLISHING_PASSWORD") ?: "" }
ext.getPublishStagingProfileId = { -> return System.getenv("PUBLISHING_PROFILE_ID") ?: "" }
ext.getPublishUrl = { ->
    return System.getenv("PUBLISHING_URL")
        ?: "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
}

ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
ext["signing.password"] = System.getenv("SIGNING_PASSWORD")

// If the key content is in an environmental var, write it to "tmp/key.gpg" and update
// ext['signing.secretKeyRingFile'] to point to it
def keyContent = System . getenv ("SIGNING_KEY")
if (keyContent != null) {
    def tempDirectory = new File("$rootProject.rootDir/tmp")
    mkdir tempDirectory
            def keyFile = new File("$tempDirectory/key.gpg")
    keyFile.createNewFile()
    def os = keyFile . newDataOutputStream ()
    os.write(keyContent.decodeBase64())
    os.close()
    keyContent = ''

    ext['signing.secretKeyRingFile'] = keyFile.absolutePath
}

ext {
    group_id = GROUP
}

nexusPublishing {
    packageGroup = GROUP

    repositories {
        sonatype {
            stagingProfileId = getPublishStagingProfileId()
            username = getPublishUsername()
            password = getPublishPassword()
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}