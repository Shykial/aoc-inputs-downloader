package com.shykial.aoc.inputsDownloader

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.useDirectoryEntries
import kotlin.io.path.writeText

object InputsDownloader {
    private const val AOC_BASE_PATH = "https://adventofcode.com"
    private val daysInputFilesRegex = Regex("""Day(\d\d)\.txt""")
    private val httpClient = HttpClient(Java) {
        expectSuccess = true
    }

    suspend fun downloadMissingInputs(inputDirectory: Path, sessionCookie: String, aocYear: Int) {
        if (!inputDirectory.exists()) inputDirectory.createDirectory()
        val lastDayOfAoc = OffsetDateTime.of(aocYear, 12, 25, 0, 0, 0, 0, ZoneOffset.UTC)
        val daysWithMissingInputs = getDaysWithMissingInputs(inputDirectory, lastDayOfAoc)
        downloadMissingInputs(
            daysToDownload = daysWithMissingInputs,
            aocYear = aocYear,
            sessionCookie = sessionCookie,
            inputDirectory = inputDirectory
        )
    }

    private suspend fun downloadMissingInputs(
        daysToDownload: List<Int>,
        aocYear: Int,
        sessionCookie: String,
        inputDirectory: Path
    ) = coroutineScope {
        println("Downloading inputs for days $daysToDownload")
        val counter = AtomicInteger()
        daysToDownload.forEach { dayNumber ->
            launch {
                val downloadedInput = downloadInputString(
                    aocYear = aocYear,
                    dayNumber = dayNumber,
                    sessionCookie = sessionCookie
                )
                createAocInputFile(inputDirectory, dayNumber, downloadedInput)
                println("Downloaded input for day $dayNumber\t-\t${counter.incrementAndGet()}/${daysToDownload.size}")
            }
        }
    }

    private fun getDaysWithMissingInputs(inputDirectory: Path, lastDayOfAoc: OffsetDateTime): List<Int> {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val availableDays = 1..minOf(now, lastDayOfAoc).dayOfMonth
        val existingInputDays = inputDirectory.useDirectoryEntries { entries ->
            entries
                .mapNotNull { daysInputFilesRegex.find(it.name)?.groupValues?.getOrNull(1)?.toInt() }
                .toSet()
        }
        return availableDays - existingInputDays
    }

    private suspend fun downloadInputString(aocYear: Int, dayNumber: Int, sessionCookie: String) =
        httpClient.get {
            url("$AOC_BASE_PATH/$aocYear/day/$dayNumber/input")
            cookie("session", sessionCookie)
        }.bodyAsText()

    private fun createAocInputFile(inputDirectory: Path, dayNumber: Int, downloadedInput: String) {
        inputDirectory
            .resolve("Day${dayNumber.toPaddedString()}.txt")
            .writeText(downloadedInput)
    }

    private fun Int.toPaddedString() = toString().padStart(2, '0')
}
