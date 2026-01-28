package com.aleksa.domain.model

import com.aleksa.domain.Money

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Money,
    val category: Category,
    val barcode: String,
    val supplier: Supplier,
    val currentStockLevel: Int,
    val minimumStockLevel: Int,
)
