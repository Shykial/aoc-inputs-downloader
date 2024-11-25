package com.shykial.aoc.inputsDownloader

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.useDirectoryEntries
import kotlin.io.path.writeText

val AOC_ZONE_ID: ZoneId = ZoneOffset.ofHours(-5)

object InputsDownloader {
    private const val AOC_BASE_PATH = "https://adventofcode.com"
    private val httpClient = HttpClient(Java) {
        expectSuccess = true
    }

    suspend fun downloadAocInputs(
        inputDirectory: Path,
        aocYear: Int,
        fileNameMapping: (Int) -> String,
        sessionCookieProvider: () -> String,
    ) {
        if (!inputDirectory.isDirectory()) inputDirectory.createDirectories()
        val lastDayOfAoc = ZonedDateTime.of(aocYear, 12, 25, 0, 0, 0, 0, AOC_ZONE_ID)
        val daysWithMissingInputs = getDaysWithMissingInputs(
            inputDirectory = inputDirectory,
            lastDayOfAoc = lastDayOfAoc,
            fileNameMapping = fileNameMapping,
        ).ifEmpty { return }
        downloadMissingInputs(
            aocYear = aocYear,
            inputDirectory = inputDirectory,
            daysToDownload = daysWithMissingInputs,
            sessionCookie = sessionCookieProvider(),
            fileNameMapping = fileNameMapping,
        )
    }

    private suspend fun downloadMissingInputs(
        aocYear: Int,
        inputDirectory: Path,
        daysToDownload: Collection<Int>,
        sessionCookie: String,
        fileNameMapping: (Int) -> String,
    ) {
        coroutineScope {
            println("Downloading inputs for days $daysToDownload")
            val counter = AtomicInteger()
            daysToDownload.forEach { dayNumber ->
                launch {
                    val downloadedInput = downloadDayInputAsString(
                        aocYear = aocYear,
                        dayNumber = dayNumber,
                        sessionCookie = sessionCookie,
                    )
                    inputDirectory
                        .resolve(fileNameMapping(dayNumber))
                        .writeText(downloadedInput)
                    println("Downloaded input for day $dayNumber\t-\t${counter.incrementAndGet()}/${daysToDownload.size}")
                }
            }
        }
    }

    private fun getDaysWithMissingInputs(
        inputDirectory: Path,
        lastDayOfAoc: ZonedDateTime,
        fileNameMapping: (Int) -> String,
    ): Collection<Int> {
        val now = ZonedDateTime.now(AOC_ZONE_ID)
        val availableDays = 1..minOf(now, lastDayOfAoc).dayOfMonth
        val availableDaysByFileName = availableDays.associateBy(fileNameMapping)
        val existingFilesNames = inputDirectory.useDirectoryEntries { entries ->
            entries.map { it.name }.toSet()
        }
        return (availableDaysByFileName - existingFilesNames).values
    }

    private suspend fun downloadDayInputAsString(
        aocYear: Int,
        dayNumber: Int,
        sessionCookie: String,
    ) = httpClient.get {
        url("$AOC_BASE_PATH/$aocYear/day/$dayNumber/input")
        cookie("session", sessionCookie)
    }.bodyAsText()
}
