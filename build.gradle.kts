buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
        classpath("com.google.gms:google-services:4.4.0")
    }
}

plugins {
    kotlin("android") version "1.9.10" apply false
    id("com.android.application") version "8.1.2" apply false
}



