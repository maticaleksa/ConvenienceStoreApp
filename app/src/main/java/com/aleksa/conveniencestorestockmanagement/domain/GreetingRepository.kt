package com.aleksa.conveniencestorestockmanagement.domain

interface GreetingRepository {
    suspend fun fetchGreeting(): String
}
