package com.foss.aihub.utils

import android.content.Context
import android.net.Uri
import com.foss.aihub.models.SettingsBackup
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDate

class SettingsBackupHelper(
    private val settingsManager: SettingsManager, private val gson: Gson = Gson()
) {

    suspend fun backupToUri(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val backup = SettingsBackup(
                version = settingsManager.getSettingVersion(),
                timestamp = LocalDate.now().toString(),
                appSettings = settingsManager.loadSettings(),
                domainsEtag = settingsManager.getDomainsEtag(),
                aiServicesEtag = settingsManager.getAiServicesEtag(),
                domainsLastUpdated = settingsManager.getDomainsLastUpdatedDate().toString(),
                aiServicesLastUpdated = settingsManager.getAiServicesLastUpdatedDate().toString(),
                lastUpdateCheck = settingsManager.getLastUpdateCheckDate().toString(),
                onboardingCompleted = settingsManager.isOnboardingCompleted()
            )

            val json = gson.toJson(backup)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
                return@withContext true
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun restoreFromUri(context: Context, uri: Uri): SettingsBackup? =
        withContext(Dispatchers.IO) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        reader.readText()
                    }
                } ?: throw IllegalArgumentException("Unable to open input stream for URI $uri")

                val backup = try {
                    gson.fromJson(json, SettingsBackup::class.java)
                } catch (e: JsonSyntaxException) {
                    throw IllegalArgumentException("Invalid JSON format", e)
                } ?: throw IllegalArgumentException("Backup is null or malformed")

                backup.domainsEtag?.let { settingsManager.saveDomainsEtag(it) }
                backup.aiServicesEtag?.let { settingsManager.saveAiServicesEtag(it) }

                backup.domainsLastUpdated?.let { settingsManager.saveDomainsLastUpdatedDate(it) }
                backup.aiServicesLastUpdated?.let { settingsManager.saveAiServicesLastUpdatedDate(it) }
                backup.lastUpdateCheck?.let { settingsManager.saveLastUpdateCheckDate(it) }

                settingsManager.setOnboardingCompleted(backup.onboardingCompleted)

                backup
            } catch (_: Exception) {
                null
            }
        }
}