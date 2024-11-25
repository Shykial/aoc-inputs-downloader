package com.shykial.aoc.inputsDownloader

import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.property
import java.nio.file.Path
import javax.inject.Inject

abstract class DownloadInputsTask @Inject constructor(project: Project) : DefaultTask() {
    @Input
    val aocYear = project.objects.property<Int>()

    @Input
    val inputsDirectory = project.objects.property<Path>()

    @Input
    @Option(option = "sessionCookie", description = "Session cookie")
    @Optional
    val sessionCookieParam = project.objects.property<String>()

    @Input
    @Optional
    val sessionCookieProvider = project.objects.property<(() -> String)?>()

    @Input
    val fileNameMapping = project.objects.property<(Int) -> String>()

    @TaskAction
    fun downloadInputs() {
        runBlocking {
            InputsDownloader.downloadAocInputs(
                inputDirectory = inputsDirectory.get(),
                sessionCookieProvider = sessionCookieParam.orNull?.let { { it } } ?: sessionCookieProvider.get(),
                fileNameMapping = fileNameMapping.get(),
                aocYear = aocYear.get(),
            )
        }
    }
}
