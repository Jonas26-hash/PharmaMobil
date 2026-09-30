import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

/**
 * URL base del backend pharmaSoft. Por defecto apunta a 10.0.2.2, que es el
 * alias del host visto desde el emulador de Android. Para un dispositivo real
 * hay que pasar la IP de la máquina que corre el backend:
 *
 *   .\gradlew :androidApp:installDebug -PpharmaApiBaseUrl=http://192.168.1.50:8080/api/v1
 *
 * Se genera un archivo Kotlin en lugar de editar ConfigApi a mano, para que la
 * dirección de la máquina del desarrollador no quede commiteada.
 */
val apiBaseUrl: String =
    (project.findProperty("pharmaApiBaseUrl") as String?)
        ?: "http://10.0.2.2:8080/api/v1"

val generarUrlApi = tasks.register<GenerarUrlApiTask>("generarUrlApi") {
    urlBase.set(apiBaseUrl)
    directorioDestino.set(layout.buildDirectory.dir("generated/urlApi/kotlin"))
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    android {
       namespace = "pe.edu.upeu.pharmamobil.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        // La URL base se genera antes de compilar commonMain.
        commonMain {
            kotlin.srcDir(generarUrlApi)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            // api: MainApplication (androidApp) usa androidContext() al arrancar Koin.
            api(libs.koin.android)
            // Motor HTTP de Android: OkHttp.
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            // Motor HTTP de iOS: Darwin (NSURLSession).
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // api: androidApp llama a initKoin(), cuya firma expone KoinAppDeclaration.
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            // Ktor Client: api para que los módulos de plataforma (androidMain,
            // iosMain) puedan ver HttpClientEngine y crear los actuals del motor.
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            // MockEngine para probar las APIs y los repositorios remotos.
            implementation(libs.ktor.client.mock)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}