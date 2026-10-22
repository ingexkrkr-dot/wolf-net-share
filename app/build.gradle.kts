import java.io.File
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

fun sourceFingerprint(projectDir: File): Int {
    val sources = File(projectDir, "src/main/java")
    if (!sources.isDirectory) return 0
    return sources.walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .sortedBy { it.path }
        .fold(7) { hash, file -> hash * 31 + file.readText().hashCode() }
}

// Locate gomobile executable across platforms (Windows / Linux / macOS)
fun findGomobileExecutable(): File? {
    val isWindows = System.getProperty("os.name").lowercase().contains("windows")
    val binaryName = if (isWindows) "gomobile.exe" else "gomobile"

    // 1. Check ~/go/bin
    val userHomeGo = File(System.getProperty("user.home"), "go/bin/$binaryName")
    if (userHomeGo.exists()) return userHomeGo

    // 2. Check GOPATH environment
    val gopath = System.getenv("GOPATH")
    if (!gopath.isNullOrBlank()) {
        val gopathGo = File(gopath, "bin/$binaryName")
        if (gopathGo.exists()) return gopathGo
    }

    // 3. Search system PATH
    val pathEnv = System.getenv("PATH") ?: ""
    for (dir in pathEnv.split(File.pathSeparator)) {
        val candidate = File(dir.trim(), binaryName)
        if (candidate.exists()) return candidate
    }

    return null
}

val gomobileBind by tasks.registering(Exec::class) {
    val goModule = rootProject.layout.projectDirectory.dir("datapath")
    val outputAar = layout.buildDirectory.file("gomobile/datapath.aar")
    val fallbackAar = file("libs/datapath.aar")

    inputs.dir(goModule).withPropertyName("goSources")
    outputs.file(outputAar).withPropertyName("aar")

    workingDir = goModule.asFile

    doFirst {
        val gomobile = findGomobileExecutable()
        val goBin = File(System.getProperty("user.home"), "go/bin")
        val gomobileDir = gomobile?.parentFile ?: goBin

        // Cross-platform PATH separator
        val newPath = "${gomobileDir.absolutePath}${File.pathSeparator}${System.getenv("PATH")}"
        environment("PATH", newPath)

        // Resolve NDK lazily inside execution phase so configuration never NPEs
        val ndkDir = android.ndkDirectory ?: File(android.sdkDirectory, "ndk/27.0.12077973")
        val envNdk = System.getenv("ANDROID_NDK_HOME") ?: System.getenv("NDK_HOME")
        val resolvedNdk = when {
            ndkDir.exists() -> ndkDir.absolutePath
            !envNdk.isNullOrBlank() && File(envNdk).exists() -> envNdk
            else -> ndkDir.absolutePath
        }
        environment("ANDROID_NDK_HOME", resolvedNdk)
        environment("ANDROID_HOME", android.sdkDirectory.absolutePath)

        outputAar.get().asFile.parentFile.mkdirs()

        if (gomobile == null || !gomobile.exists()) {
            if (fallbackAar.exists()) {
                logger.warn("gomobile not found; copying prebuilt datapath.aar from ${fallbackAar.path}")
                fallbackAar.copyTo(outputAar.get().asFile, overwrite = true)
                if (System.getProperty("os.name").lowercase().contains("windows")) {
                    commandLine("cmd", "/c", "echo", "Using fallback datapath.aar")
                } else {
                    commandLine("echo", "Using fallback datapath.aar")
                }
                return@doFirst
            } else {
                throw GradleException(
                    "gomobile not found on PATH or ~/go/bin.\n" +
                    "To build the datapath from source:\n" +
                    "  1. Install Go (https://go.dev/dl/)\n" +
                    "  2. Run: go install golang.org/x/mobile/cmd/gomobile@latest && gomobile init\n" +
                    "Or place a prebuilt datapath.aar in app/libs/datapath.aar"
                )
            }
        }

        commandLine(
            gomobile.absolutePath,
            "bind",
            "-target=android/arm64",
            "-androidapi", "24",
            "-o", outputAar.get().asFile.absolutePath,
            ".",
        )
    }

    if (System.getProperty("os.name").lowercase().contains("windows")) {
        commandLine("cmd", "/c", "echo", "Building datapath...")
    } else {
        commandLine("echo", "Building datapath...")
    }
}

android {
    namespace = "dev.shizzi"
    compileSdk = 35
    ndkVersion = "27.0.12077973"

    defaultConfig {
        applicationId = "dev.shizzi"
        minSdk = 30
        targetSdk = 35
        versionCode = 6
        versionName = "0.4.0-rc.3"
        buildConfigField("int", "SERVICE_BUILD_ID", "${sourceFingerprint(projectDir)}")

        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    val keystoreProperties = Properties().apply {
        val file = rootProject.file("keystore.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }

    signingConfigs {
        create("release") {
            val path = keystoreProperties.getProperty("storeFile")
            if (path != null) {
                storeFile = rootProject.file(path)
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (keystoreProperties.getProperty("storeFile") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        aidl = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

tasks.named("preBuild") { dependsOn(gomobileBind) }

dependencies {
    implementation(files(layout.buildDirectory.file("gomobile/datapath.aar")))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    val composeBom = platform("androidx.compose:compose-bom:2025.11.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.materialkolor:material-color-utilities:2.1.1")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:4.3")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20250107")
}
