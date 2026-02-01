package com.aleksa.conveniencestorestockmanagement.fakes

import com.aleksa.domain.Money
import com.aleksa.domain.model.Category
import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Supplier

fun product(
    id: String,
    name: String,
    category: Category,
    barcode: String = id,
    supplier: Supplier = Supplier(
        id = "s1",
        name = "Supplier",
        contactPerson = "Contact",
        phone = "123",
        email = "test@example.com",
        address = "Address",
    ),
    description: String = "",
    price: Money = Money.zero(),
    currentStockLevel: Int = 10,
    minimumStockLevel: Int = 2,
): Product {
    return Product(
        id = id,
        name = name,
        description = description,
        price = price,
        category = category,
        barcode = barcode,
        supplier = supplier,
        currentStockLevel = currentStockLevel,
        minimumStockLevel = minimumStockLevel,
    )
}
