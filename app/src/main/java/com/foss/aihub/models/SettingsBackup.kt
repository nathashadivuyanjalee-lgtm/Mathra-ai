package com.foss.aihub.models

import com.google.gson.annotations.SerializedName

data class SettingsBackup(
    @SerializedName("version") val version: Int,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("appSettings") val appSettings: AppSettings,
    @SerializedName("domainsEtag") val domainsEtag: String?,
    @SerializedName("aiServicesEtag") val aiServicesEtag: String?,
    @SerializedName("domainsLastUpdated") val domainsLastUpdated: String?,
    @SerializedName("aiServicesLastUpdated") val aiServicesLastUpdated: String?,
    @SerializedName("lastUpdateCheck") val lastUpdateCheck: String?,
    @SerializedName("onboardingCompleted") val onboardingCompleted: Boolean
)