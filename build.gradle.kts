plugins {
    `kotlin-dsl`
    `maven-publish`
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
    plugins {
        create("inputsDownloaderPlugin") {
            id = "com.shykial.aoc.inputs.downloader"
            implementationClass = "com.shykial.aoc.inputsDownloader.InputsDownloaderPlugin"
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "inputs-downloader"
            version = "0.0.3"

            from(components["java"])
        }
    }
}