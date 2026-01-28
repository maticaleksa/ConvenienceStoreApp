package com.aleksa.data.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.aleksa.domain.Money
import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Supplier
import com.aleksa.domain.model.Category
import com.aleksa.data.remote.ProductDto
import com.aleksa.data.remote.SupplierDto

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("categoryId")]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val priceMinor: Long,
    val categoryId: String,
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

data class ProductWithCategory(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity
)

fun ProductWithCategory.toDomain(): Product = Product(
    id = product.id,
    name = product.name,
    description = product.description,
    price = Money.ofMinor(product.priceMinor),
    category = category.toDomain(),
    barcode = product.barcode,
    supplier = product.supplier.toDomain(),
    currentStockLevel = product.currentStockLevel,
    minimumStockLevel = product.minimumStockLevel,
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    description = description,
    priceMinor = price.minor,
    categoryId = category.id,
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

fun ProductDto.toCategoryEntity(): CategoryEntity = CategoryEntity(
    id = normalizeCategoryId(category),
    name = category
)

fun ProductDto.toEntity(categoryId: String): ProductEntity = ProductEntity(
    id = id,
    name = name,
    description = description,
    priceMinor = Money.ofDouble(price).minor,
    categoryId = categoryId,
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

private fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
)

fun normalizeCategoryId(name: String): String {
    return name
        .trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
}
