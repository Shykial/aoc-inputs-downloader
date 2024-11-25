package com.shykial.aoc.inputsDownloader

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register
import java.nio.file.Path
import java.time.Month
import java.time.ZonedDateTime

private const val DEFAULT_INPUTS_DIRECTORY_NAME = "inputs"

class InputsDownloaderPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val downloadInputsExtension = extensions.create<DownloadInputsExtension>("downloadAocInputs")
                .applyConventions(project)

            tasks.register<DownloadInputsTask>("downloadAocInputs") {
                applyFromExtension(downloadInputsExtension)
            }
        }
    }

    private fun DownloadInputsTask.applyFromExtension(downloadInputsExtension: DownloadInputsExtension) {
        aocYear.set(downloadInputsExtension.aocYear)
        inputsDirectory.set(downloadInputsExtension.inputsDirectory)
        fileNameMapping.set(downloadInputsExtension.fileNameMapping)
        sessionCookieProvider.set(downloadInputsExtension.sessionCookieProvider)
    }

    private fun DownloadInputsExtension.applyConventions(project: Project) = apply {
        aocYear.convention(project.provider { getCurrentAocYear() })
        inputsDirectory.convention(project.provider { project.getDefaultInputsDirectory() })
        fileNameMapping.convention { it.toDefaultAocFileName() }
    }

    private fun getCurrentAocYear() = with(ZonedDateTime.now(AOC_ZONE_ID)) {
        when (month) {
            Month.DECEMBER -> year
            else -> year - 1
        }
    }

    private fun Project.getDefaultInputsDirectory(): Path =
        project.extensions.getByType<SourceSetContainer>()["main"]
            .resources
            .srcDirs
            .first()
            .resolve(DEFAULT_INPUTS_DIRECTORY_NAME)
            .toPath()

    private fun Int.toDefaultAocFileName() = "Day${this.toString().padStart(2, '0')}.txt"
}

abstract class DownloadInputsExtension(project: Project) {
    val aocYear = project.objects.property<Int>()
    val inputsDirectory = project.objects.property<Path>()
    internal val sessionCookieProvider = project.objects.property<() -> String>()
    internal val fileNameMapping = project.objects.property<(Int) -> String>()

    fun sessionCookie(block: () -> String) {
        sessionCookieProvider.set(block)
    }

    fun fileNameMapping(block: (Int) -> String) {
        fileNameMapping.set(block)
    }
}
