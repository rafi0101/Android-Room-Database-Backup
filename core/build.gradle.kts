import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish") version "0.31.0"
}

version = properties["VERSION_NAME"] as String

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        minSdk = 21
        compileSdk = 35
        buildToolsVersion = "35.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    namespace = "de.raphaelebner.roomdatabasebackup.core"

}


mavenPublishing {
    configure(
        AndroidSingleVariantLibrary(
            // the published variant
            variant = "release",
            // whether to publish a sources jar
            sourcesJar = true,
            // whether to publish a javadoc jar
            publishJavadocJar = true,
        )
    )


    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}


// apply {
//     from("${rootDir}/scripts/publish-module.gradle")
// }


dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    //ROOM SQLite
    val roomVersion = "2.6.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")

    // optional - RxJava support for Room
    implementation("androidx.room:room-rxjava2:$roomVersion")

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$roomVersion")

    // Test helpers
    testImplementation("androidx.room:room-testing:$roomVersion")

    //Material Design Implementation
    implementation("com.google.android.material:material:1.12.0")

    //Androidx Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    //Google Guava
    implementation("com.google.guava:guava:33.4.5-jre")

    //Apache commons io
    //https://mvnrepository.com/artifact/commons-io/commons-io
    //noinspection GradleDependency
    implementation("commons-io:commons-io:2.18.0")

}
