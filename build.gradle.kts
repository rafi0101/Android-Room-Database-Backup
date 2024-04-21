// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.set("kotlin_version", "1.9.10")
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        val kotlinVersion = rootProject.extra.get("kotlin_version")
        classpath("com.android.tools.build:gradle:8.3.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.jetbrains.dokka") version "1.9.10"
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}


apply {
    from("${rootDir}/scripts/publish-root.gradle")
}



allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}