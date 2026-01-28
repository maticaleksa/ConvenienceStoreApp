package com.aleksa.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val barcode: String,
    val supplier: SupplierDto,
    val currentStockLevel: Int,
    val minimumStockLevel: Int,
)

@Serializable
data class SupplierDto(
    val id: String,
    val name: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val address: String,
)
