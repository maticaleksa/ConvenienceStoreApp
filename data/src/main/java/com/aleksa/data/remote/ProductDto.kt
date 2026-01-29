package com.aleksa.data.remote

import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Supplier
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

fun Product.toDto(): ProductDto = ProductDto(
    id = id,
    name = name,
    description = description,
    price = price.minor / 100.0,
    category = category.name,
    barcode = barcode,
    supplier = supplier.toDto(),
    currentStockLevel = currentStockLevel,
    minimumStockLevel = minimumStockLevel,
)

private fun Supplier.toDto(): SupplierDto = SupplierDto(
    id = id,
    name = name,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    address = address,
)
