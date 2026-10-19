package com.davi.dev.gnixia.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data object AboutRoute : NavKey

@Serializable
data object SettingsRoute : NavKey

@Serializable
data object ChatRoute : NavKey

@Serializable
data object HistoryRoute : NavKey

@Serializable
data class HistoryDetailRoute(val id: Long) : NavKey
