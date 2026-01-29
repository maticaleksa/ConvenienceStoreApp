package com.aleksa.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.aleksa.domain.Money
import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Category
import com.aleksa.data.remote.ProductDto

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
        ),
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.RESTRICT
        ),
    ],
    indices = [Index("categoryId"), Index("supplierId")]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val priceMinor: Long,
    val categoryId: String,
    val supplierId: String,
    val barcode: String,
    val currentStockLevel: Int,
    val minimumStockLevel: Int,
)

data class ProductWithCategorySupplier(
    @androidx.room.Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity,
    @Relation(
        parentColumn = "supplierId",
        entityColumn = "id"
    )
    val supplier: SupplierEntity,
)

fun ProductWithCategorySupplier.toDomain(): Product = Product(
    id = product.id,
    name = product.name,
    description = product.description,
    price = Money.ofMinor(product.priceMinor),
    category = category.toDomain(),
    barcode = product.barcode,
    supplier = supplier.toDomain(),
    currentStockLevel = product.currentStockLevel,
    minimumStockLevel = product.minimumStockLevel,
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    description = description,
    priceMinor = price.minor,
    categoryId = category.id,
    supplierId = supplier.id,
    barcode = barcode,
    currentStockLevel = currentStockLevel,
    minimumStockLevel = minimumStockLevel,
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
    supplierId = supplierId,
    barcode = barcode,
    currentStockLevel = currentStockLevel,
    minimumStockLevel = minimumStockLevel,
)

private fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
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
