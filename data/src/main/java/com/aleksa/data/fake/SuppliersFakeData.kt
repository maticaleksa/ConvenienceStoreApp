package com.aleksa.data.fake

import com.aleksa.data.remote.SupplierDto
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

val supplierC = Supplier(
    id = "supplier-3",
    name = "Daily Essentials",
    contactPerson = "Mark Lee",
    phone = "+1122334455",
    email = "support@dailyessentials.com",
    address = "78 Supply Rd",
)

val supplierD = Supplier(
    id = "supplier-4",
    name = "Urban Grocers",
    contactPerson = "Elena Novak",
    phone = "+447700900111",
    email = "hello@urbangrocers.com",
    address = "10 City Plaza",
)

val supplierE = Supplier(
    id = "supplier-5",
    name = "Harbor Foods",
    contactPerson = "Luis Ortega",
    phone = "+1555123456",
    email = "orders@harborfoods.com",
    address = "5 Dockside Way",
)

val supplierF = Supplier(
    id = "supplier-6",
    name = "Sunrise Produce",
    contactPerson = "Priya Shah",
    phone = "+918888123456",
    email = "sales@sunriseproduce.com",
    address = "22 Orchard Lane",
)

val fakeSuppliersList = listOf(supplierA, supplierB, supplierC, supplierD, supplierE, supplierF)

val fakeSuppliersDtoList =
    fakeSuppliersList.map {
        SupplierDto(
            id = it.id,
            name = it.name,
            contactPerson = it.contactPerson,
            phone = it.phone,
            email = it.email,
            address = it.address,
        )
    }
