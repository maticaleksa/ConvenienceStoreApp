package com.aleksa.data.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aleksa.domain.Money
import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Supplier
import com.aleksa.data.remote.ProductDto
import com.aleksa.data.remote.SupplierDto

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val priceMinor: Long,
    val category: String,
    val barcode: String,
    @Embedded(prefix = "supplier_") val supplier: SupplierEmbedded,
    val currentStockLevel: Int,
    val minimumStockLevel: Int,
)

data class SupplierEmbedded(
    val id: String,
    val name: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val address: String,
)

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    description = description,
    price = Money.ofMinor(priceMinor),
    category = category,
    barcode = barcode,
    supplier = supplier.toDomain(),
    currentStockLevel = currentStockLevel,
    minimumStockLevel = minimumStockLevel,
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    description = description,
    priceMinor = price.minor,
    category = category,
    barcode = barcode,
    supplier = supplier.toEmbedded(),
    currentStockLevel = currentStockLevel,
    minimumStockLevel = minimumStockLevel,
)

private fun SupplierEmbedded.toDomain(): Supplier = Supplier(
    id = id,
    name = name,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    address = address,
)

private fun Supplier.toEmbedded(): SupplierEmbedded = SupplierEmbedded(
    id = id,
    name = name,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    address = address,
)

fun ProductDto.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    description = description,
    priceMinor = Money.ofDouble(price).minor,
    category = category,
    barcode = barcode,
    supplier = supplier.toEmbedded(),
    currentStockLevel = currentStockLevel,
    minimumStockLevel = minimumStockLevel,
)

private fun SupplierDto.toEmbedded(): SupplierEmbedded = SupplierEmbedded(
    id = id,
    name = name,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    address = address,
)
