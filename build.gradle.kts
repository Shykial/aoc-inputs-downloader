plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.publish)
    alias(libs.plugins.ktlint.gradle)
}

group = "com.shykial.aoc"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor.client)
}

kotlin {
    jvmToolchain(21)
}

gradlePlugin {
    website = "https://github.com/Shykial/aoc-inputs-downloader"
    vcsUrl = "https://github.com/Shykial/aoc-inputs-downloader.git"
    plugins {
        create("inputsDownloaderPlugin") {
            id = "com.shykial.aoc.inputs.downloader"
            implementationClass = "com.shykial.aoc.inputsDownloader.InputsDownloaderPlugin"
            displayName = "Advent of Code inputs downloader plugin"
            description = "Simple gradle plugin for automatic downloading of advent-of-code inputs"
            tags = setOf("advent-of-code")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Shykial/aoc-inputs-downloader")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_PACKAGES_TOKEN")
            }
        }
    }
}
