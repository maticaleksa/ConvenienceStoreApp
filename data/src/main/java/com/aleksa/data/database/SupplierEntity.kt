package com.aleksa.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aleksa.data.remote.SupplierDto
import com.aleksa.domain.model.Supplier

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String,
    val name: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val address: String,
)

fun SupplierEntity.toDomain(): Supplier = Supplier(
    id = id,
    name = name,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    address = address,
)

fun Supplier.toEntity(): SupplierEntity = SupplierEntity(
    id = id,
    name = name,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    address = address,
)

fun SupplierDto.toEntity(): SupplierEntity = SupplierEntity(
    id = id,
    name = name,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    address = address,
)
