package com.aleksa.conveniencestorestockmanagement.fakes

import com.aleksa.domain.SupplierRepository
import com.aleksa.domain.model.Supplier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

open class FakeSupplierRepository : SupplierRepository {
    val suppliers = MutableStateFlow<List<Supplier>>(emptyList())

    override fun observeAll(): Flow<List<Supplier>> = suppliers

    override fun observeSearch(query: String): Flow<List<Supplier>> {
        val trimmed = query.trim()
        return suppliers.map { list ->
            if (trimmed.isBlank()) {
                list
            } else {
                list.filter { supplier ->
                    supplier.name.contains(trimmed, ignoreCase = true) ||
                        supplier.contactPerson.contains(trimmed, ignoreCase = true) ||
                        supplier.phone.contains(trimmed, ignoreCase = true) ||
                        supplier.email.contains(trimmed, ignoreCase = true) ||
                        supplier.address.contains(trimmed, ignoreCase = true)
                }
            }
        }
    }

    override suspend fun upsert(supplier: Supplier) {
        val updated = suppliers.value.toMutableList()
        val index = updated.indexOfFirst { it.id == supplier.id }
        if (index >= 0) {
            updated[index] = supplier
        } else {
            updated.add(supplier)
        }
        suppliers.value = updated
    }
}
