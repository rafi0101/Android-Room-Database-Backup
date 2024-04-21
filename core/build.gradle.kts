plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.google.devtools.ksp")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

version = properties["VERSION_NAME"] as String

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        minSdk = 21
        compileSdk = 34
        buildToolsVersion = "34.0.0"

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

apply {
    from("${rootDir}/scripts/publish-module.gradle")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

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
    implementation("com.google.android.material:material:1.11.0")

    //Androidx Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    //Google Guava
    implementation("com.google.guava:guava:33.1.0-jre")

    //Apache commons io
    //https://mvnrepository.com/artifact/commons-io/commons-io
    //noinspection GradleDependency
    implementation("commons-io:commons-io:2.16.1")

}
