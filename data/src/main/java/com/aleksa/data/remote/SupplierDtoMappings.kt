package com.aleksa.data.remote

import com.aleksa.domain.model.Supplier

fun Supplier.toDto(): SupplierDto = SupplierDto(
    id = id,
    name = name,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    address = address,
)
