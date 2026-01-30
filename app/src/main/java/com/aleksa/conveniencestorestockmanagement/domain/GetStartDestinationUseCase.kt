package com.aleksa.conveniencestorestockmanagement.domain

import com.aleksa.conveniencestorestockmanagement.navigation.StartDestination
import javax.inject.Inject

class GetStartDestinationUseCase @Inject constructor() {
    operator fun invoke(isAuthenticated: Boolean): StartDestination {
        return if (isAuthenticated) StartDestination.Main else StartDestination.Auth
    }
}
