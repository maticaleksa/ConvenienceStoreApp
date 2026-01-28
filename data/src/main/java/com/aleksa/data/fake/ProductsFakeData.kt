package com.aleksa.data.fake

import com.aleksa.data.remote.ProductDto
import com.aleksa.data.remote.SupplierDto
import com.aleksa.domain.Money
import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Supplier

val supplierA = Supplier(
    id = "supplier-1",
    name = "Acme Supplies",
    contactPerson = "John Doe",
    phone = "+123456789",
    email = "contact@acme.com",
    address = "123 Industrial St",
)

val supplierB = Supplier(
    id = "supplier-2",
    name = "FreshGoods Ltd",
    contactPerson = "Jane Smith",
    phone = "+987654321",
    email = "sales@freshgoods.com",
    address = "45 Market Ave",
)

val fakeProductsList =
    listOf(
        Product(
            id = "p1",
            name = "Milk 1L",
            description = "Fresh whole milk",
            price = Money.ofDouble(1.49),
            category = "Dairy",
            barcode = "111111",
            supplier = supplierB,
            currentStockLevel = 2,
            minimumStockLevel = 2,
        ),
        Product(
            id = "p2",
            name = "White Bread",
            description = "Sliced white bread",
            price = Money.ofDouble(0.99),
            category = "Bakery",
            barcode = "222222",
            supplier = supplierB,
            currentStockLevel = 1,
            minimumStockLevel = 1,
        ),
        Product(
            id = "p3",
            name = "Eggs (12 pack)",
            description = "Free range eggs",
            price = Money.ofDouble(2.99),
            category = "Dairy",
            barcode = "333333",
            supplier = supplierB,
            currentStockLevel = 12,
            minimumStockLevel = 4,
        ),
        Product(
            id = "p4",
            name = "Cola 0.5L",
            description = "Carbonated soft drink",
            price = Money.ofDouble(1.29),
            category = "Beverages",
            barcode = "444444",
            supplier = supplierA,
            currentStockLevel = 20,
            minimumStockLevel = 5,
        ),
        Product(
            id = "p5",
            name = "Chocolate Bar",
            description = "Milk chocolate",
            price = Money.ofDouble(0.79),
            category = "Snacks",
            barcode = "555555",
            supplier = supplierA,
            currentStockLevel = 30,
            minimumStockLevel = 10,
        ),
        Product(
            id = "p6",
            name = "Pasta 500g",
            description = "Durum wheat pasta",
            price = Money.ofDouble(1.19),
            category = "Grocery",
            barcode = "666666",
            supplier = supplierA,
            currentStockLevel = 15,
            minimumStockLevel = 5,
        ),
        Product(
            id = "p7",
            name = "Tomato Sauce",
            description = "Classic tomato sauce",
            price = Money.ofDouble(1.59),
            category = "Grocery",
            barcode = "777777",
            supplier = supplierA,
            currentStockLevel = 8,
            minimumStockLevel = 3,
        ),
    )

val fakeProductsDtoList =
    fakeProductsList.map {
        ProductDto(
            id = it.id,
            name = it.name,
            description = it.description,
            price = it.price.toDecimalString().toDouble(),
            category = it.category,
            barcode = it.barcode,
            supplier = SupplierDto(
                id = it.supplier.id,
                name = it.supplier.name,
                contactPerson = it.supplier.contactPerson,
                phone = it.supplier.phone,
                email = it.supplier.email,
                address = it.supplier.address,
            ),
            currentStockLevel = it.currentStockLevel,
            minimumStockLevel = it.minimumStockLevel,
        )
    }
