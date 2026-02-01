package com.aleksa.conveniencestorestockmanagement.fakes

import com.aleksa.domain.model.Supplier

fun supplier(
    id: String,
    name: String,
    contactPerson: String = "Contact",
    phone: String = "123",
    email: String = "test@example.com",
    address: String = "Address",
): Supplier {
    return Supplier(
        id = id,
        name = name,
        contactPerson = contactPerson,
        phone = phone,
        email = email,
        address = address,
    )
}
