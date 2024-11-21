package com.shykial.aoc.inputsDownloader

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import java.nio.file.Path
import java.time.Month
import java.time.ZoneOffset
import java.time.ZonedDateTime

private const val DEFAULT_INPUTS_DIRECTORY_NAME = "inputs"

class InputsDownloaderPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val downloadInputsExtension = extensions.create<DownloadInputsExtension>("downloadAocInputs")
                .apply {
                    aocYear.convention(provider(::getCurrentAocYear))
                    inputsDirectory.convention(provider { getInputsPath() })
                }

            tasks.register<DownloadInputsTask>("downloadAocInputs") {
                aocYear.set(downloadInputsExtension.aocYear)
                inputsDirectory.set(downloadInputsExtension.inputsDirectory)
            }
        }
    }

    private fun getCurrentAocYear() = with(ZonedDateTime.now(ZoneOffset.UTC)) {
        when (month) {
            Month.DECEMBER -> year
            else -> year - 1
        }
    }

    private fun Project.getInputsPath(): Path =
        project.extensions.getByType<SourceSetContainer>()["main"]
            .resources
            .srcDirs
            .first()
            .resolve(DEFAULT_INPUTS_DIRECTORY_NAME)
            .toPath()
}

interface DownloadInputsExtension {
    val aocYear: Property<Int>
    val inputsDirectory: Property<Path>
}