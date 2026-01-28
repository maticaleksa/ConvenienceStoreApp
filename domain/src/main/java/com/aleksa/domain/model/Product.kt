package com.aleksa.domain.model

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val barcode: String,
    val supplier: Supplier,
    val currentStockLevel: Int,
    val minimumStockLevel: Int,
)
