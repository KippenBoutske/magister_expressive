package nl.vibe.magister.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {

    @Serializable
    data object Login : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Grades : Screen

    @Serializable
    data object Homework : Screen

    @Serializable
    data class Detail(val id: Long) : Screen
}
