package com.aleksa.conveniencestorestockmanagement.navigation

sealed class StartDestination {
    object Auth : StartDestination()
    object Main : StartDestination()
}
