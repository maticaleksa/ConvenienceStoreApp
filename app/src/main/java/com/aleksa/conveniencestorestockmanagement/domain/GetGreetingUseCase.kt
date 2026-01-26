package com.aleksa.conveniencestorestockmanagement.domain

import javax.inject.Inject

class GetGreetingUseCase @Inject constructor(
    private val repository: GreetingRepository,
) {
    suspend operator fun invoke(): String = repository.fetchGreeting()
}
